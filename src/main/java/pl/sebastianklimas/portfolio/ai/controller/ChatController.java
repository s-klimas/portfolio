package pl.sebastianklimas.portfolio.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.sebastianklimas.portfolio.ai.service.ChatService;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    //    @CrossOrigin(origins = "*")
    @PostMapping("/streamedChat")
    public Flux<String> streamedChat(@RequestBody String userMessage) {
        return chatService.getStreamResponse(userMessage);
    }

    //    @CrossOrigin(origins = "*")
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String userMessage) {
        return ResponseEntity.ok(chatService.getResponse(userMessage));
    }
}
