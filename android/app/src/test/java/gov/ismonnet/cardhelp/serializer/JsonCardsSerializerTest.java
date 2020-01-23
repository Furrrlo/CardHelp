package gov.ismonnet.cardhelp.serializer;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import gov.ismonnet.cardhelp.core.Card;
import gov.ismonnet.cardhelp.detection.CardImpl;

public class JsonCardsSerializerTest {
    
    @Test
    public void serialize() {
        final String game = "test_game";
        final List<Card> cards = new ArrayList<>();

        cards.add(new CardImpl(CardImpl.Suit.CLUB, 4));
        cards.add(new CardImpl(CardImpl.Suit.HEART, 3));
        cards.add(new CardImpl(CardImpl.Suit.DIAMOND, 12));
        cards.add(new CardImpl(CardImpl.Suit.SPADE,5));

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