package gov.ismonnet.cardhelp;

import android.app.Application;

import java.util.Set;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import gov.ismonnet.cardhelp.activity.LifeCycle;

public class CardApplication extends Application implements HasAndroidInjector {

    @Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;
    @Inject Set<LifeCycle> lifeCycleListeners;

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerCardApplicationComponent.factory()
                .make(this)
                .inject(this);

        lifeCycleListeners.forEach(LifeCycle::onCreate);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }
}
