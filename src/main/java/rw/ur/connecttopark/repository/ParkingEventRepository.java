package rw.ur.connecttopark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.ur.connecttopark.entity.ParkingEvent;

import java.util.List;

public interface ParkingEventRepository extends JpaRepository<ParkingEvent, Long> {

    List<ParkingEvent> findAllByOrderByTimestampDesc();
}
