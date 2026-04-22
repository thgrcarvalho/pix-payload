package io.github.thgrcarvalho.pix;

import io.github.thgrcarvalho.pix.internal.Crc16;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Crc16Test {

    // Reference vector from BCB's Pix EMV-QRCPS-MPM specification
    private static final String BCB_PAYLOAD_WITHOUT_CRC =
            "00020126580014BR.GOV.BCB.PIX0136123e4567-e12b-12d1-a456-4266554400005204000053039865802BR5913Fulano de Tal6008BRASILIA62070503***6304";

    // Standard check vector for CRC16/CCITT-FALSE (poly=0x1021, init=0xFFFF)
    @Test
    void computesKnownValueForStandardTestVector() {
        assertEquals(0x29B1, Crc16.compute("123456789"));
    }

    @Test
    void computesCorrectCrcForBcbReferenceVector() {
        assertEquals("F01B", Crc16.computeHex(BCB_PAYLOAD_WITHOUT_CRC));
    }

    @Test
    void computesInitValueForEmptyString() {
        assertEquals(0xFFFF, Crc16.compute(""));
    }

    @Test
    void computeHexIsFourUppercaseCharacters() {
        String hex = Crc16.computeHex("test");
        assertEquals(4, hex.length());
        assertEquals(hex, hex.toUpperCase());
    }
}
