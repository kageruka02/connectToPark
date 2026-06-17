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
import rw.ur.connecttopark.repository.ParkingEventRepository;
import rw.ur.connecttopark.repository.ParkingLotRepository;
import rw.ur.connecttopark.websocket.ParkingBroadcaster;

import org.mockito.ArgumentCaptor;
import rw.ur.connecttopark.dto.ParkingResponseDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ParkingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingLotRepository lotRepository;

    @Autowired
    private ParkingEventRepository eventRepository;

    @MockBean
    private ParkingBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        lotRepository.deleteAll();
    }

    // ── POST /api/parking (create lot) ──────────────────────────────────────────

    @Test
    void createParkingLot_validBody_returns201WithAllFields() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UR Parking\",\"totalCapacity\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("UR Parking"))
                .andExpect(jsonPath("$.totalCapacity").value(10))
                .andExpect(jsonPath("$.availableSlots").value(10))
                .andExpect(jsonPath("$.isFull").value(false))
                .andExpect(jsonPath("$.lastUpdated").isNotEmpty());
    }

    @Test
    void createParkingLot_availableSlotsEqualsCapacityOnCreate() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Lot\",\"totalCapacity\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableSlots").value(5))
                .andExpect(jsonPath("$.isFull").value(false));
    }

    @Test
    void createParkingLot_duplicate_returns409() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"First Lot\",\"totalCapacity\":10}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Second Lot\",\"totalCapacity\":5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void createParkingLot_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalCapacity\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createParkingLot_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"totalCapacity\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createParkingLot_missingCapacity_returns400() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Lot\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.totalCapacity").exists());
    }

    @Test
    void createParkingLot_zeroCapacity_returns400() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Lot\",\"totalCapacity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.totalCapacity").exists());
    }

    // ── GET /api/parking ─────────────────────────────────────────────────────────

    @Test
    void getParkingLot_noLotExists_returns404() throws Exception {
        mockMvc.perform(get("/api/parking"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getParkingLot_afterCreate_returnsLot() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UR Parking\",\"totalCapacity\":10}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/parking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UR Parking"))
                .andExpect(jsonPath("$.totalCapacity").value(10));
    }

    // ── POST /api/parking/status ─────────────────────────────────────────────────

    @Test
    void updateStatus_validBody_returns200WithCorrectFields() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":8,\"totalSlots\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots").value(8))
                .andExpect(jsonPath("$.totalSlots").value(10))
                .andExpect(jsonPath("$.isFull").value(false))
                .andExpect(jsonPath("$.lastUpdated").isNotEmpty());
    }

    @Test
    void updateStatus_parkingFull_returnIsFullTrue() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":0,\"totalSlots\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots").value(0))
                .andExpect(jsonPath("$.isFull").value(true));
    }

    @Test
    void updateStatus_savesEventLog() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":5,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        assertThat(eventRepository.findAll()).hasSize(1);
        assertThat(eventRepository.findAll().get(0).getAvailableSlots()).isEqualTo(5);
    }

    @Test
    void updateStatus_multipleUpdates_logsEachOne() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":8,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":7,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        assertThat(eventRepository.findAll()).hasSize(2);
    }

    @Test
    void updateStatus_missingAvailableSlots_returns400() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalSlots\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.availableSlots").exists());
    }

    @Test
    void updateStatus_missingTotalSlots_returns400() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.totalSlots").exists());
    }

    @Test
    void updateStatus_negativeAvailableSlots_returns400() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":-1,\"totalSlots\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.availableSlots").exists());
    }

    @Test
    void updateStatus_zeroTotalSlots_returns400() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":0,\"totalSlots\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.totalSlots").exists());
    }

    @Test
    void updateStatus_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/parking/status ──────────────────────────────────────────────────

    @Test
    void getStatus_noDataYet_returnsZerosAndIsFull() throws Exception {
        mockMvc.perform(get("/api/parking/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots").value(0))
                .andExpect(jsonPath("$.totalSlots").value(0))
                .andExpect(jsonPath("$.isFull").value(true));
    }

    @Test
    void getStatus_afterUpdate_returnsLatestValues() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":3,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/parking/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots").value(3))
                .andExpect(jsonPath("$.totalSlots").value(10))
                .andExpect(jsonPath("$.isFull").value(false));
    }

    @Test
    void getStatus_multipleUpdates_returnsLastValue() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":8,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":2,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/parking/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSlots").value(2));
    }

    // ── WebSocket broadcast verification ────────────────────────────────────────

    @Test
    void updateStatus_broadcastsCorrectPayload() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":7,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        ArgumentCaptor<ParkingResponseDTO> captor = ArgumentCaptor.forClass(ParkingResponseDTO.class);
        verify(broadcaster, times(1)).broadcast(captor.capture());

        ParkingResponseDTO broadcasted = captor.getValue();
        assertThat(broadcasted.getAvailableSlots()).isEqualTo(7);
        assertThat(broadcasted.getTotalSlots()).isEqualTo(10);
        assertThat(broadcasted.isFull()).isFalse();
    }

    @Test
    void updateStatus_fullParking_broadcastsIsFullTrue() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":0,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        ArgumentCaptor<ParkingResponseDTO> captor = ArgumentCaptor.forClass(ParkingResponseDTO.class);
        verify(broadcaster).broadcast(captor.capture());

        assertThat(captor.getValue().isFull()).isTrue();
        assertThat(captor.getValue().getAvailableSlots()).isZero();
    }

    @Test
    void updateStatus_calledTwice_broadcastsTwice() throws Exception {
        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":8,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/parking/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"availableSlots\":6,\"totalSlots\":10}"))
                .andExpect(status().isOk());

        verify(broadcaster, times(2)).broadcast(any(ParkingResponseDTO.class));
    }

    @Test
    void createParkingLot_doesNotTriggerBroadcast() throws Exception {
        mockMvc.perform(post("/api/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UR Parking\",\"totalCapacity\":10}"))
                .andExpect(status().isCreated());

        verify(broadcaster, never()).broadcast(any());
    }
}
