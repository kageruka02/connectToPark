package rw.ur.connecttopark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ParkingResponseDTO {

    private int availableSlots;
    private int totalSlots;

    @JsonProperty("isFull")
    private boolean isFull;

    private LocalDateTime lastUpdated;
}
