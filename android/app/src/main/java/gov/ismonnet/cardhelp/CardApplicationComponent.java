package gov.ismonnet.cardhelp;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import gov.ismonnet.cardhelp.camera.MainActivityModule;

@Component(modules = { AndroidInjectionModule.class, MainActivityModule.class })
interface CardApplicationComponent {

    void inject(CardApplication cardApplication);
}
