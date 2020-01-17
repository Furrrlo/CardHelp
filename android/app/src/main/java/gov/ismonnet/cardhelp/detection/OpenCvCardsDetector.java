package gov.ismonnet.cardhelp.detection;

import android.media.Image;

import java.util.Collection;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.core.CardsDetector;

class OpenCvCardsDetector implements CardsDetector {

    @Inject OpenCvCardsDetector() {}

    @Override
    public Collection<Card> detectCards(Image image) {
        return null;
    }
}
