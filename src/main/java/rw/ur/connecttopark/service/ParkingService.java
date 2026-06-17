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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingLotRepository lotRepository;
    private final ParkingEventRepository eventRepository;

    @Transactional
    public ParkingLotDTO createParkingLot(CreateParkingLotDTO dto) {
        if (lotRepository.findFirstByOrderByIdAsc().isPresent()) {
            throw new ParkingLotAlreadyExistsException();
        }
        ParkingLot lot = lotRepository.save(new ParkingLot(dto.getName(), dto.getTotalCapacity()));
        return toLotDTO(lot);
    }

    @Transactional(readOnly = true)
    public ParkingLotDTO getParkingLot() {
        return lotRepository.findFirstByOrderByIdAsc()
                .map(this::toLotDTO)
                .orElse(null);
    }

    @Transactional
    public ParkingResponseDTO updateStatus(ParkingStatusDTO dto) {
        ParkingLot lot = lotRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> new ParkingLot("Default", dto.getTotalSlots()));

        lot.setTotalCapacity(dto.getTotalSlots());
        lot.setAvailableSlots(dto.getAvailableSlots());
        lot.setLastUpdated(LocalDateTime.now());

        lotRepository.save(lot);
        eventRepository.save(new ParkingEvent(dto.getAvailableSlots(), dto.getTotalSlots()));

        return toStatusResponse(lot);
    }

    @Transactional(readOnly = true)
    public ParkingResponseDTO getStatus() {
        return lotRepository.findFirstByOrderByIdAsc()
                .map(this::toStatusResponse)
                .orElse(new ParkingResponseDTO(0, 0, true, null));
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
