package gov.ismonnet.cardhelp.serializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.core.Card;
import gov.ismonnet.cardhelp.core.CardFactory;
import gov.ismonnet.cardhelp.core.Detection;
import gov.ismonnet.cardhelp.core.DetectionDeserializer;
import gov.ismonnet.cardhelp.core.DetectionFactory;
import gov.ismonnet.cardhelp.core.SerializationException;

public class JsonDetectionDeserializer implements DetectionDeserializer {

    private final DetectionFactory detectionFactory;
    private final CardFactory cardFactory;

    @Inject JsonDetectionDeserializer(DetectionFactory detectionFactory,
                                      CardFactory cardFactory) {
        this.detectionFactory = detectionFactory;
        this.cardFactory = cardFactory;
    }

    @Override
    public Detection deserialize(String string) throws SerializationException {
        try {
            final JSONObject obj = new JSONObject(string);

            final File thumbnailPath = new File(obj.getString("thumbnail"));
            if(!thumbnailPath.exists() || !thumbnailPath.isFile())
                throw new FileNotFoundException("Thumbnail file " + thumbnailPath + " does not exist or is a directory");

            final File inputPath = new File(obj.getString("input"));
            if(!inputPath.exists() || !inputPath.isFile())
                throw new FileNotFoundException("Input file " + inputPath + " does not exist or  is a directory");

            final File outputPath = new File(obj.getString("output"));
            if(!outputPath.exists() || !outputPath.isFile())
                throw new FileNotFoundException("Output file " + outputPath + " does not exist or is a directory");

            final JSONArray cards = obj.getJSONArray("cards");
            final List<Card> parsedCards = new ArrayList<>();

            for(int i = 0; i < cards.length(); i++) {
                final JSONObject card = cards.getJSONObject(i);
                parsedCards.add(cardFactory.makeCard(
                        Card.Suit.valueOf(card.getString("suit")),
                        card.getInt("number")));
            }

            return detectionFactory.makeDetection(thumbnailPath,
                    inputPath,
                    outputPath,
                    parsedCards,
                    obj.getString("game"),
                    obj.getString("score"),
                    Instant.parse(obj.getString("timestamp")));

        } catch (JSONException | FileNotFoundException ex) {
            throw new SerializationException(ex);
        }
    }
}
