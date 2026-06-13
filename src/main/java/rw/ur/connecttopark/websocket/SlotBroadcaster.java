package rw.ur.connecttopark.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import rw.ur.connecttopark.dto.SlotResponseDTO;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SlotBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Pushes the full current slot list to all subscribed WebSocket clients.
     * Called after every successful status update so the frontend stays in sync.
     */
    public void broadcastSlots(List<SlotResponseDTO> slots) {
        messagingTemplate.convertAndSend("/topic/slots", slots);
    }
}
