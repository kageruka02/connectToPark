package rw.ur.connecttopark.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "occupancy_logs")
@Getter
@Setter
@NoArgsConstructor
public class OccupancyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String slotCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public OccupancyLog(String slotCode, SlotStatus status) {
        this.slotCode = slotCode;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
