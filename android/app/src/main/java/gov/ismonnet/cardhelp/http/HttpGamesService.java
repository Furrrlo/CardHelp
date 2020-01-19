package gov.ismonnet.cardhelp.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

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
            URL url = new URL("http://cardhelp.altervista.org/games.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonString = new StringBuilder();

            String line;
            while ((line = rd.readLine()) != null)
                jsonString.append(line);
            rd.close();

            return gamesDeserializer.deserialize(jsonString.toString());

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
