package rw.ur.connecttopark.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import rw.ur.connecttopark.dto.ParkingStatusDTO;

public class SlotRangeValidator implements ConstraintValidator<ValidSlotRange, ParkingStatusDTO> {

    @Override
    public boolean isValid(ParkingStatusDTO dto, ConstraintValidatorContext context) {
        if (dto.getAvailableSlots() == null || dto.getTotalSlots() == null) {
            return true; // @NotNull handles null fields
        }
        if (dto.getAvailableSlots() > dto.getTotalSlots()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("availableSlots cannot be greater than totalSlots")
                    .addPropertyNode("availableSlots")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
