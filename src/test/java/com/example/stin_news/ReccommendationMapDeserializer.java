package com.example.stin_news;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import services.RecommendationMapDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationMapDeserializerTest {

    private RecommendationMapDeserializer deserializer;
    private ObjectMapper objectMapper;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        deserializer = new RecommendationMapDeserializer();
        objectMapper = new ObjectMapper();
        // Již se nepokoušíme mockovat statický logger
    }

    private JsonParser createParserFromString(String json) throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(json);
        parser.nextToken(); // Move to the first token
        return parser;
    }

    @Test
    void deserialize_validRecommendation() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"AAPL\", \"sell\": 1 }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNotNull(recommendation);
        assertEquals("AAPL", recommendation.get("name"));
        assertEquals(1, recommendation.get("sell"));
    }

    @Test
    void deserialize_missingName() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"sell\": 0 }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(recommendation);
    }

    @Test
    void deserialize_emptyName() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"\", \"sell\": 1 }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(recommendation);
    }

    @Test
    void deserialize_nullName() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": null, \"sell\": 0 }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(recommendation);
    }

    @Test
    void deserialize_nullNameInPayload() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": null, \"date\": 1746723926000, \"rating\": 2, \"sell\": 1 }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(recommendation);
        // Volitelně můžete ověřit logování, pokud na tom trváte, ale je to méně spolehlivé.
        // verify(logger).warn(ArgumentMatchers.startsWith("Chyba při parsování 'name'"), ArgumentMatchers.any(JsonNode.class));
    }

    @Test
    void deserialize_missingSell() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"GOOGL\" }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(recommendation);
    }

    @Test
    void deserialize_invalidSell_notIntegerOrString() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"MSFT\", \"sell\": true }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(recommendation);
    }

    @Test
    void deserialize_invalidSell_wrongIntegerValue() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"AMZN\", \"sell\": 2 }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(recommendation);
    }

    @Test
    void deserialize_invalidSell_wrongStringValue() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"TSLA\", \"sell\": \"abc\" }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(recommendation);
    }

    @Test
    void deserialize_validSell_asStringZero() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"NVDA\", \"sell\": \"0\" }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNotNull(recommendation);
        assertEquals("NVDA", recommendation.get("name"));
        assertEquals(0, recommendation.get("sell"));
    }

    @Test
    void deserialize_validSell_asStringOne() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"BABA\", \"sell\": \"1\" }");
        Map<String, Object> recommendation = deserializer.deserialize(spiedParser, deserializationContext);
        assertNotNull(recommendation);
        assertEquals("BABA", recommendation.get("name"));
        assertEquals(1, recommendation.get("sell"));
    }
}