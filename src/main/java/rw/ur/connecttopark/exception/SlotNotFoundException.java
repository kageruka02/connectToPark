package rw.ur.connecttopark.exception;

public class SlotNotFoundException extends RuntimeException {
    public SlotNotFoundException(String slotCode) {
        super("Parking slot not found: " + slotCode);
    }
}
