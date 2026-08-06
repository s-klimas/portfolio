package pl.sebastianklimas.portfolio.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import pl.sebastianklimas.portfolio.exceptions.SystemMessageException;
import reactor.core.publisher.Flux;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        var system = loadSystemPrompt();

        var questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(6)
                        .similarityThreshold(0.75d)
                        .build())
                .build();

        this.chatClient = builder
                .defaultSystem(system)
                .defaultAdvisors(questionAnswerAdvisor)
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

    private String loadSystemPrompt() {
        var resource = new ClassPathResource("prompts/system.txt");

        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            throw new SystemMessageException(e.getMessage());
        }
    }
}
