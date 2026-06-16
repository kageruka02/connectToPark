package rw.ur.connecttopark.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.ur.connecttopark.dto.CreateParkingLotDTO;
import rw.ur.connecttopark.dto.ParkingLotDTO;
import rw.ur.connecttopark.dto.ParkingResponseDTO;
import rw.ur.connecttopark.dto.ParkingStatusDTO;
import rw.ur.connecttopark.service.ParkingService;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping
    public ResponseEntity<ParkingLotDTO> createParkingLot(@Valid @RequestBody CreateParkingLotDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingService.createParkingLot(dto));
    }

    @GetMapping
    public ResponseEntity<ParkingLotDTO> getParkingLot() {
        ParkingLotDTO lot = parkingService.getParkingLot();
        if (lot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lot);
    }

    @PostMapping("/status")
    public ResponseEntity<ParkingResponseDTO> updateStatus(@Valid @RequestBody ParkingStatusDTO dto) {
        return ResponseEntity.ok(parkingService.updateStatus(dto));
    }

    @GetMapping("/status")
    public ResponseEntity<ParkingResponseDTO> getStatus() {
        return ResponseEntity.ok(parkingService.getStatus());
    }
}
