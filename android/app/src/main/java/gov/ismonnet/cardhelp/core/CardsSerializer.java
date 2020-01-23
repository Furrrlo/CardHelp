package gov.ismonnet.cardhelp.core;

import java.util.Collection;

public interface CardsSerializer {

    String serialize(String game, Collection<Card> cards);
}
