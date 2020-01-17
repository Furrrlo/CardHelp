package gov.ismonnet.cardhelp.core;

import java.util.Collection;

import gov.ismonnet.cardhelp.Card;

public interface CardsSerializer {

    String serialize(String game, Collection<Card> cards);
}
