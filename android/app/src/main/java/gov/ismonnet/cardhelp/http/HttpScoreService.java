package gov.ismonnet.cardhelp.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.core.CardsSerializer;
import gov.ismonnet.cardhelp.core.ScoreService;

class HttpScoreService implements ScoreService {

    private final CardsSerializer cardsSerializer;

    @Inject HttpScoreService(CardsSerializer cardsSerializer) {
        this.cardsSerializer = cardsSerializer;
    }

    @Override
    public int calculatePoints(String game, Collection<Card> cards) {
        try {
            // HttpURLConnection post
            if(false)
                throw new IOException("temp");

            return -1;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
