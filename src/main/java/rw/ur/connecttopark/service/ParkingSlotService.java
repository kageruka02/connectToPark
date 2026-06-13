package rw.ur.connecttopark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ur.connecttopark.dto.CreateParkingSlotDTO;
import rw.ur.connecttopark.dto.UpdateSlotStatusDTO;
import rw.ur.connecttopark.dto.OccupancyLogDTO;
import rw.ur.connecttopark.dto.SlotResponseDTO;
import rw.ur.connecttopark.dto.SlotUpdateDTO;
import rw.ur.connecttopark.entity.OccupancyLog;
import rw.ur.connecttopark.entity.ParkingSlot;
import rw.ur.connecttopark.exception.SlotAlreadyExistsException;
import rw.ur.connecttopark.exception.SlotNotFoundException;
import rw.ur.connecttopark.repository.OccupancyLogRepository;
import rw.ur.connecttopark.repository.ParkingSlotRepository;
import rw.ur.connecttopark.websocket.SlotBroadcaster;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSlotService {

    private final ParkingSlotRepository slotRepository;
    private final OccupancyLogRepository logRepository;
    private final SlotBroadcaster broadcaster;

    /**
     * Processes a status update from a microcontroller sensor.
     * Only writes an OccupancyLog entry when the status actually changes to
     * prevent duplicate rows from repeated sensor readings with the same value.
     * After persisting, broadcasts the refreshed slot list over WebSocket.
     */
    @Transactional
    public SlotResponseDTO updateSlotStatus(SlotUpdateDTO dto) {
        ParkingSlot slot = slotRepository.findBySlotCode(dto.getSlotCode())
                .orElseGet(() -> new ParkingSlot(dto.getSlotCode()));

        boolean statusChanged = slot.getStatus() != dto.getStatus();

        if (statusChanged) {
            slot.setStatus(dto.getStatus());
            slot.setLastUpdated(LocalDateTime.now());
            slotRepository.save(slot);

            logRepository.save(new OccupancyLog(slot.getSlotCode(), slot.getStatus()));
        }

        // Always broadcast so the frontend receives a heartbeat even on no-ops
        broadcaster.broadcastSlots(getAllSlots());

        return toSlotResponse(slot);
    }

    /**
     * Returns the current status of every parking slot in the lot.
     */
    @Transactional(readOnly = true)
    public List<SlotResponseDTO> getAllSlots() {
        return slotRepository.findAll()
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    /**
     * Returns the full occupancy history for a given slot, newest events first.
     * Throws SlotNotFoundException if the slot code has never been registered.
     */
    @Transactional(readOnly = true)
    public List<OccupancyLogDTO> getSlotHistory(String slotCode) {
        // Validate the slot exists before querying logs
        slotRepository.findBySlotCode(slotCode)
                .orElseThrow(() -> new SlotNotFoundException(slotCode));

        return logRepository.findBySlotCodeOrderByTimestampDesc(slotCode)
                .stream()
                .map(log -> new OccupancyLogDTO(
                        log.getId(),
                        log.getSlotCode(),
                        log.getStatus(),
                        log.getTimestamp()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SlotResponseDTO getSlotByCode(String slotCode) {
        ParkingSlot slot = slotRepository.findBySlotCode(slotCode)
                .orElseThrow(() -> new SlotNotFoundException(slotCode));
        return toSlotResponse(slot);
    }

    @Transactional
    public SlotResponseDTO updateStatus(UpdateSlotStatusDTO dto) {
        ParkingSlot slot = slotRepository.findBySlotCode(dto.getSlotCode())
                .orElseThrow(() -> new SlotNotFoundException(dto.getSlotCode()));

        if (slot.getStatus() != dto.getStatus()) {
            slot.setStatus(dto.getStatus());
            slot.setLastUpdated(LocalDateTime.now());
            slotRepository.save(slot);
            logRepository.save(new OccupancyLog(slot.getSlotCode(), slot.getStatus()));
        }

        broadcaster.broadcastSlots(getAllSlots());
        return toSlotResponse(slot);
    }

    @Transactional
    public void deleteSlot(String slotCode) {
        ParkingSlot slot = slotRepository.findBySlotCode(slotCode)
                .orElseThrow(() -> new SlotNotFoundException(slotCode));
        logRepository.deleteBySlotCode(slotCode);
        slotRepository.delete(slot);
        broadcaster.broadcastSlots(getAllSlots());
    }

    @Transactional
    public SlotResponseDTO createParkingSlot(CreateParkingSlotDTO dto) {
        if (slotRepository.findBySlotCode(dto.getSlotCode()).isPresent()) {
            throw new SlotAlreadyExistsException(dto.getSlotCode());
        }
        ParkingSlot slot = slotRepository.save(new ParkingSlot(dto.getSlotCode()));
        broadcaster.broadcastSlots(getAllSlots());
        return toSlotResponse(slot);
    }

    @Transactional
    public OccupancyLogDTO createOccupancyLog(SlotUpdateDTO dto) {
        ParkingSlot slot = slotRepository.findBySlotCode(dto.getSlotCode())
                .orElseThrow(() -> new SlotNotFoundException(dto.getSlotCode()));

        slot.setStatus(dto.getStatus());
        slot.setLastUpdated(LocalDateTime.now());
        slotRepository.save(slot);

        OccupancyLog log = logRepository.save(new OccupancyLog(slot.getSlotCode(), slot.getStatus()));
        broadcaster.broadcastSlots(getAllSlots());

        return new OccupancyLogDTO(log.getId(), log.getSlotCode(), log.getStatus(), log.getTimestamp());
    }

    private SlotResponseDTO toSlotResponse(ParkingSlot slot) {
        return new SlotResponseDTO(
                slot.getId(),
                slot.getSlotCode(),
                slot.getStatus(),
                slot.getLastUpdated());
    }
}
