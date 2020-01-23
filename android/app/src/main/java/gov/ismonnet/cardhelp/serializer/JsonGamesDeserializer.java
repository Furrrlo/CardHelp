package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.GamesDeserializer;
import gov.ismonnet.cardhelp.core.SerializationException;

class JsonGamesDeserializer implements GamesDeserializer {

    @Inject JsonGamesDeserializer() {}

    @Override
    public List<String> deserialize(String toDeserialize) throws SerializationException {
        List<String> res = new ArrayList<>();

        try {
            JSONArray jArray = new JSONArray(toDeserialize);
            int size = jArray.length();

            for(int i = 0; i < size; ++i)
                res.add(jArray.get(i).toString());

        } catch (JSONException e) {
            throw new SerializationException(e);
        }

        return res;
    }
}
