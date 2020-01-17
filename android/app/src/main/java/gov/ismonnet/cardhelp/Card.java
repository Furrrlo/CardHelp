package gov.ismonnet.cardhelp;

import java.util.Objects;

public class Card {

    private final Suit suit;
    private final int number;

    public Card(Suit suit, int number) {
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
        if (!(o instanceof Card)) return false;
        Card card = (Card) o;
        return number == card.number &&
                suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, number);
    }

    @Override
    public String toString() {
        return "Card{" +
                "suit=" + suit +
                ", number=" + number +
                '}';
    }

    public enum Suit { DIAMOND, HEART, CLUB, SPADE }
}
