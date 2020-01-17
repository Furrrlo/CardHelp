package gov.ismonnet.cardhelp.core;

import java.util.Collection;

import gov.ismonnet.cardhelp.Card;

public interface ScoreService {

    int calculatePoints(String game, Collection<Card> cards);
}
