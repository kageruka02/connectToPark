package rw.ur.connecttopark.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_slots")
@Getter
@Setter
@NoArgsConstructor
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String slotCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.FREE;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public ParkingSlot(String slotCode) {
        this.slotCode = slotCode;
        this.status = SlotStatus.FREE;
        this.lastUpdated = LocalDateTime.now();
    }
}
