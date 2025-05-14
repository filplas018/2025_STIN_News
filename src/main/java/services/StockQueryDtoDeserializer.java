package services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import dtos.StockQueryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StockQueryDtoDeserializer extends JsonDeserializer<StockQueryDto> {

    private static final Logger logger = LoggerFactory.getLogger(StockQueryDtoDeserializer.class);

    @Override
    public StockQueryDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        StockQueryDto dto = new StockQueryDto();
        boolean hasErrors = false;

        // Parsování name
        JsonNode nameNode = node.get("name");
        if (nameNode != null) {
            if (nameNode.isTextual()) {
                String name = nameNode.asText();
                if (!name.isEmpty()) {
                    dto.setName(name);
                } else {
                    logger.warn("Pole 'name' je prázdné, nastavuji na null.");
                    dto.setName(null);
                    hasErrors = true;
                }
            } else {
                logger.warn("Pole 'name' má nesprávný typ (očekáván String), nastavuji na null.");
                dto.setName(null);
                hasErrors = true;
            }
        } else {
            logger.warn("Pole 'name' chybí, nastavuji na null.");
            dto.setName(null);
            hasErrors = true;
        }

        // Parsování date
        JsonNode dateNode = node.get("date");
        if (dateNode != null) {
            if (dateNode.isNumber()) {
                try {
                    dto.setDate(dateNode.asLong());
                } catch (Exception e) {
                    logger.warn("Chyba při převodu 'date' na long ({}), nastavuji na 0.", dateNode.asText());
                    dto.setDate(0);
                    hasErrors = true;
                }
            } else if (dateNode.isTextual()) {
                try {
                    dto.setDate(Long.parseLong(dateNode.asText()));
                } catch (NumberFormatException e) {
                    logger.warn("Pole 'date' má nesprávný formát ({}), očekáváno číslo (long), nastavuji na 0.", dateNode.asText());
                    dto.setDate(0);
                    hasErrors = true;
                }
            } else {
                logger.warn("Pole 'date' má nesprávný typ (očekáváno číslo nebo text), nastavuji na 0.");
                dto.setDate(0);
                hasErrors = true;
            }
        } else {
            logger.warn("Pole 'date' chybí, nastavuji na 0.");
            dto.setDate(0);
            hasErrors = true;
        }

        // Parsování rating
        JsonNode ratingNode = node.get("rating");
        dto.setRating(ratingNode != null && ratingNode.isInt() ? ratingNode.asInt() : 0);

        return hasErrors ? null : dto;
    }
}