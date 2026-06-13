package rw.ur.connecttopark.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.ur.connecttopark.dto.CreateParkingSlotDTO;
import rw.ur.connecttopark.dto.UpdateSlotStatusDTO;
import rw.ur.connecttopark.dto.OccupancyLogDTO;
import rw.ur.connecttopark.dto.SlotResponseDTO;
import rw.ur.connecttopark.dto.SlotUpdateDTO;
import rw.ur.connecttopark.service.ParkingSlotService;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SlotController {

    private final ParkingSlotService slotService;

    @PostMapping
    public ResponseEntity<SlotResponseDTO> createSlot(@Valid @RequestBody CreateParkingSlotDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.createParkingSlot(dto));
    }

    @PostMapping("/occupancy")
    public ResponseEntity<OccupancyLogDTO> createOccupancyLog(@Valid @RequestBody SlotUpdateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slotService.createOccupancyLog(dto));
    }

    /**
     * Receives an occupancy update from a microcontroller/IoT sensor.
     * Updates the slot's status, conditionally logs the change, and
     * broadcasts the full slot list via WebSocket.
     */
    @PostMapping("/status")
    public ResponseEntity<SlotResponseDTO> updateStatus(@Valid @RequestBody SlotUpdateDTO dto) {
        SlotResponseDTO updated = slotService.updateSlotStatus(dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Returns the current occupancy status of every parking slot.
     */
    @GetMapping
    public ResponseEntity<List<SlotResponseDTO>> getAllSlots() {
        return ResponseEntity.ok(slotService.getAllSlots());
    }

    @GetMapping("/{slotCode}")
    public ResponseEntity<SlotResponseDTO> getSlot(@PathVariable String slotCode) {
        return ResponseEntity.ok(slotService.getSlotByCode(slotCode));
    }

    @PutMapping("/status")
    public ResponseEntity<SlotResponseDTO> updateStatus(@Valid @RequestBody UpdateSlotStatusDTO dto) {
        return ResponseEntity.ok(slotService.updateStatus(dto));
    }

    @DeleteMapping("/{slotCode}")
    public ResponseEntity<Void> deleteSlot(@PathVariable String slotCode) {
        slotService.deleteSlot(slotCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the occupancy history (newest first) for a specific slot.
     */
    @GetMapping("/{slotCode}/history")
    public ResponseEntity<List<OccupancyLogDTO>> getHistory(@PathVariable String slotCode) {
        return ResponseEntity.ok(slotService.getSlotHistory(slotCode));
    }
}
