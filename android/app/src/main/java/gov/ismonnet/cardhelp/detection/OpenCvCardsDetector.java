package gov.ismonnet.cardhelp.detection;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.core.CardsDetector;

class OpenCvCardsDetector implements CardsDetector {

    @Inject OpenCvCardsDetector() {}

    @Override
    public Collection<Card> detectCards(Bitmap image) {

        final Collection<Card> cards = new ArrayList<>();

        final Mat input = new Mat();
        Utils.bitmapToMat(image, input);

        return cards;
    }
}
