package rw.ur.connecttopark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateParkingSlotDTO {

    @NotBlank(message = "slotCode must not be blank")
    @Size(max = 10, message = "slotCode must be at most 10 characters")
    private String slotCode;
}
