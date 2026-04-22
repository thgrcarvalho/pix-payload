package io.github.thgrcarvalho.pix;

import io.github.thgrcarvalho.pix.internal.Crc16;

import java.math.BigDecimal;

final class EmvDecoder {

    private EmvDecoder() {}

    static PixPayload decode(String payload) {
        validateCrc(payload);

        String pixKey = null;
        String merchantName = null;
        String merchantCity = null;
        BigDecimal amount = null;
        String txId = null;
        String additionalInfo = null;
        String postalCode = null;
        boolean dynamic = false;

        int idx = 0;
        while (idx < payload.length()) {
            if (idx + 4 > payload.length()) break;

            String id = payload.substring(idx, idx + 2);

            if ("63".equals(id)) break; // CRC field — stop

            int len;
            try {
                len = Integer.parseInt(payload.substring(idx + 2, idx + 4));
            } catch (NumberFormatException e) {
                throw new PixPayloadException("Malformed payload: invalid length at position " + idx);
            }

            if (idx + 4 + len > payload.length()) {
                throw new PixPayloadException("Malformed payload: field " + id + " exceeds payload length");
            }

            String value = payload.substring(idx + 4, idx + 4 + len);
            idx += 4 + len;

            switch (id) {
                case "01" -> dynamic = "12".equals(value);
                case "26" -> {
                    String[] maiResult = parseMai(value);
                    pixKey = maiResult[0];
                    additionalInfo = maiResult[1];
                }
                case "54" -> amount = new BigDecimal(value);
                case "59" -> merchantName = value;
                case "60" -> merchantCity = value;
                case "61" -> postalCode = value;
                case "62" -> txId = parseAdf(value);
            }
        }

        if (pixKey == null) {
            throw new PixPayloadException("Invalid payload: missing Pix key (field 26/01)");
        }
        if (merchantName == null) {
            throw new PixPayloadException("Invalid payload: missing Merchant Name (field 59)");
        }
        if (merchantCity == null) {
            throw new PixPayloadException("Invalid payload: missing Merchant City (field 60)");
        }

        return new PixPayload(pixKey, merchantName, merchantCity, amount, txId, additionalInfo, postalCode, dynamic);
    }

    private static String[] parseMai(String mai) {
        String pixKey = null;
        String additionalInfo = null;
        int idx = 0;

        while (idx + 4 <= mai.length()) {
            String id = mai.substring(idx, idx + 2);
            int len = Integer.parseInt(mai.substring(idx + 2, idx + 4));
            String value = mai.substring(idx + 4, idx + 4 + len);
            idx += 4 + len;

            switch (id) {
                case "01" -> pixKey = value;
                case "02" -> additionalInfo = value;
            }
        }

        return new String[]{pixKey, additionalInfo};
    }

    private static String parseAdf(String adf) {
        int idx = 0;
        while (idx + 4 <= adf.length()) {
            String id = adf.substring(idx, idx + 2);
            int len = Integer.parseInt(adf.substring(idx + 2, idx + 4));
            String value = adf.substring(idx + 4, idx + 4 + len);
            idx += 4 + len;

            if ("05".equals(id) && !"***".equals(value)) {
                return value;
            }
        }
        return null;
    }

    private static void validateCrc(String payload) {
        if (payload == null || payload.length() < 8) {
            throw new PixPayloadException("Payload is too short to be valid");
        }

        // Payload ends with: ...6304XXXX
        // CRC was computed over everything including "6304"
        int crcValueStart = payload.length() - 4;
        String preCrc = payload.substring(0, crcValueStart);

        if (!preCrc.endsWith("6304")) {
            throw new PixPayloadException("Payload does not contain a valid CRC field (expected ...6304XXXX at end)");
        }

        String expected = Crc16.computeHex(preCrc);
        String actual = payload.substring(crcValueStart).toUpperCase();

        if (!expected.equals(actual)) {
            throw new PixPayloadException("CRC mismatch: expected " + expected + " but got " + actual);
        }
    }
}
