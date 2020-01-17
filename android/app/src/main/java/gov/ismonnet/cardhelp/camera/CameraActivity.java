package gov.ismonnet.cardhelp.camera;

import android.media.Image;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;

import java.util.Collection;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import dagger.android.AndroidInjection;
import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.core.CardsDetector;
import gov.ismonnet.cardhelp.core.GamesService;
import gov.ismonnet.cardhelp.core.ScoreService;

public class CameraActivity extends AppCompatActivity {

    @Inject CardsDetector cardsDetector;
    @Inject GamesService gamesService;
    @Inject ScoreService scoreService;

    private BaseLoaderCallback openCvLoaderCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
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
