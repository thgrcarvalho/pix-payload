# pix-payload

[![CI](https://github.com/thgrcarvalho/pix-payload/actions/workflows/ci.yml/badge.svg)](https://github.com/thgrcarvalho/pix-payload/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.thgrcarvalho/pix-payload)](https://central.sonatype.com/artifact/io.github.thgrcarvalho/pix-payload)
[![codecov](https://codecov.io/gh/thgrcarvalho/pix-payload/branch/main/graph/badge.svg)](https://codecov.io/gh/thgrcarvalho/pix-payload)

Zero-dependency Java 21 library for encoding and decoding **Pix Copia e Cola** (copy-and-paste) strings, following the EMV-QRCPS-MPM specification with the Brazilian Pix extensions defined by Banco Central do Brasil.

## Why this exists

Brazil's Pix has no native recurring-payment primitive. Every QR code or copy-and-paste string is a single-transaction EMV payload — and every team building on Pix eventually hand-rolls the same encoding/decoding logic, including the CRC16/CCITT-FALSE checksum. This library makes that a one-liner.

## Installation

**Gradle:**
```groovy
dependencies {
    implementation 'io.github.thgrcarvalho:pix-payload:0.1.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.thgrcarvalho</groupId>
    <artifactId>pix-payload</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

### Encoding

```java
String encoded = PixPayload.newBuilder()
    .pixKey("thiago@example.com")           // e-mail, CPF, CNPJ, phone, or EVP key
    .merchantName("Thiago Carvalho")         // max 25 characters
    .merchantCity("Rio de Janeiro")          // max 15 characters
    .amount(new BigDecimal("149.90"))        // omit to allow any amount
    .txId("SUB20250001")                     // alphanumeric, max 25 chars; omit for open payments
    .additionalInfo("Plano mensal")          // shown to the payer (optional)
    .postalCode("20040020")                  // optional
    .dynamic(true)                           // marks the QR as single-use
    .build()
    .encode();
```

### Decoding

```java
PixPayload payload = PixPayload.parse(encoded);

payload.getPixKey();        // "thiago@example.com"
payload.getMerchantName();  // "Thiago Carvalho"
payload.getAmount();        // BigDecimal("149.90")
payload.getTxId();          // "SUB20250001"
payload.isDynamic();        // true
```

`parse()` validates the CRC before returning. It throws `PixPayloadException` if the checksum is invalid or required fields are missing.

## What's in the payload

| EMV field | Purpose | Required |
|-----------|---------|----------|
| `00` | Payload Format Indicator | always `01` |
| `01` | Point of Initiation Method | `12` if `dynamic=true` |
| `26` | Merchant Account Information | ✓ |
| `26/01` | Pix key (chave Pix) | ✓ |
| `26/02` | Additional info | optional |
| `52` | Merchant Category Code | always `0000` |
| `53` | Transaction Currency | always `986` (BRL) |
| `54` | Transaction Amount | optional |
| `58` | Country Code | always `BR` |
| `59` | Merchant Name | ✓ |
| `60` | Merchant City | ✓ |
| `61` | Postal Code | optional |
| `62/05` | Reference Label (txId) | `***` if absent |
| `63` | CRC16/CCITT-FALSE | computed |

## CRC algorithm

CRC16/CCITT-FALSE: polynomial `0x1021`, initial value `0xFFFF`, no input/output reflection, no final XOR.  
Standard check value for `"123456789"`: `0x29B1`.

The CRC is computed over the entire payload string including the literal `6304` (field ID + length of the CRC field itself), then appended as 4 uppercase hex digits.

## Running tests

```bash
./gradlew test
```

Includes round-trip tests and field-level assertions for all optional fields.

## Non-goals

- **Pix Automático** (the BCB recurring-payment protocol introduced in 2025) — different spec
- **QR code image generation** — use ZXing or similar
- **Pix key validation** — CPF/CNPJ/e-mail format checking is out of scope

## Tech

Java 21 · Gradle · JUnit 5 · zero runtime dependencies
