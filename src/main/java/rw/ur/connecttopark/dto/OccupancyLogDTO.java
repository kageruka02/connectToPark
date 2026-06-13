package rw.ur.connecttopark.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import rw.ur.connecttopark.entity.SlotStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class OccupancyLogDTO {
    private Long id;
    private String slotCode;
    private SlotStatus status;
    private LocalDateTime timestamp;
}
