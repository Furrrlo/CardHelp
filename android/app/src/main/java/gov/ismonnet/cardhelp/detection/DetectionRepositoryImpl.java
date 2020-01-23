package gov.ismonnet.cardhelp.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.activity.LifeCycle;
import gov.ismonnet.cardhelp.core.Card;
import gov.ismonnet.cardhelp.core.CardsDetector;
import gov.ismonnet.cardhelp.core.Detection;
import gov.ismonnet.cardhelp.core.DetectionDeserializer;
import gov.ismonnet.cardhelp.core.DetectionFactory;
import gov.ismonnet.cardhelp.core.DetectionRepository;
import gov.ismonnet.cardhelp.core.DetectionSerializer;
import gov.ismonnet.cardhelp.core.ScoreService;
import gov.ismonnet.cardhelp.core.SerializationException;

class DetectionRepositoryImpl implements DetectionRepository, LifeCycle {

    private static final String TAG = DetectionRepositoryImpl.class.getSimpleName();
    private static final String EXT = "json";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final Context context;

    private final File metadataDirectory;
    private final DetectionDeserializer deserializer;

    private int thumbnailWidth;
    private int thumbnailHeight;

    private String noGame;

    private final ExecutorService executor;
    private final CardsDetector cardsDetector;
    private final ScoreService scoreService;

    private final DetectionFactory detectionFactory;
    private final DetectionSerializer serializer;

    private final SparseArray<Detection> idToDetections;
    private final List<Detection> detections;
    private final List<Detection> unmodifiableDetections;
    private final Comparator<Detection> comparator;

    @Inject DetectionRepositoryImpl(Context context,
                                    @Metadata File metadataDirectory,
                                    DetectionDeserializer deserializer,
                                    ExecutorService executor,
                                    CardsDetector cardsDetector,
                                    ScoreService scoreService,
                                    DetectionFactory detectionFactory,
                                    DetectionSerializer serializer) {
        this.context = context;

        this.metadataDirectory = metadataDirectory;
        this.deserializer = deserializer;

        this.executor = executor;
        this.cardsDetector = cardsDetector;
        this.scoreService = scoreService;

        this.detectionFactory = detectionFactory;
        this.serializer = serializer;

        // Compare for timestamp (last to first)
        this.idToDetections = new SparseArray<>();
        this.detections = new ArrayList<>();
        this.unmodifiableDetections = Collections.unmodifiableList(detections);
        this.comparator = (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp());
    }

