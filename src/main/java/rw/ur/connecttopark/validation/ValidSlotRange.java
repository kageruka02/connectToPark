package rw.ur.connecttopark.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SlotRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSlotRange {
    String message() default "availableSlots cannot be greater than totalSlots";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
