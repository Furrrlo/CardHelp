package gov.ismonnet.cardhelp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import dagger.android.AndroidInjection;
import gov.ismonnet.cardhelp.BuildConfig;
import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.core.CardsDetector;
import gov.ismonnet.cardhelp.core.GamesService;
import gov.ismonnet.cardhelp.core.ScoreService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private static final String CURR_PHOTO_FILE_KEY = "CURR_PHOTO";
    private static final String SELECTED_GAME_KEY = "SELECTED_GAME";
    private static final String SCORE_KEY = "SCORE";

    @Inject Set<ActivityLifeCycle> lifecycleListeners;

    @Inject CardsDetector cardsDetector;
    @Inject GamesService gamesService;
    @Inject ScoreService scoreService;

    private ViewFlipper viewFlipper;

    private Uri currPhotoUri;
    private String selectedGame;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        viewFlipper = findViewById(R.id.viewFlipper);

        System.out.println(lifecycleListeners);
        lifecycleListeners.forEach(ActivityLifeCycle::onCreate);

        // Actual init

        currPhotoUri = null;
        selectedGame = null;
        score = -1;

        if(savedInstanceState != null) {
            currPhotoUri = Uri.parse(savedInstanceState.getString(CURR_PHOTO_FILE_KEY));
            selectedGame = savedInstanceState.getString(SELECTED_GAME_KEY);
            score = savedInstanceState.getInt(SCORE_KEY);
//            currPhotoUri = FileProvider.getUriForFile(this,
//                    BuildConfig.APPLICATION_ID + ".fileprovider",
//                    new File(
//                            getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//                            "curr.jpg"));

            try {
                parsePicture(currPhotoUri);

                if(selectedGame == null || score == -1)
                    askGameInput();

            } catch (UncheckedIOException ex) {
                currPhotoUri = null;
                takePicture();
                throw ex;
            }

        } else {
            takePicture();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(CURR_PHOTO_FILE_KEY, currPhotoUri.toString());
        outState.putString(SELECTED_GAME_KEY, selectedGame);
        outState.putInt(SCORE_KEY, score);

        super.onSaveInstanceState(outState);
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null)
            throw new RuntimeException("Couldn't locate any camera activity");

        final File photoFile = createImageFile();
        // TODO: as always, deleteOnExit doesn't really do anything
        //       so find a way to actually delete saved files once they are no longer needed
        photoFile.deleteOnExit();

        currPhotoUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currPhotoUri);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            parsePicture(currPhotoUri);
            askGameInput();
        }
    }

    private void parsePicture(Uri photoUri) {
        final Bitmap photo = processPicture(loadBitmap(photoUri));
        // TODO: not really needed now but I wanted in the future
        //       to be able to look at all the processing steps
        final ImageView imageView = (ImageView) View.inflate(this, R.layout.image_view, null);
        imageView.setImageBitmap(photo);
        viewFlipper.addView(imageView);
    }

    private void askGameInput() {
//        // TODO: game menu popup -> using gamesService
//        selectedGame = "";
//        score = scoreService.calculatePoints(selectedGame, cards);
//        // TODO: show result
    }

    // Lifecycle

    @Override
    protected void onStart() {
        super.onStart();
        lifecycleListeners.forEach(ActivityLifeCycle::onStart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleListeners.forEach(ActivityLifeCycle::onResume);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lifecycleListeners.forEach(ActivityLifeCycle::onPause);
    }

    @Override
    protected void onStop() {
        super.onStop();
        lifecycleListeners.forEach(ActivityLifeCycle::onStop);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        lifecycleListeners.forEach(ActivityLifeCycle::onRestart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleListeners.forEach(ActivityLifeCycle::onDestroy);
    }

    // Utilities

    private Bitmap loadBitmap(Uri photoUri) {
        try {
            final Bitmap orig = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
            final float angle = getExifAngle(photoUri);
            if(angle == -1f)
                return orig;
            return rotateImage(orig, angle);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        final Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source,
                0, 0,
                source.getWidth(), source.getHeight(),
                matrix, true);
    }


    public float getExifAngle(Uri uri) throws IOException {
        ExifInterface exifInterface = getExifInterface(uri);
        if(exifInterface == null)
            return -1f;

        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90f;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180f;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270f;
            case ExifInterface.ORIENTATION_NORMAL:
                return 0f;
            case ExifInterface.ORIENTATION_UNDEFINED:
            default:
                return -1f;
        }
    }

    public ExifInterface getExifInterface(Uri uri) throws IOException {
        String path = uri.toString();
        if (path.startsWith("file://"))
            return new ExifInterface(path);
        if (path.startsWith("content://"))
            return new ExifInterface(getContentResolver().openInputStream(uri));
        return null;
    }

    private Bitmap processPicture(Bitmap photo) {
        final List<Card> cards = new ArrayList<>();
        final Bitmap out = cardsDetector.detectCards(photo, cards);

        return out;
    }

}
