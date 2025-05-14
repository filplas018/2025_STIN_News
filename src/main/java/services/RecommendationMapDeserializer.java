package services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecommendationMapDeserializer extends JsonDeserializer<Map<String, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationMapDeserializer.class);

    @Override
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        Map<String, Object> recommendation = new HashMap<>();
        boolean hasErrors = false;

        // Parsování name
        JsonNode nameNode = node.get("name");
        if (nameNode != null && nameNode.isTextual() && !nameNode.asText().trim().isEmpty()) {
            recommendation.put("name", nameNode.asText().trim());
        } else {
            logger.warn("Chyba při parsování 'name', doporučení bude ignorováno: {}", node);
            hasErrors = true;
        }

        // Parsování sell
        JsonNode sellNode = node.get("sell");
        if (sellNode != null) {
            Integer sellValue = null;
            if (sellNode.isInt()) {
                sellValue = sellNode.asInt();
            } else if (sellNode.isTextual()) {
                try {
                    sellValue = Integer.parseInt(sellNode.asText());
                } catch (NumberFormatException e) {
                    logger.warn("Chyba při parsování 'sell' ({}), očekáváno 0 nebo 1, doporučení bude ignorováno: {}", sellNode.asText(), node);
                    hasErrors = true;
                }
            }

            if (sellValue != null && (sellValue == 0 || sellValue == 1)) {
                recommendation.put("sell", sellValue);
            } else {
                logger.warn("Chybná hodnota 'sell' ({}), očekáváno 0 nebo 1, doporučení bude ignorováno: {}", sellNode != null ? sellNode.asText() : "null", node);
                hasErrors = true;
            }
        } else {
            logger.warn("Pole 'sell' chybí, doporučení bude ignorováno: {}", node);
            hasErrors = true;
        }

        return hasErrors ? null : recommendation;
    }
}