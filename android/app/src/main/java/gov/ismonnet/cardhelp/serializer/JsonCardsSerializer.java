package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.Card;
import gov.ismonnet.cardhelp.core.CardsSerializer;
import gov.ismonnet.cardhelp.core.SerializationException;

class JsonCardsSerializer implements CardsSerializer {

    @Inject JsonCardsSerializer() {}

    @Override
    public String serialize(String game, Collection<Card> cards) throws SerializationException {
        JSONObject jRes = new JSONObject();
        JSONObject jTemp = new JSONObject();

        JSONArray jCards = new JSONArray();

        try {
            //put game in JSON result
            jRes.put("game", game);

            for (Card card : cards) {
                //create new card object
                jTemp.put("number", card.getNumber());
                jTemp.put("suit", card.getSuit().name().toLowerCase());

                //put card to array
                jCards.put(new JSONObject(jTemp.toString()));
                //remove existing card object

                jTemp.remove("suit");
                jTemp.remove("number");
            }

            //put vector into result
            jRes.put("cards", jCards);

        } catch (JSONException e) {
            throw new SerializationException(e);
        }

        return jRes.toString();
    }
}
