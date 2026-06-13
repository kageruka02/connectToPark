package rw.ur.connecttopark.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import rw.ur.connecttopark.entity.SlotStatus;

@Getter
@NoArgsConstructor
public class UpdateSlotStatusDTO {

    @NotBlank(message = "slotCode must not be blank")
    private String slotCode;

    @NotNull(message = "status must not be null")
    private SlotStatus status;
}
