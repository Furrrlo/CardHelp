package gov.ismonnet.cardhelp.http;

import dagger.Binds;
import dagger.Module;
import gov.ismonnet.cardhelp.core.GamesService;
import gov.ismonnet.cardhelp.core.ScoreService;
import gov.ismonnet.cardhelp.serializer.JsonSerializationModule;

@Module(includes = JsonSerializationModule.class)
public abstract class HttpModule {

    @Binds
    abstract GamesService gamesService(HttpGamesService httpGamesService);

    @Binds
    abstract ScoreService scoreService(HttpScoreService httpScoreService);
}
