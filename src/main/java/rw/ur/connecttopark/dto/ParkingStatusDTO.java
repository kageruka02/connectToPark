package rw.ur.connecttopark.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.ur.connecttopark.validation.ValidSlotRange;

@Getter
@Setter
@NoArgsConstructor
@ValidSlotRange
public class ParkingStatusDTO {

    @NotNull(message = "availableSlots is required")
    @Min(value = 0, message = "availableSlots cannot be negative")
    private Integer availableSlots;

    @NotNull(message = "totalSlots is required")
    @Min(value = 1, message = "totalSlots must be at least 1")
    private Integer totalSlots;
}
