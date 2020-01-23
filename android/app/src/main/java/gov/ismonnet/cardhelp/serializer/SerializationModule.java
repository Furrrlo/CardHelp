package gov.ismonnet.cardhelp.serializer;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import gov.ismonnet.cardhelp.core.CardsSerializer;
import gov.ismonnet.cardhelp.core.DetectionDeserializer;
import gov.ismonnet.cardhelp.core.DetectionSerializer;
import gov.ismonnet.cardhelp.core.GamesDeserializer;

@Module
public abstract class SerializationModule {

    @Binds
    @Singleton
    abstract CardsSerializer cardsSerializer(JsonCardsSerializer jsonCardsSerializer);

    @Binds
    @Singleton
    abstract GamesDeserializer gamesDeserializer(JsonGamesDeserializer jsonGamesDeserializer);

    @Binds
    @Singleton
    abstract DetectionSerializer detectionSerializer(JsonDetectionSerializer impl);

    @Binds
    @Singleton
    abstract DetectionDeserializer detectionDeserializer(JsonDetectionDeserializer impl);
}
