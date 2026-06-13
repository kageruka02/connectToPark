package rw.ur.connecttopark.exception;

public class InvalidStatusException extends RuntimeException {
    public InvalidStatusException(String value) {
        super("Invalid slot status value: '" + value + "'. Accepted values are FREE or OCCUPIED.");
    }
}
