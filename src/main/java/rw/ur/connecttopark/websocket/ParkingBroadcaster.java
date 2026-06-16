package rw.ur.connecttopark.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import rw.ur.connecttopark.dto.ParkingResponseDTO;

@Component
@RequiredArgsConstructor
public class ParkingBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(ParkingResponseDTO status) {
        messagingTemplate.convertAndSend("/topic/parking", status);
    }
}
