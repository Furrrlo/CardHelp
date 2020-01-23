package gov.ismonnet.cardhelp.detection;

import com.google.auto.factory.AutoFactory;

import java.util.Objects;

import gov.ismonnet.cardhelp.core.Card;
import gov.ismonnet.cardhelp.core.CardFactory;

@AutoFactory(implementing = CardFactory.class)
public class CardImpl implements Card {

    private final Suit suit;
    private final int number;

    public CardImpl(Suit suit, int number) {
        this.suit = Objects.requireNonNull(suit, "Card suit cannot be null");
        this.number = Objects.requireNonNull(number, "Card number cannot be null");
    }

    public Suit getSuit() {
        return suit;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardImpl)) return false;
        CardImpl card = (CardImpl) o;
        return number == card.number &&
                suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, number);
    }

    @Override
    public String toString() {
        return "CardImpl{" +
                "suit=" + suit +
                ", number=" + number +
                '}';
    }
}
