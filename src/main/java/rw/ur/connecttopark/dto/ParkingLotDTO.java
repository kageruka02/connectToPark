package rw.ur.connecttopark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ParkingLotDTO {

    private Long id;
    private String name;
    private int totalCapacity;
    private int availableSlots;

    @JsonProperty("isFull")
    private boolean isFull;

    private LocalDateTime lastUpdated;
}
