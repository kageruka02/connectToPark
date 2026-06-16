package rw.ur.connecttopark.exception;

public class ParkingLotAlreadyExistsException extends RuntimeException {

    public ParkingLotAlreadyExistsException() {
        super("A parking lot already exists. Only one parking lot is supported.");
    }
}
