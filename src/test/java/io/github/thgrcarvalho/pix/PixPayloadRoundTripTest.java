package io.github.thgrcarvalho.pix;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PixPayloadRoundTripTest {

    @Test
    void minimalPayloadSurvivesRoundTrip() {
        PixPayload original = PixPayload.newBuilder()
                .pixKey("thiago@example.com")
                .merchantName("Thiago Carvalho")
                .merchantCity("Rio de Janeiro")
                .build();

        PixPayload parsed = PixPayload.parse(original.encode());

        assertEquals(original, parsed);
    }

    @Test
    void fullPayloadSurvivesRoundTrip() {
        PixPayload original = PixPayload.newBuilder()
                .pixKey("thiago@example.com")
                .merchantName("Thiago Carvalho")
                .merchantCity("Rio de Janeiro")
                .amount(new BigDecimal("199.90"))
                .txId("SUB20250422")
                .additionalInfo("Plano mensal Premium")
                .postalCode("20040020")
                .dynamic(true)
                .build();

        PixPayload parsed = PixPayload.parse(original.encode());

        assertEquals(original, parsed);
    }

    @Test
    void encodeIsStableAcrossMultipleCalls() {
        PixPayload payload = PixPayload.newBuilder()
                .pixKey("stable@example.com")
                .merchantName("Stable Test")
                .merchantCity("Brasilia")
                .build();

        assertEquals(payload.encode(), payload.encode());
    }

    @Test
    void encodeThenParsePreservesAmount() {
        BigDecimal amount = new BigDecimal("1234.56");

        PixPayload parsed = PixPayload.parse(
                PixPayload.newBuilder()
                        .pixKey("test@example.com")
                        .merchantName("Test Merchant")
                        .merchantCity("Test City")
                        .amount(amount)
                        .build()
                        .encode()
        );

        assertEquals(0, amount.compareTo(parsed.getAmount()));
    }
}
