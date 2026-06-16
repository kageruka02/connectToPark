package rw.ur.connecttopark.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_events")
@Getter
@Setter
@NoArgsConstructor
public class ParkingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int availableSlots;

    @Column(nullable = false)
    private int totalSlots;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ParkingEvent(int availableSlots, int totalSlots) {
        this.availableSlots = availableSlots;
        this.totalSlots = totalSlots;
        this.timestamp = LocalDateTime.now();
    }
}
