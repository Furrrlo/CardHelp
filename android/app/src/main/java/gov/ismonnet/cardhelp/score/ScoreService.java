package gov.ismonnet.cardhelp.score;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;

import gov.ismonnet.cardhelp.Card;

public interface ScoreService {

    int calculatePoints(String game, Collection<Card> cards);
}
