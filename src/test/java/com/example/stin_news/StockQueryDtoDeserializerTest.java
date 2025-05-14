package com.example.stin_news;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.StockQueryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import services.StockQueryDtoDeserializer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockQueryDtoDeserializerTest {

    private StockQueryDtoDeserializer deserializer;
    private ObjectMapper objectMapper;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        deserializer = new StockQueryDtoDeserializer();
        objectMapper = new ObjectMapper();
        // Již se nepokoušíme mockovat statický logger pro ověření warn zpráv
    }

    private JsonParser createParserFromString(String json) throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(json);
        parser.nextToken(); // Move to the first token
        return parser;
    }

    @Test
    void deserialize_validDto() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"AAPL\", \"date\": 1678886400000, \"rating\": 3 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNotNull(dto);
        assertEquals("AAPL", dto.getName());
        assertEquals(1678886400000L, dto.getDate());
        assertEquals(3, dto.getRating());
    }

    @Test
    void deserialize_missingName() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"date\": 1678886400000, \"rating\": 1 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(dto);
    }

    @Test
    void deserialize_emptyName() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"\", \"date\": 1678886400000, \"rating\": 2 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(dto);
    }

    @Test
    void deserialize_nullName() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": null, \"date\": 1678886400000, \"rating\": 2 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(dto);
    }

    @Test
    void deserialize_invalidNameType() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": 123, \"date\": 1678886400000, \"rating\": 4 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(dto);
    }

    @Test
    void deserialize_missingDate() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"GOOGL\", \"rating\": 5 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(dto);
    }

    @Test
    void deserialize_invalidDateType_notNumberOrText() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"MSFT\", \"date\": true, \"rating\": 0 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(dto);
    }

    @Test
    void deserialize_invalidDateType_textualNotLong() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"AMZN\", \"date\": \"abc\", \"rating\": 3 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNull(dto);
    }

    @Test
    void deserialize_validDateAsString() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"TSLA\", \"date\": \"1678886400000\", \"rating\": 2 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNotNull(dto);
        assertEquals("TSLA", dto.getName());
        assertEquals(1678886400000L, dto.getDate());
        assertEquals(2, dto.getRating());
    }

    @Test
    void deserialize_missingRating() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"NVDA\", \"date\": 1678886400000 }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNotNull(dto);
        assertEquals("NVDA", dto.getName());
        assertEquals(1678886400000L, dto.getDate());
        assertEquals(0, dto.getRating()); // Default value
    }

    @Test
    void deserialize_invalidRatingType() throws IOException {
        JsonParser spiedParser = createParserFromString("{ \"name\": \"BABA\", \"date\": 1678886400000, \"rating\": \"abc\" }");
        StockQueryDto dto = deserializer.deserialize(spiedParser, deserializationContext);
        assertNotNull(dto);
        assertEquals("BABA", dto.getName());
        assertEquals(1678886400000L, dto.getDate());
        assertEquals(0, dto.getRating()); // Default value
    }
}