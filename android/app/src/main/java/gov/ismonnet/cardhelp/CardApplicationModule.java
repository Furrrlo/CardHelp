package gov.ismonnet.cardhelp;

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import gov.ismonnet.cardhelp.activity.MainActivity;
import gov.ismonnet.cardhelp.activity.MainActivitySubcomponent;

@Module(subcomponents = MainActivitySubcomponent.class)
abstract class CardApplicationModule {

    @Binds
    @IntoMap
    @ClassKey(MainActivity.class)
    abstract AndroidInjector.Factory<?> bindMainActivityFactory(MainActivitySubcomponent.Factory factory);
}
