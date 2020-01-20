package gov.ismonnet.cardhelp.detection;

import android.content.Context;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import gov.ismonnet.cardhelp.activity.ActivityLifeCycle;
import gov.ismonnet.cardhelp.activity.ActivityScope;
import gov.ismonnet.cardhelp.core.CardsDetector;

@Module
public abstract class OpenCvModule {

    @Provides
    @ActivityScope
    static OpenCvLoader loader(Context context) {
        return new OpenCvLoader(context);
    }

    @Binds
    @ActivityScope
    abstract CardsDetector cardsDetector(OpenCvCardsDetector openCvCardsDetector);

    @ActivityScope
    @Provides @ElementsIntoSet
    static Set<ActivityLifeCycle> provideActivityLifecycleListener(OpenCvLoader loader,
                                                                   CardsDetector detector) {
        return new LinkedHashSet<>(Arrays.asList(loader, (OpenCvCardsDetector) detector));
    }
}
