package rw.ur.connecttopark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ur.connecttopark.entity.ParkingLot;

import java.util.Optional;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    Optional<ParkingLot> findFirstByOrderByIdAsc();
}
