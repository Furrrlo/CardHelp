package gov.ismonnet.cardhelp.serializer;

import java.util.Collection;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.core.CardsSerializer;

class JsonCardsSerializer implements CardsSerializer {

    @Inject JsonCardsSerializer() {}

    @Override
    public String serialize(String game, Collection<Card> cards) {

        /*
        org.json.JSONObject
        org.json.JSONArray
        '{
            "game": "scala",
            "cards": [
                {
                    "suit": "picche",
                    "number": 1
                },
                {
                    "suit": "fiori",
                    "number": 2
                },
                {
                    "suit": "cuori",
                    "number": 3
                },
                {
                    "suit": "quadri",
                    "number": 3
                },
                {
                    "suit": "fiori",
                    "number": 4
                },
                {
                    "suit": "quadri",
                    "number": 7
                },
                {
                    "suit": "picche",
                    "number": 10
                },
                {
                    "suit": "cuori",
                    "number": 12
                },
                {
                    "suit": "fiori",
                    "number": 12
                },
                {
                    "suit": "quadri",
                    "number": 13
                }
            ]
        }'
        */

        return null;
    }
}
