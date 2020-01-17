package gov.ismonnet.cardhelp.serializer;

import java.util.Collection;
import java.util.Collections;

public class JsonGamesDeserializer implements GamesDeserializer {
    @Override
    public Collection<String> deserialize(String toDeserialize) {
        // org.json.JSONArray
        return Collections.emptyList();
    }
}
