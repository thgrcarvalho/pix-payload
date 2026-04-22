package io.github.thgrcarvalho.pix;

import io.github.thgrcarvalho.pix.internal.EmvEncoder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a Pix Copia e Cola payload, following the EMV-QRCPS-MPM specification
 * with the Brazilian Pix extensions defined by Banco Central do Brasil.
 *
 * <p>Use {@link #newBuilder()} to construct a payload for encoding, or {@link #parse(String)}
 * to decode an existing copy-and-paste string.</p>
 *
 * <pre>{@code
 * // Encoding
 * String encoded = PixPayload.newBuilder()
 *     .pixKey("thiago@example.com")
 *     .merchantName("Thiago Carvalho")
 *     .merchantCity("Rio de Janeiro")
 *     .amount(new BigDecimal("49.90"))
 *     .txId("SUB001")
 *     .build()
 *     .encode();
 *
 * // Decoding
 * PixPayload payload = PixPayload.parse(encoded);
 * }</pre>
 */
public final class PixPayload {

    private static final int MAX_MERCHANT_NAME = 25;
    private static final int MAX_MERCHANT_CITY = 15;
    private static final Pattern TX_ID_PATTERN = Pattern.compile("[A-Za-z0-9]{1,25}");

    private final String pixKey;
    private final String merchantName;
    private final String merchantCity;
    private final BigDecimal amount;
    private final String txId;
    private final String additionalInfo;
    private final String postalCode;
    private final boolean dynamic;

    // Package-private: used by EmvDecoder to bypass builder validation on parsed payloads
    PixPayload(String pixKey, String merchantName, String merchantCity,
               BigDecimal amount, String txId, String additionalInfo,
               String postalCode, boolean dynamic) {
        this.pixKey = pixKey;
        this.merchantName = merchantName;
        this.merchantCity = merchantCity;
        this.amount = amount != null ? amount.setScale(2, RoundingMode.HALF_UP) : null;
        this.txId = txId;
        this.additionalInfo = additionalInfo;
        this.postalCode = postalCode;
        this.dynamic = dynamic;
    }

    /** The Pix key (chave Pix): e-mail, CPF, CNPJ, phone, or EVP (random key). */
    public String getPixKey() { return pixKey; }

    /** Merchant or recipient name, max 25 characters. */
    public String getMerchantName() { return merchantName; }

    /** Merchant or recipient city, max 15 characters. */
    public String getMerchantCity() { return merchantCity; }

    /** Transaction amount. {@code null} means the payer can enter any value. */
    public BigDecimal getAmount() { return amount; }

    /**
     * Transaction identifier (txid). Alphanumeric, max 25 characters.
     * Used to correlate the Pix payment back to this specific charge.
     * {@code null} means no correlation is needed (static QR for open payments).
     */
    public String getTxId() { return txId; }

    /** Optional free-text description shown to the payer. */
    public String getAdditionalInfo() { return additionalInfo; }

    /** Optional postal code of the merchant location. */
    public String getPostalCode() { return postalCode; }

    /**
     * Whether this is a dynamic payload (single-use). Dynamic payloads encode
     * Point of Initiation Method = "12". Static (reusable) payloads omit that field.
     */
    public boolean isDynamic() { return dynamic; }

    /** Encodes this payload into a Pix Copia e Cola string. */
    public String encode() {
        return EmvEncoder.encode(this);
    }

    /**
     * Parses a Pix Copia e Cola string into a {@link PixPayload}.
     *
     * @throws PixPayloadException if the CRC is invalid or required fields are missing
     */
    public static PixPayload parse(String payload) {
        Objects.requireNonNull(payload, "payload must not be null");
        return EmvDecoder.decode(payload.trim());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PixPayload that)) return false;
        return dynamic == that.dynamic
                && Objects.equals(pixKey, that.pixKey)
                && Objects.equals(merchantName, that.merchantName)
                && Objects.equals(merchantCity, that.merchantCity)
                && amountEquals(amount, that.amount)
                && Objects.equals(txId, that.txId)
                && Objects.equals(additionalInfo, that.additionalInfo)
                && Objects.equals(postalCode, that.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                pixKey, merchantName, merchantCity,
                amount != null ? amount.stripTrailingZeros() : null,
                txId, additionalInfo, postalCode, dynamic
        );
    }

    @Override
    public String toString() {
        return "PixPayload{pixKey='%s', merchantName='%s', merchantCity='%s', amount=%s, dynamic=%s}"
                .formatted(pixKey, merchantName, merchantCity, amount, dynamic);
    }

    private static boolean amountEquals(BigDecimal a, BigDecimal b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    public static final class Builder {

        private String pixKey;
        private String merchantName;
        private String merchantCity;
        private BigDecimal amount;
        private String txId;
        private String additionalInfo;
        private String postalCode;
        private boolean dynamic;

        private Builder() {}

        public Builder pixKey(String pixKey) {
            this.pixKey = pixKey;
            return this;
        }

        public Builder merchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }

        public Builder merchantCity(String merchantCity) {
            this.merchantCity = merchantCity;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder txId(String txId) {
            this.txId = txId;
            return this;
        }

        public Builder additionalInfo(String additionalInfo) {
            this.additionalInfo = additionalInfo;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder dynamic(boolean dynamic) {
            this.dynamic = dynamic;
            return this;
        }

        public PixPayload build() {
            if (pixKey == null || pixKey.isBlank()) {
                throw new PixPayloadException("pixKey is required");
            }
            if (merchantName == null || merchantName.isBlank()) {
                throw new PixPayloadException("merchantName is required");
            }
            if (merchantCity == null || merchantCity.isBlank()) {
                throw new PixPayloadException("merchantCity is required");
            }
            if (merchantName.length() > MAX_MERCHANT_NAME) {
                throw new PixPayloadException(
                        "merchantName must be at most %d characters, got %d".formatted(MAX_MERCHANT_NAME, merchantName.length())
                );
            }
            if (merchantCity.length() > MAX_MERCHANT_CITY) {
                throw new PixPayloadException(
                        "merchantCity must be at most %d characters, got %d".formatted(MAX_MERCHANT_CITY, merchantCity.length())
                );
            }
            if (txId != null && !TX_ID_PATTERN.matcher(txId).matches()) {
                throw new PixPayloadException(
                        "txId must be alphanumeric and at most 25 characters"
                );
            }
            if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new PixPayloadException("amount must be positive");
            }

            return new PixPayload(pixKey, merchantName, merchantCity, amount, txId, additionalInfo, postalCode, dynamic);
        }
    }
}
