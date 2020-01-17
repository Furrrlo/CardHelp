package gov.ismonnet.cardhelp.detection;

import android.media.Image;

import java.util.Collection;

import gov.ismonnet.cardhelp.Card;

public interface CardsDetector {

    Collection<Card> detectCards(Image image);
}
