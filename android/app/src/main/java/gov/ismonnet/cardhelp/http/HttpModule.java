package gov.ismonnet.cardhelp.http;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import gov.ismonnet.cardhelp.core.GamesService;
import gov.ismonnet.cardhelp.core.ScoreService;
import gov.ismonnet.cardhelp.serializer.SerializationModule;

@Module(includes = SerializationModule.class)
public abstract class HttpModule {

    @Binds
    @Singleton
    abstract GamesService gamesService(HttpGamesService httpGamesService);

    @Binds
    @Singleton
    abstract ScoreService scoreService(HttpScoreService httpScoreService);
}
