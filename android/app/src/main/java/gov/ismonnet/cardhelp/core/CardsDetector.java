package gov.ismonnet.cardhelp.core;

import android.media.Image;

import java.util.Collection;

import gov.ismonnet.cardhelp.Card;

public interface CardsDetector {

    Collection<Card> detectCards(Image image);
}
