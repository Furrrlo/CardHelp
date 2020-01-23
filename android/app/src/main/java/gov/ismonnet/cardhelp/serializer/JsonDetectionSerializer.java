package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.Card;
import gov.ismonnet.cardhelp.core.Detection;
import gov.ismonnet.cardhelp.core.DetectionSerializer;
import gov.ismonnet.cardhelp.core.SerializationException;

public class JsonDetectionSerializer implements DetectionSerializer {

    @Inject JsonDetectionSerializer() {}

    @Override
    public String serialize(Detection detection) throws SerializationException {
        try {
            final JSONObject obj = new JSONObject();

            obj.put("thumbnail", detection.getThumbnailPath().getAbsolutePath());
            obj.put("input", detection.getInputPath().getAbsolutePath());
            obj.put("output", detection.getOutputPath().getAbsolutePath());

            final JSONArray arr = new JSONArray();
            for(Card card : detection.getCards()) {
                final JSONObject cardObj = new JSONObject();
                cardObj.put("suit", card.getSuit().name());
                cardObj.put("number", card.getNumber());

                arr.put(cardObj);
            }
            obj.put("cards", arr);

            obj.put("game", detection.getGame());
            obj.put("score", detection.getScore());
            obj.put("timestamp", detection.getTimestamp().toString());

            return obj.toString();

        } catch (JSONException e) {
            throw new SerializationException(e);
        }
    }
}
