package gov.ismonnet.cardhelp.activity;

import android.content.Context;

import dagger.Binds;
import dagger.Module;
import gov.ismonnet.cardhelp.detection.OpenCvModule;
import gov.ismonnet.cardhelp.http.HttpModule;

@Module(includes = { OpenCvModule.class, HttpModule.class })
abstract class MainActivityModule {

    @Binds
    @ActivityScope
    abstract Context provideContext(MainActivity activity);
}
