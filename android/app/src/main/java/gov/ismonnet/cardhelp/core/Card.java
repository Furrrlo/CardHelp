package gov.ismonnet.cardhelp.core;

public interface Card {

    Suit getSuit();

    int getNumber();

    enum Suit { DIAMOND, HEART, CLUB, SPADE }
}
