package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.core.CardsSerializer;

class JsonCardsSerializer implements CardsSerializer {

    @Inject JsonCardsSerializer() {}

    @Override
    public String serialize(String game, Collection<Card> cards) {
        JSONObject jRes = new JSONObject();
        JSONObject jTemp = new JSONObject();

        JSONArray jCards = new JSONArray();

        try {
            //put game in JSON result
            jRes.put("game", game);

            cards.forEach(card -> {
                try {
                    //create new card object
                    jTemp.put("number", card.getNumber());
                    jTemp.put("suit", card.getSuit().name().toLowerCase());

                    //put card to array
                    jCards.put(new JSONObject(jTemp.toString()));
                    //remove existing card object

                    jTemp.remove("suit");
                    jTemp.remove("number");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } );

            //put vector into result
            jRes.put("cards", jCards);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return jRes.toString();
    }
}
