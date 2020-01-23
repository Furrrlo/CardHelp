package gov.ismonnet.cardhelp.detector;

import android.content.Context;

import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;
import gov.ismonnet.cardhelp.activity.Activity;
import gov.ismonnet.cardhelp.activity.LifeCycle;
import gov.ismonnet.cardhelp.core.CardsDetector;

@Module
public abstract class OpenCvModule {

    @Provides
    @Singleton
    static OpenCvLoader loader(Context context) {
        return new OpenCvLoader(context);
    }

    @Binds
    @Singleton
    abstract CardsDetector cardsDetector(OpenCvCardsDetector openCvCardsDetector);

    @Provides @ElementsIntoSet
    static Set<LifeCycle> provideAppLifecycleListener(CardsDetector cardsDetector) {
        return Collections.singleton((OpenCvCardsDetector) cardsDetector);
    }

    @Binds @IntoSet @Activity
    abstract LifeCycle provideActivityLifecycleListener(OpenCvLoader loader);
}
