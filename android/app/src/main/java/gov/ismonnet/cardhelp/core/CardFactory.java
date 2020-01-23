package gov.ismonnet.cardhelp.core;

public interface CardFactory {

    Card makeCard(Card.Suit suit, int number);
}
