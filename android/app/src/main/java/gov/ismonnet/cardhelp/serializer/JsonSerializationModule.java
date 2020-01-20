package gov.ismonnet.cardhelp.serializer;

import dagger.Binds;
import dagger.Module;
import gov.ismonnet.cardhelp.activity.ActivityScope;
import gov.ismonnet.cardhelp.core.CardsSerializer;
import gov.ismonnet.cardhelp.core.GamesDeserializer;

@Module
public abstract class JsonSerializationModule {

    @Binds
    @ActivityScope
    abstract CardsSerializer cardsSerializer(JsonCardsSerializer jsonCardsSerializer);

    @Binds
    @ActivityScope
    abstract GamesDeserializer gamesDeserializer(JsonGamesDeserializer jsonGamesDeserializer);
}
