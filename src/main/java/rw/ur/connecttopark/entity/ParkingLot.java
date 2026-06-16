package rw.ur.connecttopark.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "parking_lot")
@Getter
@Setter
@NoArgsConstructor
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int totalCapacity;

    @Column(nullable = false)
    private int availableSlots;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    public ParkingLot(String name, int totalCapacity) {
        this.name = name;
        this.totalCapacity = totalCapacity;
        this.availableSlots = totalCapacity;
        this.lastUpdated = LocalDateTime.now();
    }
}
