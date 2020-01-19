package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Vector;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.core.CardsSerializer;

class JsonCardsSerializer implements CardsSerializer {

    @Inject JsonCardsSerializer() {}

    @Override
    public String serialize(String game, Collection<Card> cards) {
        //TODO: When the application can detect cards, remove initialization
        cards = new Vector<>();

        cards.add(new Card(Card.Suit.CLUB,4));
        cards.add(new Card(Card.Suit.HEART,3));
        cards.add(new Card(Card.Suit.DIAMOND,12));
        cards.add(new Card(Card.Suit.SPADE,5));

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
                    jTemp.put("suit", card.getSuit().toString());

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
