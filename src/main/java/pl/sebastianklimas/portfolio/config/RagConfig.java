package pl.sebastianklimas.portfolio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.JsonMetadataGenerator;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.*;

@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    @Value("${vectorstore.path:/app/data/vectorstore.json}")
    private String vectorStorePath;

    private static final String[] CODE_CHUNK_CONTENT_KEYS = {
            "project", "class_name", "method_name", "http_method", "http_path",
            "description", "code_snippet"
    };
    private static final Set<String> CODE_CHUNK_METADATA_EXCLUDE = Set.of("description", "code_snippet");

    private static final Set<String> CODE_CHUNK_FILES = Set.of(
            "recipe-manager-all-chunks.json",
            "coupon-calculator-all-chunks.json",
            "ai-guesser-all-chunks.json"
    );

    private static final String PERSONAL_CHUNKS_FILE = "me-and-links.json";
    private static final String PERSONAL_CHUNKS_POINTER = "/chunks";
    private static final String[] PERSONAL_CHUNK_CONTENT_KEYS = {"title", "content"};

    @Value("classpath:/jsons-for-RAG/*.json")
    private Resource[] ragJsonResources;

    @Bean
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File vectorStoreFile = getVectorStoreFile();

        if (vectorStoreFile.exists()) {
            log.info("Vector store file exists at {}, loading it", vectorStoreFile.getAbsolutePath());
            simpleVectorStore.load(vectorStoreFile);
        } else {
            log.info("Vector store file not found. Building new store at {}", vectorStoreFile.getAbsolutePath());
            List<Document> documents = loadAllRagDocuments();
            simpleVectorStore.add(documents);
            simpleVectorStore.save(vectorStoreFile);
        }

        return simpleVectorStore;
    }

    private List<Document> loadAllRagDocuments() {
        if (ragJsonResources == null || ragJsonResources.length == 0) {
            log.warn("No JSON files found under classpath:/jsons-for-RAG/ - vector store will be empty!");
            return List.of();
        }

        List<Document> allDocuments = new ArrayList<>();

        for (Resource jsonResource : ragJsonResources) {
            String filename = jsonResource.getFilename();
            log.info("Loading RAG source file: {}", filename);

            List<Document> documents = readJsonAsDocuments(jsonResource, filename);
            documents.forEach(doc -> doc.getMetadata().putIfAbsent("filename", filename));

            allDocuments.addAll(documents);

            log.info("Loaded {} chunks from {}", documents.size(), filename);
        }

        log.info("Total documents loaded into vector store: {}", allDocuments.size());
        return allDocuments;
    }

    private List<Document> readJsonAsDocuments(Resource jsonResource, String filename) {
        if (CODE_CHUNK_FILES.contains(filename)) {
            return readCodeChunkDocuments(jsonResource);
        }
        if (PERSONAL_CHUNKS_FILE.equals(filename)) {
            return readPersonalChunkDocuments(jsonResource);
        }

        log.info("File {} not registered as a known schema - using whole JSON node as content", filename);
        return new JsonReader(jsonResource).get();
    }

    private List<Document> readCodeChunkDocuments(Resource jsonResource) {
        JsonMetadataGenerator metadataGenerator = jsonMap -> {
            Map<String, Object> metadata = new LinkedHashMap<>(jsonMap);
            CODE_CHUNK_METADATA_EXCLUDE.forEach(metadata::remove);
            metadata.values().removeIf(Objects::isNull);
            return metadata;
        };

        JsonReader jsonReader = new JsonReader(jsonResource, metadataGenerator, CODE_CHUNK_CONTENT_KEYS);
        return jsonReader.get();
    }

    private List<Document> readPersonalChunkDocuments(Resource jsonResource) {
        JsonMetadataGenerator metadataGenerator = jsonMap -> {
            Map<String, Object> metadata = new LinkedHashMap<>(jsonMap);
            metadata.remove("content");
            metadata.remove("title");
            metadata.values().removeIf(Objects::isNull);
            return metadata;
        };

        JsonReader jsonReader = new JsonReader(jsonResource, metadataGenerator, PERSONAL_CHUNK_CONTENT_KEYS);
        return jsonReader.get(PERSONAL_CHUNKS_POINTER);
    }

    private File getVectorStoreFile() {
        File file = new File(vectorStorePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                log.warn("Could not create directory {}", parentDir.getAbsolutePath());
            }
        }
        return file;
    }
}
