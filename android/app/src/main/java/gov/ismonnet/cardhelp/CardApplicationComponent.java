package gov.ismonnet.cardhelp;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import gov.ismonnet.cardhelp.camera.CameraActivityModule;

@Component(modules = { AndroidInjectionModule.class, CameraActivityModule.class })
interface CardApplicationComponent {

    void inject(CardApplication cardApplication);
}
