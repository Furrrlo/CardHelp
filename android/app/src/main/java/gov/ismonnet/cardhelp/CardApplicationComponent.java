package gov.ismonnet.cardhelp;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import gov.ismonnet.cardhelp.detection.DetectionModule;

@Singleton
@Component(modules = { AndroidInjectionModule.class,
        CardApplicationModule.class,
        DetectionModule.class })
interface CardApplicationComponent {

    void inject(CardApplication cardApplication);

    @Component.Factory
    interface Factory {
        CardApplicationComponent make(@BindsInstance CardApplication application);
    }
}
