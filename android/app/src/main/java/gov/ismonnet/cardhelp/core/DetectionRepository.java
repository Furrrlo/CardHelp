package gov.ismonnet.cardhelp.core;

import android.graphics.Bitmap;

import java.util.List;

public interface DetectionRepository {

    List<Detection> getDetections();

    Detection getDetection(int id);

    Detection.Builder newDetection(Bitmap input);
}
