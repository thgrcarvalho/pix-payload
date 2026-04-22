package io.github.thgrcarvalho.pix.internal;

import io.github.thgrcarvalho.pix.PixPayload;

import java.math.RoundingMode;

public final class EmvEncoder {

    private EmvEncoder() {}

    public static String encode(PixPayload p) {
        StringBuilder sb = new StringBuilder();

        // 00 — Payload Format Indicator (always "01")
        sb.append(field("00", "01"));

        // 01 — Point of Initiation: "12" = dynamic (single-use), absent = static (reusable)
        if (p.isDynamic()) {
            sb.append(field("01", "12"));
        }

        // 26 — Merchant Account Information
        StringBuilder mai = new StringBuilder();
        mai.append(field("00", "BR.GOV.BCB.PIX"));
        mai.append(field("01", p.getPixKey()));
        if (p.getAdditionalInfo() != null) {
            mai.append(field("02", p.getAdditionalInfo()));
        }
        sb.append(field("26", mai.toString()));

        // 52 — Merchant Category Code (always "0000" for Pix)
        sb.append(field("52", "0000"));

        // 53 — Transaction Currency (986 = BRL)
        sb.append(field("53", "986"));

        // 54 — Transaction Amount (optional)
        if (p.getAmount() != null) {
            String amount = p.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
            sb.append(field("54", amount));
        }

        // 58 — Country Code
        sb.append(field("58", "BR"));

        // 59 — Merchant Name
        sb.append(field("59", p.getMerchantName()));

        // 60 — Merchant City
        sb.append(field("60", p.getMerchantCity()));

        // 61 — Postal Code (optional)
        if (p.getPostalCode() != null) {
            sb.append(field("61", p.getPostalCode()));
        }

        // 62 — Additional Data Field
        String txId = p.getTxId() != null ? p.getTxId() : "***";
        sb.append(field("62", field("05", txId)));

        // 63 — CRC16: computed over the full payload including "6304"
        sb.append("6304");
        sb.append(Crc16.computeHex(sb.toString()));

        return sb.toString();
    }

    private static String field(String id, String value) {
        return id + String.format("%02d", value.length()) + value;
    }
}
