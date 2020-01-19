package gov.ismonnet.cardhelp.serializer;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import gov.ismonnet.cardhelp.Card;

public class JsonCardsSerializerTest {


    @Test
    public void serialize() {
        final String game = "test_game";
        final List<Card> cards = new ArrayList<>();

        cards.add(new Card(Card.Suit.CLUB, 4));
        cards.add(new Card(Card.Suit.HEART, 3));
        cards.add(new Card(Card.Suit.DIAMOND, 12));
        cards.add(new Card(Card.Suit.SPADE,5));

        Assert.assertEquals(
                "{\"game\":\"" + game + "\"," +
                        "\"cards\":[" +
                        "{\"number\":4,\"suit\":\"club\"}," +
                        "{\"number\":3,\"suit\":\"heart\"}," +
                        "{\"number\":12,\"suit\":\"diamond\"}," +
                        "{\"number\":5,\"suit\":\"spade\"}" +
                        "]}",
                new JsonCardsSerializer().serialize(game, cards));
    }
}