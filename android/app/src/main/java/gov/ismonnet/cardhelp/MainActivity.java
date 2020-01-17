package gov.ismonnet.cardhelp;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;

import java.util.Collection;

import gov.ismonnet.cardhelp.detection.CardsDetector;
import gov.ismonnet.cardhelp.detection.OpenCvCardsDetector;
import gov.ismonnet.cardhelp.games.GamesService;
import gov.ismonnet.cardhelp.games.HttpGamesService;
import gov.ismonnet.cardhelp.score.HttpScoreService;
import gov.ismonnet.cardhelp.score.ScoreService;
import gov.ismonnet.cardhelp.serializer.JsonCardsSerializer;
import gov.ismonnet.cardhelp.serializer.JsonGamesDeserializer;

public class MainActivity extends AppCompatActivity {

    private CardsDetector cardsDetector;
    private GamesService gamesService;
    private ScoreService scoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        cardsDetector = new OpenCvCardsDetector();
        gamesService = new HttpGamesService(new JsonGamesDeserializer());
        scoreService = new HttpScoreService(new JsonCardsSerializer());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Init OpenCv

        // Do stuff

        final Image pic = takePicture();
        final Collection<Card> cards = cardsDetector.detectCards(pic);

        // TODO: game menu popup -> using gamesService
        final String game = "";

        final int res = scoreService.calculatePoints(game, cards);
        // TODO: show result
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private Image takePicture() {
        return null;
    }
}
