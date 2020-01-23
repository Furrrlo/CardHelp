package gov.ismonnet.cardhelp.core;

import java.util.Collection;

public interface ScoreService {

    int calculatePoints(String game, Collection<Card> cards);
}
