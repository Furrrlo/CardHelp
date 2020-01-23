package gov.ismonnet.cardhelp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import dagger.android.AndroidInjection;
import gov.ismonnet.cardhelp.BuildConfig;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.core.Detection;

public class HistoryActivity
        extends BaseActivity
        implements DetectionFragment.OnListFragmentInteractionListener {

    private static final String TAG = HistoryActivity.class.getSimpleName();
    private static final int REQUEST_TAKE_PHOTO = 1;

    private File currPhotoFile;
    private Uri currPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.no_camera)
                        .setCancelable(true)
                        .setNegativeButton(R.string.cancel_btn, (dialog, id) -> dialog.cancel())
                        .create()
                        .show();
                return;
            }

            if(currPhotoFile != null && !currPhotoFile.delete())
                Log.w(TAG, "Couldn't delete old photo " + currPhotoFile);

            currPhotoFile = createImageFile();
            // TODO: as always, deleteOnExit doesn't really do anything
            //       so find a way to actually delete saved files once they are no longer needed
            currPhotoFile.deleteOnExit();

            currPhotoUri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    currPhotoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currPhotoUri);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        });
    }

    @Override
    public void onListFragmentInteraction(Detection item) {
        final Intent detectionIntent = new Intent(this, DetectionActivity.class);
        detectionIntent.putExtra(DetectionActivity.INPUT_DETECTION_ID, item.getId());
        startActivity(detectionIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            final Intent newDetectionIntent = new Intent(this, ComputingActivity.class);
            newDetectionIntent.putExtra(ComputingActivity.INPUT_BITMAP_URI, currPhotoUri);

            currPhotoFile = null;
            currPhotoUri = null;

            startActivity(newDetectionIntent);
        }
    }

    private File createImageFile() {
        try {
            final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            return File.createTempFile(
                    "JPEG_" + timeStamp + "_",
                    ".jpg",
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
