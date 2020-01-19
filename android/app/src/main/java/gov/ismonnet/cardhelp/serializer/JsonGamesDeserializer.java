package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.GamesDeserializer;

class JsonGamesDeserializer implements GamesDeserializer {

    @Inject JsonGamesDeserializer() {}

    @Override
    public Collection<String> deserialize(String toDeserialize) {
        Collection<String> res = new ArrayList<>();

        try {
            JSONArray jArray = new JSONArray(toDeserialize);
            int size = jArray.length();

            for(int i = 0; i < size; ++i)
                res.add(jArray.get(i).toString());

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return res;
    }
}
