package gov.ismonnet.cardhelp.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.Card;
import gov.ismonnet.cardhelp.core.CardsSerializer;
import gov.ismonnet.cardhelp.core.ScoreService;

class HttpScoreService implements ScoreService {

    private final CardsSerializer cardsSerializer;

    @Inject HttpScoreService(CardsSerializer cardsSerializer) {
        this.cardsSerializer = cardsSerializer;
    }

    @Override
    public int calculatePoints(String game, Collection<Card> cards) {

        String serialized = cardsSerializer.serialize(game, cards);

        try {
            // HttpURLConnection post
            URL url = new URL("http://cardhelp.altervista.org/score.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStream out = connection.getOutputStream();
            out.write(serialized.getBytes(StandardCharsets.UTF_8));
            out.close();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = rd.readLine()) != null)
                response.append(line);
            rd.close();

            try {
                return Integer.parseInt(response.toString());
            } catch(NumberFormatException e) {
                return -1;
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

    }
}
