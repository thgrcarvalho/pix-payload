package io.github.thgrcarvalho.pix;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PixPayloadEncoderTest {

    // BCB reference payload — EVP key, no amount, static QR
    // CRC16/CCITT-FALSE (poly=0x1021, init=0xFFFF) of the payload content
    private static final String BCB_REFERENCE =
            "00020126580014BR.GOV.BCB.PIX0136123e4567-e12b-12d1-a456-4266554400005204000053039865802BR5913Fulano de Tal6008BRASILIA62070503***6304F01B";

    @Test
    void encodeMatchesBcbReferenceVector() {
        String encoded = PixPayload.newBuilder()
                .pixKey("123e4567-e12b-12d1-a456-426655440000")
                .merchantName("Fulano de Tal")
                .merchantCity("BRASILIA")
                .build()
                .encode();

        assertEquals(BCB_REFERENCE, encoded);
    }

    @Test
    void encodeIncludesAmountWhenProvided() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .amount(new BigDecimal("99.90"))
                .build()
                .encode();

        assertTrue(encoded.contains("5405"), "should contain field 54 with length 5");
        assertTrue(encoded.contains("99.90"));
    }

    @Test
    void encodeOmitsAmountFieldWhenNotProvided() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .build()
                .encode();

        assertFalse(encoded.contains("5406"), "should not contain amount field");
    }

    @Test
    void encodeSetsDynamicFlagWhenRequested() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .dynamic(true)
                .build()
                .encode();

        assertTrue(encoded.startsWith("00020101021226"), "dynamic flag must be field 01 = '12'");
    }

    @Test
    void encodeIncludesTxIdWhenProvided() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .txId("SUB001")
                .build()
                .encode();

        assertTrue(encoded.contains("SUB001"));
    }

    @Test
    void encodeUsesPlaceholderWhenTxIdAbsent() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .build()
                .encode();

        assertTrue(encoded.contains("0503***"), "txId placeholder must be '***'");
    }

    @Test
    void encodeIncludesAdditionalInfoWhenProvided() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .additionalInfo("Mensalidade Janeiro")
                .build()
                .encode();

        assertTrue(encoded.contains("Mensalidade Janeiro"));
    }

    @Test
    void encodeIncludesPostalCodeWhenProvided() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .postalCode("01310100")
                .build()
                .encode();

        assertTrue(encoded.contains("6108"), "should contain field 61 with length 8");
        assertTrue(encoded.contains("01310100"));
    }

    @Test
    void encodedPayloadAlwaysEndsWithFourHexDigitCrc() {
        String encoded = PixPayload.newBuilder()
                .pixKey("test@example.com")
                .merchantName("Test")
                .merchantCity("Test City")
                .build()
                .encode();

        String crc = encoded.substring(encoded.length() - 4);
        assertTrue(crc.matches("[0-9A-F]{4}"), "CRC must be 4 uppercase hex digits");
    }

    @Test
    void buildThrowsWhenPixKeyMissing() {
        assertThrows(PixPayloadException.class, () ->
                PixPayload.newBuilder()
                        .merchantName("Joao")
                        .merchantCity("Rio")
                        .build()
        );
    }

    @Test
    void buildThrowsWhenMerchantNameExceedsLimit() {
        assertThrows(PixPayloadException.class, () ->
                PixPayload.newBuilder()
                        .pixKey("joao@example.com")
                        .merchantName("A".repeat(26))
                        .merchantCity("Rio")
                        .build()
        );
    }

    @Test
    void buildThrowsWhenMerchantCityExceedsLimit() {
        assertThrows(PixPayloadException.class, () ->
                PixPayload.newBuilder()
                        .pixKey("joao@example.com")
                        .merchantName("Joao")
                        .merchantCity("A".repeat(16))
                        .build()
        );
    }

    @Test
    void buildThrowsWhenTxIdContainsSpecialCharacters() {
        assertThrows(PixPayloadException.class, () ->
                PixPayload.newBuilder()
                        .pixKey("joao@example.com")
                        .merchantName("Joao")
                        .merchantCity("Rio")
                        .txId("INVALID-TX-ID!")
                        .build()
        );
    }

    @Test
    void buildThrowsWhenAmountIsNegative() {
        assertThrows(PixPayloadException.class, () ->
                PixPayload.newBuilder()
                        .pixKey("joao@example.com")
                        .merchantName("Joao")
                        .merchantCity("Rio")
                        .amount(new BigDecimal("-1.00"))
                        .build()
        );
    }
}
