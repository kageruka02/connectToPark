package rw.ur.connecttopark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ur.connecttopark.dto.CreateParkingLotDTO;
import rw.ur.connecttopark.dto.ParkingLotDTO;
import rw.ur.connecttopark.dto.ParkingResponseDTO;
import rw.ur.connecttopark.dto.ParkingStatusDTO;
import rw.ur.connecttopark.entity.ParkingEvent;
import rw.ur.connecttopark.entity.ParkingLot;
import rw.ur.connecttopark.exception.ParkingLotAlreadyExistsException;
import rw.ur.connecttopark.repository.ParkingEventRepository;
import rw.ur.connecttopark.repository.ParkingLotRepository;
import rw.ur.connecttopark.websocket.ParkingBroadcaster;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingLotRepository lotRepository;
    private final ParkingEventRepository eventRepository;
    private final ParkingBroadcaster broadcaster;

    @Transactional
    public ParkingLotDTO createParkingLot(CreateParkingLotDTO dto) {
        if (!lotRepository.findAll().isEmpty()) {
            throw new ParkingLotAlreadyExistsException();
        }
        ParkingLot lot = lotRepository.save(new ParkingLot(dto.getName(), dto.getTotalCapacity()));
        return toLotDTO(lot);
    }

    @Transactional(readOnly = true)
    public ParkingLotDTO getParkingLot() {
        List<ParkingLot> lots = lotRepository.findAll();
        if (lots.isEmpty()) {
            return null;
        }
        return toLotDTO(lots.get(0));
    }

    @Transactional
    public ParkingResponseDTO updateStatus(ParkingStatusDTO dto) {
        List<ParkingLot> lots = lotRepository.findAll();

        ParkingLot lot;
        if (lots.isEmpty()) {
            lot = new ParkingLot("Default", dto.getTotalSlots());
            lot.setAvailableSlots(dto.getAvailableSlots());
        } else {
            lot = lots.get(0);
            lot.setTotalCapacity(dto.getTotalSlots());
            lot.setAvailableSlots(dto.getAvailableSlots());
            lot.setLastUpdated(LocalDateTime.now());
        }

        lotRepository.save(lot);
        eventRepository.save(new ParkingEvent(dto.getAvailableSlots(), dto.getTotalSlots()));

        ParkingResponseDTO response = toStatusResponse(lot);
        broadcaster.broadcast(response);
        return response;
    }

    @Transactional(readOnly = true)
    public ParkingResponseDTO getStatus() {
        List<ParkingLot> lots = lotRepository.findAll();
        if (lots.isEmpty()) {
            return new ParkingResponseDTO(0, 0, true, null);
        }
        return toStatusResponse(lots.get(0));
    }

    private ParkingLotDTO toLotDTO(ParkingLot lot) {
        return new ParkingLotDTO(
                lot.getId(),
                lot.getName(),
                lot.getTotalCapacity(),
                lot.getAvailableSlots(),
                lot.getAvailableSlots() == 0,
                lot.getLastUpdated()
        );
    }

    private ParkingResponseDTO toStatusResponse(ParkingLot lot) {
        return new ParkingResponseDTO(
                lot.getAvailableSlots(),
                lot.getTotalCapacity(),
                lot.getAvailableSlots() == 0,
                lot.getLastUpdated()
        );
    }
}
