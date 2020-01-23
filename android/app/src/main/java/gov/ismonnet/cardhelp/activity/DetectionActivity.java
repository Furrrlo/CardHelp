package gov.ismonnet.cardhelp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.core.Detection;
import gov.ismonnet.cardhelp.core.DetectionRepository;

public class DetectionActivity extends BaseActivity {

    public static final String INPUT_DETECTION_ID = "detection_id";

    @Inject DetectionRepository detectionRepository;

    private ViewFlipper viewFlipper;

    private int detectionId;
    private Detection detection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        // TODO: rotating screen causes crash, needs a model

        // Check extras

        final Bundle extras = getIntent().getExtras();
        if(extras == null)
            throw new UnsupportedOperationException("Missing extras");

        detectionId = extras.getInt(INPUT_DETECTION_ID);
        if(detectionId == 0)
            throw new UnsupportedOperationException("Missing extra INPUT_DETECTION_ID");

        // Layout

        setContentView(R.layout.activity_detection);
        viewFlipper = findViewById(R.id.viewFlipper);

        // Load data

        detection = detectionRepository.getDetection(detectionId);

        // TODO: not really needed now but I wanted in the future
        //       to be able to look at all the processing steps
        final ImageView imageView = (ImageView) View.inflate(this, R.layout.detection_image_view, null);
        imageView.setImageBitmap(detection.getOutput());
        viewFlipper.addView(imageView);

        // TODO: show score and game
    }
}
