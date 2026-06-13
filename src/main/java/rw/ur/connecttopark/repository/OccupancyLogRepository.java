package rw.ur.connecttopark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.ur.connecttopark.entity.OccupancyLog;

import java.util.List;

@Repository
public interface OccupancyLogRepository extends JpaRepository<OccupancyLog, Long> {
    List<OccupancyLog> findBySlotCodeOrderByTimestampDesc(String slotCode);

    void deleteBySlotCode(String slotCode);
}
