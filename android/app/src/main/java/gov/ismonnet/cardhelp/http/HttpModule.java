package gov.ismonnet.cardhelp.http;

import dagger.Binds;
import dagger.Module;
import gov.ismonnet.cardhelp.activity.ActivityScope;
import gov.ismonnet.cardhelp.core.GamesService;
import gov.ismonnet.cardhelp.core.ScoreService;
import gov.ismonnet.cardhelp.serializer.JsonSerializationModule;

@Module(includes = JsonSerializationModule.class)
public abstract class HttpModule {

    @Binds
    @ActivityScope
    abstract GamesService gamesService(HttpGamesService httpGamesService);

    @Binds
    @ActivityScope
    abstract ScoreService scoreService(HttpScoreService httpScoreService);
}
