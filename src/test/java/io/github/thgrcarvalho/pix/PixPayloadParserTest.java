package io.github.thgrcarvalho.pix;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PixPayloadParserTest {

    private static final String BCB_REFERENCE =
            "00020126580014BR.GOV.BCB.PIX0136123e4567-e12b-12d1-a456-4266554400005204000053039865802BR5913Fulano de Tal6008BRASILIA62070503***6304F01B";

    @Test
    void parsesBcbReferenceVector() {
        PixPayload payload = PixPayload.parse(BCB_REFERENCE);

        assertEquals("123e4567-e12b-12d1-a456-426655440000", payload.getPixKey());
        assertEquals("Fulano de Tal", payload.getMerchantName());
        assertEquals("BRASILIA", payload.getMerchantCity());
        assertNull(payload.getAmount());
        assertNull(payload.getTxId());
        assertFalse(payload.isDynamic());
    }

    @Test
    void parsesAmountCorrectly() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .amount(new BigDecimal("123.45"))
                .build()
                .encode();

        PixPayload parsed = PixPayload.parse(encoded);

        assertNotNull(parsed.getAmount());
        assertEquals(0, new BigDecimal("123.45").compareTo(parsed.getAmount()));
    }

    @Test
    void parsesTxIdCorrectly() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .txId("SUB20250001")
                .build()
                .encode();

        PixPayload parsed = PixPayload.parse(encoded);

        assertEquals("SUB20250001", parsed.getTxId());
    }

    @Test
    void parsesDynamicFlagCorrectly() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .dynamic(true)
                .build()
                .encode();

        PixPayload parsed = PixPayload.parse(encoded);

        assertTrue(parsed.isDynamic());
    }

    @Test
    void parsesPostalCodeCorrectly() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .postalCode("01310100")
                .build()
                .encode();

        PixPayload parsed = PixPayload.parse(encoded);

        assertEquals("01310100", parsed.getPostalCode());
    }

    @Test
    void parsesAdditionalInfoCorrectly() {
        String encoded = PixPayload.newBuilder()
                .pixKey("joao@example.com")
                .merchantName("Joao Silva")
                .merchantCity("Sao Paulo")
                .additionalInfo("Mensalidade Fevereiro")
                .build()
                .encode();

        PixPayload parsed = PixPayload.parse(encoded);

        assertEquals("Mensalidade Fevereiro", parsed.getAdditionalInfo());
    }

    @Test
    void parseStripsLeadingAndTrailingWhitespace() {
        PixPayload payload = PixPayload.parse("  " + BCB_REFERENCE + "  ");
        assertEquals("Fulano de Tal", payload.getMerchantName());
    }

    @Test
    void parseThrowsOnInvalidCrc() {
        String tampered = BCB_REFERENCE.substring(0, BCB_REFERENCE.length() - 4) + "0000";
        assertThrows(PixPayloadException.class, () -> PixPayload.parse(tampered));
    }

    @Test
    void parseThrowsOnTooShortInput() {
        assertThrows(PixPayloadException.class, () -> PixPayload.parse("0002"));
    }

    @Test
    void parseThrowsOnNullInput() {
        assertThrows(NullPointerException.class, () -> PixPayload.parse(null));
    }
}
