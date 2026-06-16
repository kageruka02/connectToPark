package rw.ur.connecttopark.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateParkingLotDTO {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "totalCapacity is required")
    @Min(value = 1, message = "totalCapacity must be at least 1")
    private Integer totalCapacity;
}
