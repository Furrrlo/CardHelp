package gov.ismonnet.cardhelp.camera;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import gov.ismonnet.cardhelp.detection.OpenCvModule;
import gov.ismonnet.cardhelp.http.HttpModule;

@Module(includes = { OpenCvModule.class, HttpModule.class })
public abstract class CameraActivityModule {

    @ContributesAndroidInjector
    abstract CameraActivity contributeCameraActivity();
}
