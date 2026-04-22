package io.github.thgrcarvalho.pix;

public class PixPayloadException extends RuntimeException {

    public PixPayloadException(String message) {
        super(message);
    }

    public PixPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
