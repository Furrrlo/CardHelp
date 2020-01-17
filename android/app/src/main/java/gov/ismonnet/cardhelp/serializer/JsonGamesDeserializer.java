package gov.ismonnet.cardhelp.serializer;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.GamesDeserializer;

class JsonGamesDeserializer implements GamesDeserializer {

    @Inject JsonGamesDeserializer() {}

    @Override
    public Collection<String> deserialize(String toDeserialize) {
        // org.json.JSONArray
        return Collections.emptyList();
    }
}
