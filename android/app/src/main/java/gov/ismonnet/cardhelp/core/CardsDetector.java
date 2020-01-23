package gov.ismonnet.cardhelp.core;

import android.graphics.Bitmap;

import java.util.Collection;

public interface CardsDetector {

    Bitmap detectCards(Bitmap input, Collection<Card> cards);
}
