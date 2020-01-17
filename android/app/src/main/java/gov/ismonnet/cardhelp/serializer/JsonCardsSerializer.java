package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;

import gov.ismonnet.cardhelp.Card;

public class JsonCardsSerializer implements CardsSerializer {
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
