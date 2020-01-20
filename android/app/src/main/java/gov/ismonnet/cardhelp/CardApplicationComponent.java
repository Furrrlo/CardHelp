package gov.ismonnet.cardhelp;

import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Component(modules = { AndroidInjectionModule.class, CardApplicationModule.class })
interface CardApplicationComponent {

    void inject(CardApplication cardApplication);
}
