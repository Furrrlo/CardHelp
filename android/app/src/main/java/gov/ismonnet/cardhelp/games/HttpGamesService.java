package gov.ismonnet.cardhelp.games;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.score.ScoreService;
import gov.ismonnet.cardhelp.serializer.GamesDeserializer;

public class HttpGamesService implements GamesService {

    private final GamesDeserializer gamesDeserializer;

    public HttpGamesService(GamesDeserializer gamesDeserializer) {
        this.gamesDeserializer = gamesDeserializer;
    }

    @Override
    public Collection<String> listGames() {
        try {
            // HttpURLConnection get
            if(false)
                throw new IOException("temp");

            return Collections.emptyList();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
