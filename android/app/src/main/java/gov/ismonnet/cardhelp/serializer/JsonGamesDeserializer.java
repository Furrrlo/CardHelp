package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.GamesDeserializer;

class JsonGamesDeserializer implements GamesDeserializer {

    @Inject JsonGamesDeserializer() {}

    @Override
    public Collection<String> deserialize(String toDeserialize) {
        Collection<String> res = new Vector<>();

        try {
            JSONArray jArray = new JSONArray(toDeserialize);
            int size = jArray.length();

            for(int i = 0;i < size; ++i)
                res.add(jArray.get(i).toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }
}
