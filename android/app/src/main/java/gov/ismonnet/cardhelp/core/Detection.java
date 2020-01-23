package gov.ismonnet.cardhelp.core;

import android.graphics.Bitmap;

import java.io.File;
import java.time.Instant;
import java.util.Collection;

import javax.annotation.Nullable;

import androidx.lifecycle.LiveData;

public interface Detection {

    int getId();

    Bitmap getThumbnail();

    File getThumbnailPath();

    Bitmap getInput();

    File getInputPath();

    Bitmap getOutput();

    File getOutputPath();

    Collection<Card> getCards();

    String getGame();

    String getScore();

    Instant getTimestamp();

    interface Builder {

        Builder setGame(String game);

        LiveData<DetectionResult> makeDetection();
    }

    interface DetectionResult {

        @Nullable
        Detection getDetection();

        @Nullable
        String getErrorMessage();

        @Nullable
        Throwable getException();
    }
}
