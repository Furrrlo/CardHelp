package gov.ismonnet.cardhelp.detection;

import dagger.Binds;
import dagger.Module;
import gov.ismonnet.cardhelp.core.CardsDetector;

@Module
public abstract class OpenCvModule {

    @Binds
    abstract CardsDetector cardsDetector(OpenCvCardsDetector openCvCardsDetector);
}
