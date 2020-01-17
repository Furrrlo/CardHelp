package gov.ismonnet.cardhelp;

import android.app.Application;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class CardApplication extends Application implements HasAndroidInjector {

    @Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerCardApplicationComponent
                .create()
                .inject(this);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }
}
