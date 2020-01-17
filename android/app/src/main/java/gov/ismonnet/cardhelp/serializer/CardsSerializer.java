package gov.ismonnet.cardhelp.serializer;

import java.util.Collection;

import gov.ismonnet.cardhelp.Card;

public interface CardsSerializer {

    String serialize(String game, Collection<Card> cards);
}
