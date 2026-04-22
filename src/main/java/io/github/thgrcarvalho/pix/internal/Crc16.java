package io.github.thgrcarvalho.pix.internal;

/**
 * CRC16/CCITT-FALSE as required by the Pix EMV-QRCPS-MPM specification.
 * Polynomial: 0x1021 | Init: 0xFFFF | RefIn: false | RefOut: false | XorOut: 0x0000
 */
public final class Crc16 {

    private Crc16() {}

    public static int compute(String input) {
        int crc = 0xFFFF;
        for (char c : input.toCharArray()) {
            crc ^= (c << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }
        return crc;
    }

    public static String computeHex(String input) {
        return String.format("%04X", compute(input));
    }
}
