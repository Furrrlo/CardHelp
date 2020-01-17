package gov.ismonnet.cardhelp.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.GamesDeserializer;
import gov.ismonnet.cardhelp.core.GamesService;

class HttpGamesService implements GamesService {

    private final GamesDeserializer gamesDeserializer;

    @Inject HttpGamesService(GamesDeserializer gamesDeserializer) {
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
