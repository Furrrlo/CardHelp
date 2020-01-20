package gov.ismonnet.cardhelp.core;

import android.graphics.Bitmap;

import java.util.Collection;

import gov.ismonnet.cardhelp.Card;

public interface CardsDetector {

    Bitmap detectCards(Bitmap input, Collection<Card> cards);
}
