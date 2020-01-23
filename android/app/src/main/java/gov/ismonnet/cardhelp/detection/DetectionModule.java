package gov.ismonnet.cardhelp.detection;

import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import gov.ismonnet.cardhelp.activity.LifeCycle;
import gov.ismonnet.cardhelp.core.CardFactory;
import gov.ismonnet.cardhelp.core.DetectionFactory;
import gov.ismonnet.cardhelp.core.DetectionRepository;
import gov.ismonnet.cardhelp.detector.OpenCvModule;
import gov.ismonnet.cardhelp.http.HttpModule;
import gov.ismonnet.cardhelp.serializer.SerializationModule;

@Module(includes = { OpenCvModule.class, SerializationModule.class, HttpModule.class })
public abstract class DetectionModule {

    @Binds
    @Singleton
    abstract CardFactory cardFactory(CardImplFactory impl);

    @Binds
    @Singleton
    abstract DetectionFactory detectionFactory(DetectionImplFactory impl);

    @Binds
    @Singleton
    abstract DetectionRepository detectionRepository(DetectionRepositoryImpl impl);

    @Provides
    @ElementsIntoSet
    static Set<LifeCycle> provideAppLifecycleListener(DetectionRepository cardsDetector) {
        return Collections.singleton((DetectionRepositoryImpl) cardsDetector);
    }
}
