package rw.ur.connecttopark.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import rw.ur.connecttopark.entity.OccupancyLog;
import rw.ur.connecttopark.entity.ParkingSlot;
import rw.ur.connecttopark.entity.SlotStatus;
import rw.ur.connecttopark.repository.OccupancyLogRepository;
import rw.ur.connecttopark.repository.ParkingSlotRepository;
import rw.ur.connecttopark.websocket.SlotBroadcaster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SlotControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingSlotRepository slotRepository;

    @Autowired
    private OccupancyLogRepository logRepository;

    @MockBean
    private SlotBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        slotRepository.deleteAll();
    }

    // ── POST /api/slots ──────────────────────────────────────────────────────────

    @Test
    void createSlot_validCode_returns201WithFreeStatus() throws Exception {
        mockMvc.perform(post("/api/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"A1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotCode").value("A1"))
                .andExpect(jsonPath("$.status").value("FREE"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.lastUpdated").isNotEmpty());
    }

    @Test
    void createSlot_duplicateCode_returns409() throws Exception {
        slotRepository.save(new ParkingSlot("B1"));

        mockMvc.perform(post("/api/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"B1\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Parking slot already exists: B1"));
    }

    @Test
    void createSlot_blankCode_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.slotCode").exists());
    }

    @Test
    void createSlot_codeTooLong_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"TOOLONGCODE1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.slotCode").exists());
    }

    // ── POST /api/slots/occupancy ────────────────────────────────────────────────

    @Test
    void createOccupancyLog_existingSlot_returns201WithLog() throws Exception {
        slotRepository.save(new ParkingSlot("C1"));

        mockMvc.perform(post("/api/slots/occupancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"C1\",\"status\":\"OCCUPIED\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotCode").value("C1"))
                .andExpect(jsonPath("$.status").value("OCCUPIED"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        assertThat(logRepository.findBySlotCodeOrderByTimestampDesc("C1")).hasSize(1);
    }

    @Test
    void createOccupancyLog_updatesSlotCurrentStatus() throws Exception {
        slotRepository.save(new ParkingSlot("C2"));

        mockMvc.perform(post("/api/slots/occupancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"C2\",\"status\":\"OCCUPIED\"}"))
                .andExpect(status().isCreated());

        ParkingSlot updated = slotRepository.findBySlotCode("C2").orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(SlotStatus.OCCUPIED);
    }

    @Test
    void createOccupancyLog_slotNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/slots/occupancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"MISSING\",\"status\":\"OCCUPIED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Parking slot not found: MISSING"));
    }

    @Test
    void createOccupancyLog_missingStatus_returns400WithFieldError() throws Exception {
        slotRepository.save(new ParkingSlot("C3"));

        mockMvc.perform(post("/api/slots/occupancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"C3\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
    }

    @Test
    void createOccupancyLog_invalidStatusValue_returns400() throws Exception {
        mockMvc.perform(post("/api/slots/occupancy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"C4\",\"status\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/slots/{slotCode} ────────────────────────────────────────────────

    @Test
    void getSlot_existingCode_returns200WithSlot() throws Exception {
        slotRepository.save(new ParkingSlot("D1"));

        mockMvc.perform(get("/api/slots/D1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotCode").value("D1"))
                .andExpect(jsonPath("$.status").value("FREE"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getSlot_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/slots/GHOST"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Parking slot not found: GHOST"));
    }

    // ── DELETE /api/slots/{slotCode} ─────────────────────────────────────────────

    @Test
    void deleteSlot_existingCode_returns204AndRemovesSlot() throws Exception {
        slotRepository.save(new ParkingSlot("E1"));

        mockMvc.perform(delete("/api/slots/E1"))
                .andExpect(status().isNoContent());

        assertThat(slotRepository.findBySlotCode("E1")).isEmpty();
    }

    @Test
    void deleteSlot_alsoRemovesOccupancyLogs() throws Exception {
        slotRepository.save(new ParkingSlot("E2"));
        logRepository.save(new OccupancyLog("E2", SlotStatus.OCCUPIED));
        logRepository.save(new OccupancyLog("E2", SlotStatus.FREE));

        mockMvc.perform(delete("/api/slots/E2"))
                .andExpect(status().isNoContent());

        assertThat(logRepository.findBySlotCodeOrderByTimestampDesc("E2")).isEmpty();
    }

    @Test
    void deleteSlot_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/slots/GHOST"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Parking slot not found: GHOST"));
    }

    @Test
    void deleteSlot_subsequentGet_returns404() throws Exception {
        slotRepository.save(new ParkingSlot("E3"));

        mockMvc.perform(delete("/api/slots/E3")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/slots/E3")).andExpect(status().isNotFound());
    }

    // ── PUT /api/slots/status ────────────────────────────────────────────────────

    @Test
    void updateStatus_changedStatus_returns200AndCreatesLog() throws Exception {
        slotRepository.save(new ParkingSlot("F1")); // starts FREE

        mockMvc.perform(put("/api/slots/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"F1\",\"status\":\"OCCUPIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotCode").value("F1"))
                .andExpect(jsonPath("$.status").value("OCCUPIED"));

        assertThat(logRepository.findBySlotCodeOrderByTimestampDesc("F1")).hasSize(1);
    }

    @Test
    void updateStatus_sameStatus_returns200WithoutNewLog() throws Exception {
        slotRepository.save(new ParkingSlot("F2")); // starts FREE

        mockMvc.perform(put("/api/slots/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"F2\",\"status\":\"FREE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FREE"));

        assertThat(logRepository.findBySlotCodeOrderByTimestampDesc("F2")).isEmpty();
    }

    @Test
    void updateStatus_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/slots/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"GHOST\",\"status\":\"OCCUPIED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Parking slot not found: GHOST"));
    }

    @Test
    void updateStatus_missingSlotCode_returns400WithFieldError() throws Exception {
        mockMvc.perform(put("/api/slots/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OCCUPIED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.slotCode").exists());
    }

    @Test
    void updateStatus_missingStatus_returns400WithFieldError() throws Exception {
        mockMvc.perform(put("/api/slots/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotCode\":\"F3\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists());
    }
}
