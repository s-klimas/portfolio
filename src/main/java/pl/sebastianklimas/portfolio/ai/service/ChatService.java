package pl.sebastianklimas.portfolio.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder, ChatMemory chatMemory) {
        var system = "";
        this.chatClient = builder
                .defaultSystem(system)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public Flux<String> getStreamResponse(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    public String getResponse(String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .call()
                .content();
    }
}
