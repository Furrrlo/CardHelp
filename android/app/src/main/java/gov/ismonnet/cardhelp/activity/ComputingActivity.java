package gov.ismonnet.cardhelp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import dagger.android.AndroidInjection;
import gov.ismonnet.cardhelp.BitmapUtils;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.core.Detection;
import gov.ismonnet.cardhelp.core.DetectionRepository;
import gov.ismonnet.cardhelp.core.GamesService;

public class ComputingActivity extends BaseActivity {

    private static final String TAG = ComputingActivity.class.getSimpleName();
    public static final String INPUT_BITMAP_URI = "input";

    @Inject DetectionRepository detectionRepository;
    @Inject ExecutorService executor;
    @Inject GamesService gamesService;

    private Detection.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        // TODO: rotating screen causes crash, needs a model

        // Check extras

        final Bundle extras = getIntent().getExtras();
        if(extras == null)
            throw new UnsupportedOperationException("Missing extras");

        final Uri inputUri = (Uri) getIntent().getExtras().get(INPUT_BITMAP_URI);
        if(inputUri == null)
            throw new UnsupportedOperationException("Missing extra INPUT_BITMAP_URI");

        // Layout

        setContentView(R.layout.activity_computing);

        // Load data

        final Bitmap input;
        try {
            input = BitmapUtils.rotateImage(
                    MediaStore.Images.Media.getBitmap(getContentResolver(), inputUri),
                    BitmapUtils.getExifAngle(this, inputUri));
        } catch (IOException e) {
            new ErrorDialogFragment("Couldn't load input bitmap", e)
                    .show(getSupportFragmentManager(), "ErrorDialogFragment");
            return;
        } finally {
            final File inputFile = new File(inputUri.getPath());
            if(inputFile.exists() && !inputFile.delete())
                Log.w(TAG, "Couldn't delete input file " + inputFile);
        }

        builder = detectionRepository.newDetection(input);

        executor.submit(() -> {
            try {
                final List<String> temp = gamesService.listGames();
                // Capitalized
                final List<String> games = new ArrayList<>();
                games.add(getResources().getString(R.string.no_game));
                for(String s : temp)
                    games.add(s.substring(0,1).toUpperCase() + s.substring(1));
                // Open on the main thread
                final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
                mainThreadHandler.post(() -> new AlertDialog.Builder(this)
                        .setTitle(R.string.pick_game)
                        .setItems(
                                games.toArray(new CharSequence[0]),
                                (dialog, which) -> openDetection(games.get(which)))
                        .setCancelable(false)
                        .create()
                        .show());

            } catch (Exception ex) {
                new ErrorDialogFragment("Couldn't load games list", ex)
                        .show(getSupportFragmentManager(), "ErrorDialogFragment");
                openDetection(getResources().getString(R.string.no_game));
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent going back
        Toast.makeText(this, R.string.no_going_back, Toast.LENGTH_LONG).show();
    }

    private void openDetection(String game) {
        builder.setGame(game)
                .makeDetection()
                .observe(this, res -> {
                    final Detection detection = res.getDetection();
                    if(detection == null) {
                        new ErrorDialogFragment(res.getErrorMessage(), res.getException())
                                .addCancelListener(d -> finish())
                                .show(getSupportFragmentManager(), "ErrorDialogFragment");
                        return;
                    }

                    final Intent detectionIntent = new Intent(this, DetectionActivity.class);
                    detectionIntent.putExtra(DetectionActivity.INPUT_DETECTION_ID, detection.getId());

                    finish();
                    startActivity(detectionIntent);
                });
    }
}
