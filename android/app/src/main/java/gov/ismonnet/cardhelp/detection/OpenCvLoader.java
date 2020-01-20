package gov.ismonnet.cardhelp.detection;

import android.content.Context;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.activity.ActivityLifeCycle;

import static android.content.ContentValues.TAG;

public class OpenCvLoader implements ActivityLifeCycle {

    private final Context context;
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    @Inject OpenCvLoader(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        if(!loaded.getAndSet(true))
            load();
    }

    @Override
    public void onResume() {
        if(!loaded.getAndSet(true))
            load();
    }

    @Override
    public void onPause() {
        loaded.set(false);
    }

    @Override
    public void onStop() {
        loaded.set(false);
    }

    @Override
    public void onDestroy() {
        loaded.set(false);
    }

    private void load() {
        final CountDownLatch latch = new CountDownLatch(1);
        final BaseLoaderCallback openCvLoaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                if (status == BaseLoaderCallback.SUCCESS) {
                    latch.countDown();
                    return;
                }

                super.onManagerConnected(status);
            }
        };

        if(!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Couldn't find internal OpenCV library. Attempting to load it using OpenCV Engine service.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,
                    context,
                    openCvLoaderCallback);
        } else {
            openCvLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
