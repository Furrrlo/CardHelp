package gov.ismonnet.cardhelp.detection;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.BitmapUtils;
import gov.ismonnet.cardhelp.core.Card;
import gov.ismonnet.cardhelp.core.Detection;
import gov.ismonnet.cardhelp.core.DetectionFactory;

@AutoFactory(implementing = DetectionFactory.class)
class DetectionImpl implements Detection {

    private static final AtomicInteger DETECTION_ID_GENERATOR = new AtomicInteger(1);

    private final Context context;
    private final int id;

    private final File thumbnailPath;
    private final File inputPath;
    private final File outputPath;

    private final String game;
    private final String score;
    private final Collection<Card> cards;
    private final Instant timestamp;

    private Bitmap thumbnail;

    @Inject DetectionImpl(@Provided Context context,
                          File thumbnailPath,
                          File inputPath,
                          File outputPath,
                          Collection<Card> cards,
                          String game,
                          String score,
                          Instant timestamp) {

        this.context = context;
        this.id = DETECTION_ID_GENERATOR.getAndIncrement();
        this.thumbnailPath = thumbnailPath;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.cards = cards;
        this.game = game;
        this.score = score;
        this.timestamp = timestamp;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Bitmap getThumbnail() {
        if(thumbnail == null) {
            try {
                thumbnail = BitmapUtils.loadBitmap(context, thumbnailPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return thumbnail;
    }

    @Override
    public File getThumbnailPath() {
        return thumbnailPath;
    }

    @Override
    public Bitmap getInput() {
        try {
            return BitmapUtils.loadBitmap(context, inputPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public File getInputPath() {
        return inputPath;
    }

    @Override
    public Bitmap getOutput() {
        try {
            return BitmapUtils.loadBitmap(context, outputPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public File getOutputPath() {
        return outputPath;
    }

    @Override
    public Collection<Card> getCards() {
        return cards;
    }

    @Override
    public String getGame() {
        return game;
    }

    @Override
    public String getScore() {
        return score;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetectionImpl)) return false;
        DetectionImpl detection = (DetectionImpl) o;
        return thumbnailPath.equals(detection.thumbnailPath) &&
                inputPath.equals(detection.inputPath) &&
                outputPath.equals(detection.outputPath) &&
                game.equals(detection.game) &&
                score.equals(detection.score) &&
                cards.equals(detection.cards) &&
                timestamp.equals(detection.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(thumbnailPath, inputPath, outputPath, game, score, cards, timestamp);
    }

    @Override
    public String toString() {
        return "DetectionImpl{" +
                "thumbnailPath=" + thumbnailPath +
                ", inputPath=" + inputPath +
                ", outputPath=" + outputPath +
                ", game='" + game + '\'' +
                ", score='" + score + '\'' +
                ", cards=" + cards +
                ", timestamp=" + timestamp +
                '}';
    }
}
