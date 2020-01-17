package gov.ismonnet.cardhelp.camera;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.util.Collection;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import dagger.android.AndroidInjection;
import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.core.CardsDetector;
import gov.ismonnet.cardhelp.core.GamesService;
import gov.ismonnet.cardhelp.core.ScoreService;

import static android.content.ContentValues.TAG;

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

        openCvLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                if (status != BaseLoaderCallback.SUCCESS) {
                    processPicture();
                    return;
                }
                super.onManagerConnected(status);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO: move this out of the activity
        //       as it does not belong here
        // Init OpenCv

        if(!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Couldn't find internal OpenCV library. Attempting to load it using OpenCV Engine service.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,
                    this,
                    openCvLoaderCallback);
        } else {
            openCvLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void processPicture() {
        // Do stuff

        final Image pic = takePicture();
        final Collection<Card> cards = cardsDetector.detectCards(pic);

        // TODO: game menu popup -> using gamesService
        final String game = "";

        final int res = scoreService.calculatePoints(game, cards);
        // TODO: show result
    }

    private Image takePicture() {
        return null;
    }
}