    @Override
    public void onCreate() {

        if(!metadataDirectory.exists() && !metadataDirectory.mkdirs())
            throw new RuntimeException("Couldn't create dir " + metadataDirectory);

        thumbnailWidth = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_width);
        thumbnailHeight = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_height);

        noGame = context.getResources().getString(R.string.no_game);

        for (File file : metadataDirectory.listFiles()) {

            if(!file.getName().endsWith(EXT))
                continue;

            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET))) {

                final StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                    sb.append(line).append("\n");

                final String content = sb.toString();
                final Detection deserialized = deserializer.deserialize(content);

                idToDetections.put(deserialized.getId(), deserialized);

                detections.add(deserialized);
                detections.sort(comparator);

            } catch (SerializationException ex) {
                Log.w(TAG, "Couldn't deserialize file " + file, ex);
            } catch (IOException ex) {
                Log.e(TAG, "Couldn't read file " + file, ex);
            }
        }
    }

    @Override
    public List<Detection> getDetections() {
        return unmodifiableDetections;
    }

    @Override
    public Detection getDetection(int id) {
        return idToDetections.get(id);
    }

    @Override
    public Detection.Builder newDetection(Bitmap input) {
        final DetectionBuilderImpl builder = new DetectionBuilderImpl(input, Instant.now());
        builder.detection.observeForever(new Observer<Detection.DetectionResult>() {
            @Override
            public void onChanged(Detection.DetectionResult res) {

                final Detection detection = res.getDetection();
                if(detection == null)
                    return;

                final String serialized = serializer.serialize(detection);
                final File file = new File(metadataDirectory,
                        "detection-" + detection.getTimestamp().toEpochMilli() + "." + EXT);

                try (FileWriter fw = new FileWriter(file)) {
                    fw.write(serialized);
                } catch (IOException ex) {
                    Log.w(TAG, "Couldn't write file " + file, ex);
                }

                idToDetections.put(detection.getId(), detection);

                detections.add(detection);
                detections.sort(comparator);

                builder.detection.removeObserver(this);
            }
        });

        return builder;
    }

    class DetectionBuilderImpl implements Detection.Builder {

        private final AtomicBoolean built = new AtomicBoolean(false);
        private final MutableLiveData<Detection.DetectionResult> detection;

        private final File thumbnailPath;
        private final File inputPath;
        private final File outputPath;
        private final Instant timestamp;

        private final List<Card> cards;
        private String game = noGame; // TODO: remove this
        private String score = "none"; // TODO: remove this

        DetectionBuilderImpl(Bitmap input, Instant timestamp) {

            detection = new MutableLiveData<>();

            final long toSave = timestamp.toEpochMilli();
            this.thumbnailPath = ensureDontExists(metadataDirectory, "thumb-" + toSave, ".jpg");
            this.inputPath = ensureDontExists(metadataDirectory, "input-" + toSave, ".jpg");
            this.outputPath = ensureDontExists(metadataDirectory, "output-" + toSave, ".jpg");

            this.timestamp = timestamp;
            this.cards = new ArrayList<>();
            handleInput(input);
        }

        @SuppressWarnings("SameParameterValue")
        private File ensureDontExists(File folder, String name, String ext) {
            File file = new File(folder, name + ext);
            int i = 1;
            while(file.exists())
                file = new File(folder, name + "_" + (i++) + ext);
            return file;
        }

        @Override
        public Detection.Builder setGame(String game) {
            if(built.get())
                throw new UnsupportedOperationException("Detection was already built");

            this.game = game;
            return this;
        }

        private void handleInput(Bitmap input) {
            executor.submit(() -> {
                try {
                    try (FileOutputStream out = new FileOutputStream(inputPath)) {
                        input.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    } catch (IOException e) {
                        detection.postValue(new DetectionResultImpl("Couldn't copy input bitmap", e));
                        return;
                    }

                    final Bitmap output = cardsDetector.detectCards(input, cards);

                    try (FileOutputStream out = new FileOutputStream(outputPath)) {
                        output.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    } catch (IOException e) {
                        detection.postValue(new DetectionResultImpl("Couldn't copy output bitmap", e));
                        return;
                    }

                    try (FileOutputStream out = new FileOutputStream(thumbnailPath)) {
                        Bitmap.createScaledBitmap(output, thumbnailWidth, thumbnailHeight, false)
                                .compress(Bitmap.CompressFormat.JPEG, 100, out);
                    } catch (IOException e) {
                        detection.postValue(new DetectionResultImpl("Couldn't copy output bitmap", e));
                        return;
                    }

                    detection.postValue(new DetectionResultImpl(detectionFactory.makeDetection(
                            thumbnailPath,
                            inputPath,
                            outputPath,
                            cards,
                            game,
                            score,
                            timestamp)));
                } catch (Throwable t) {
                    detection.postValue(new DetectionResultImpl("Couldn't perform detection", t));
                }
            });
        }

        @Override
        public LiveData<Detection.DetectionResult> makeDetection() {

            if(built.getAndSet(true))
                return detection;

            // TODO: score
            // If game is noGame, set score to "none"

            return detection;
        }
    }

    static class DetectionResultImpl implements Detection.DetectionResult {

        @Nullable
        final Detection result;
        @Nullable
        final String errorMsg;
        @Nullable
        final Throwable error;

        DetectionResultImpl(@NonNull Detection result) {
            this.result = result;
            this.errorMsg = null;
            this.error = null;
        }

        DetectionResultImpl(@NonNull String errorMsg,
                            @NonNull Throwable error) {
            this.errorMsg = errorMsg;
            this.error = error;
            this.result = null;
        }

        @Nullable
        @Override
        public Detection getDetection() {
            return result;
        }

        @Nullable
        @Override
        public String getErrorMessage() {
            return errorMsg;
        }

        @Nullable
        @Override
        public Throwable getException() {
            return error;
        }
    }
}
