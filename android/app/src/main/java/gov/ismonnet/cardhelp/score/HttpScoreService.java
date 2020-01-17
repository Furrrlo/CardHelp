package gov.ismonnet.cardhelp.score;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.serializer.CardsSerializer;

public class HttpScoreService implements ScoreService {

    private final CardsSerializer cardsSerializer;

    public HttpScoreService(CardsSerializer cardsSerializer) {
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
