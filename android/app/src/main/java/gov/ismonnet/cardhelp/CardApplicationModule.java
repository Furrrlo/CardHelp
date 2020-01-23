package gov.ismonnet.cardhelp;

import android.content.Context;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import gov.ismonnet.cardhelp.activity.ComputingActivity;
import gov.ismonnet.cardhelp.activity.DetectionActivity;
import gov.ismonnet.cardhelp.activity.DetectionFragment;
import gov.ismonnet.cardhelp.activity.HistoryActivity;
import gov.ismonnet.cardhelp.detection.DetectionModule;
import gov.ismonnet.cardhelp.detection.Metadata;

@Module(includes = { DetectionModule.class })
abstract class CardApplicationModule {

    @Binds
    @Singleton
    abstract Context context(CardApplication application);

    @Provides
    @Metadata
    static File metadataDirectory(Context context) {
        return new File(context.getExternalFilesDir(null), "detected");
    }

    @Provides
    static ExecutorService executor() {
        return Executors.newFixedThreadPool(10);
    }

    @ContributesAndroidInjector
    abstract HistoryActivity contributeHistoryActivity();

    @ContributesAndroidInjector
    abstract ComputingActivity contributeComputingActivity();

    @ContributesAndroidInjector
    abstract DetectionActivity contributeDetectionActivity();

    @ContributesAndroidInjector
    abstract DetectionFragment contributeDetectionFragment();
}
