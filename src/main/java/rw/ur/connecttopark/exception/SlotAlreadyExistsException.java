package rw.ur.connecttopark.exception;

public class SlotAlreadyExistsException extends RuntimeException {

    public SlotAlreadyExistsException(String slotCode) {
        super("Parking slot already exists: " + slotCode);
    }
}
