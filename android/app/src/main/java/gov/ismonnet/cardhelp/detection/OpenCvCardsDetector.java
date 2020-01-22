package gov.ismonnet.cardhelp.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.ArrayMap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.inject.Inject;

import gov.ismonnet.cardhelp.Card;
import gov.ismonnet.cardhelp.R;
import gov.ismonnet.cardhelp.activity.ActivityLifeCycle;
import gov.ismonnet.cardhelp.core.CardsDetector;

import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.sumElems;
import static org.opencv.imgcodecs.Imgcodecs.IMREAD_GRAYSCALE;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.CONTOURS_MATCH_I1;
import static org.opencv.imgproc.Imgproc.CV_SHAPE_ELLIPSE;
import static org.opencv.imgproc.Imgproc.CV_SHAPE_RECT;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_PLAIN;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.matchShapes;
import static org.opencv.imgproc.Imgproc.minAreaRect;
import static org.opencv.imgproc.Imgproc.morphologyEx;
import static org.opencv.imgproc.Imgproc.putText;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.threshold;
import static org.opencv.imgproc.Imgproc.warpAffine;
import static org.opencv.imgproc.Imgproc.warpPerspective;

class OpenCvCardsDetector implements CardsDetector, ActivityLifeCycle {

    private static final int SUIT_MIN_AREA = 5000;

    private static final int SUIT_WIDTH = 70;
    private static final int SUIT_HEIGHT = 100;

    private final Context context;
    private final OpenCvLoader loader;

    private Map<ReferenceSuit, Card.Suit> referenceSuits;

    @Inject OpenCvCardsDetector(Context context, OpenCvLoader loader) {
        this.context = context;
        this.loader = loader;
    }

    @Override
    public void onCreate() {
        // Need to make sure this is loaded
        loader.onCreate();

        try {
            final Map<Mat, Card.Suit> temp0 = new ArrayMap<>();
            temp0.put(Utils.loadResource(context, R.drawable.hearts, IMREAD_GRAYSCALE), Card.Suit.HEART);
            temp0.put(Utils.loadResource(context, R.drawable.obese_clubs, IMREAD_GRAYSCALE), Card.Suit.CLUB);
            temp0.put(Utils.loadResource(context, R.drawable.retarded_club, IMREAD_GRAYSCALE), Card.Suit.CLUB);
            temp0.put(Utils.loadResource(context, R.drawable.obese_spades, IMREAD_GRAYSCALE), Card.Suit.SPADE);
            temp0.put(Utils.loadResource(context, R.drawable.retarded_spades, IMREAD_GRAYSCALE), Card.Suit.SPADE);
            temp0.put(Utils.loadResource(context, R.drawable.diamonds, IMREAD_GRAYSCALE), Card.Suit.DIAMOND);

            final Map<ReferenceSuit, Card.Suit> temp = new ArrayMap<>();
            for(Map.Entry<Mat, Card.Suit> entry : temp0.entrySet()) {
                final Mat mat = entry.getKey();

                final Mat thresholded = new Mat();
                threshold(mat, thresholded, 127, 255, THRESH_BINARY);

                final List<MatOfPoint> contours = new ArrayList<>();
                findContours(thresholded, contours, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);
                if(contours.size() != 1)
                    throw new AssertionError("There shouldn't be more or less than 1 contour (" + contours.size() + ')');

                final MatOfPoint contour = contours.get(0);
                final double area = contourArea(contour);

                temp.put(new ReferenceSuit(thresholded, contour, area), entry.getValue());
            }

            referenceSuits = Collections.unmodifiableMap(temp);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    class ReferenceSuit {
        private final Mat mat;
        private final MatOfPoint contour;
        private final double area;

        public ReferenceSuit(Mat mat, MatOfPoint contour, double area) {
            this.mat = mat;
            this.contour = contour;
            this.area = area;
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public Bitmap detectCards(Bitmap input, Collection<Card> cards) {

        final Mat bgr = new Mat();
        Utils.bitmapToMat(input, bgr);

        // TODO: remove
        final Mat grey = new Mat();
        cvtColor(bgr, grey, COLOR_BGR2GRAY);

        final Mat processed = new Mat();
        preprocessImage(bgr, processed);
        // Get all the possible contours
        final List<MatOfPoint> allContours = new ArrayList<>();
        final Mat allHierarchy = new Mat();
        findContours(processed, allContours, allHierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);
        // Get a first list of contours that might be card referenceSuits
        // and put them on a mask
        final Mat candidatesMask = new Mat(bgr.size(), CvType.CV_8U, new Scalar(0));
        drawCandidatesMask(candidatesMask, findSuitCandidates(allContours, allHierarchy));
        // Get only candidates contours
        final List<MatOfPoint> contours = new ArrayList<>();
        final Mat hierarchy = new Mat();
        findContours(candidatesMask, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);
        // Find matching referenceSuits
        final Map<MatOfPoint, Card.Suit> matchedSuits = matchSuits(bgr, filterSuits(contours, hierarchy));
        int i = 0;
        for(Map.Entry<MatOfPoint, Card.Suit> entry : matchedSuits.entrySet()) {
            final Rect boundingRect = boundingRect(entry.getKey());
            rectangle(bgr, boundingRect, new Scalar(255, 0, 0), 10);
            putText(bgr,
                    i + " " + entry.getValue().name().toLowerCase(),
                    new Point(boundingRect.x, boundingRect.y - 5),
                    FONT_HERSHEY_PLAIN,
                    5, new Scalar(255, 0, 0), 10);
            i++;
        }

        // TODO: temp

        i = 0;
        Mat test = null;
        for(MatOfPoint candidateContour : filterSuits(contours, hierarchy)) {
            final Rect boundingRect = boundingRect(candidateContour);

            final Mat warped = new Mat();
            warp(grey, warped, candidateContour);

            for(float rotation : new float[] { 135 }) {
                // Isolate suit
                final Mat processedCandidate = new Mat();
                if(!isolateSuit(warped.submat(boundingRect), processedCandidate, rotation))
                    continue;
                // Resize to the comparison img dimensions
                final Mat resized = new Mat();
                resize(processedCandidate, resized, new Size(SUIT_WIDTH, SUIT_HEIGHT), 0, 0);
                // Get the per element difference
//                final Mat imgDiff = new Mat();
//                absdiff(resized, referenceSuits.get(Card.Suit.CLUB).mat, imgDiff);

                test = resized;
            }

            if(i == 6)
                break;
            i++;
        }

        // fine temp

        final Mat out = bgr;
        final Bitmap output = Bitmap.createBitmap(out.width(),
                out.height(),
                input.getConfig(),
                input.hasAlpha());
        Utils.matToBitmap(out, output);

        return output;
    }

    private void preprocessImage(Mat bgr, Mat output) {
        // Grey to threshold
        final Mat grey = new Mat();
        cvtColor(bgr, grey, COLOR_BGR2GRAY);
        // Remove a bit of noise
        final Mat blur = new Mat();
        GaussianBlur(grey, blur, new Size(5,5), 0);
        // Threshold to find all contours
        final Mat threshold = new Mat();
        adaptiveThreshold(blur, threshold,
                255,
                ADAPTIVE_THRESH_MEAN_C,
                THRESH_BINARY,
                15, 8);
        // Remove noise v2
        morphologyEx(threshold, output,
                MORPH_CLOSE,
                getStructuringElement(CV_SHAPE_RECT, new Size(5, 5)));
    }

    private List<MatOfPoint> findSuitCandidates(List<MatOfPoint> contours, Mat hierarchy) {

        // Search possible cards in the contours

        final List<MatOfPoint> suits = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            final MatOfPoint contour = contours.get(i);

            // No child

            // [Next, Previous, First_Child, Parent]
            final double[] contourInfo = hierarchy.get(0, i);
            final int firstChild = (int) contourInfo[2];

            if(firstChild != -1)
                continue;

            // Get rid of a bit of noise

            final double area = contourArea(contour);
            if(area < 100)
                continue;

            suits.add(contour);
        }

        return suits;
    }

    private List<MatOfPoint> filterSuits(List<MatOfPoint> contours, Mat hierarchy) {

        final List<MatOfPoint> suits = new ArrayList<>();
        for (int i = 0; i < contours.size(); i++) {
            final MatOfPoint contour = contours.get(i);

            // No parent

            // [Next, Previous, First_Child, Parent]
            final double[] contourInfo = hierarchy.get(0, i);
            final int parent = (int) contourInfo[3];

            if(parent != -1)
                continue;

            // Decent warped size

            final double area = boundingRect(contour).area();
            if(area < SUIT_MIN_AREA)
                continue;

            suits.add(contour);
        }

        return suits;
    }

    private void drawCandidatesMask(final Mat mask,
                                    final List<MatOfPoint> candidates) {
        drawContours(mask,
                candidates,
                -1,
                new Scalar(255));
        // Dilate to merge some of the contours
        final Mat dilated = new Mat();
        dilate(mask, dilated,
                getStructuringElement(CV_SHAPE_ELLIPSE, new Size(5, 5)),
                new Point(-1, -1), // Default, means center of the image
                3);
        dilated.copyTo(mask);
    }

    private Map<MatOfPoint, Card.Suit> matchSuits(Mat bgr, List<MatOfPoint> contours) {

        // TODO: non linked HashMap
        final Map<MatOfPoint, Card.Suit> matchedSuits = new LinkedHashMap<>();

        final Mat grey = new Mat();
        cvtColor(bgr, grey, COLOR_BGR2GRAY);

        int i = 0, j = 0;
        for(MatOfPoint candidateContour : contours) {
            final Rect boundingRect = boundingRect(candidateContour);

            final Mat warped = new Mat();
            warp(grey, warped, candidateContour);

            class Match {
                private final Card.Suit suit;
                private final double diff;
                private final double pxDiff;
                private final double areaDiff;
                private final double matchShapes;
                private final float rotation;

                private Match(Card.Suit suit, double pxDiff, double areaDiff, double matchShapes, float rotation) {
                    this.suit = suit;
                    this.diff = pxDiff + areaDiff + matchShapes;
                    this.pxDiff = pxDiff;
                    this.areaDiff = areaDiff;
                    this.matchShapes = matchShapes;
                    this.rotation = rotation;
                }

                @Override
                public String toString() {
                    return "Match{" +
                            "suit=" + suit +
                            ", diff=" + diff +
                            ", pxDiff=" + pxDiff +
                            ", areaDiff=" + areaDiff +
                            ", matchShapes=" + matchShapes +
                            ", rotation=" + rotation +
                            '}';
                }
            }

            final List<Match> matches = new ArrayList<>();
            for(float rotation : new float[] { 0, 45, 90, 135, 180, 225, 270, 315 }) {
                // Isolate suit
                final Mat processedCandidate = new Mat();
                if(!isolateSuit(warped.submat(boundingRect), processedCandidate, rotation))
                    continue;
                // Resize to the comparison img dimensions
                final Mat resized = new Mat();
                resize(processedCandidate, resized, new Size(SUIT_WIDTH, SUIT_HEIGHT), 0, 0);
                // Get the contour areaDiff
                final List<MatOfPoint> processedContours = new ArrayList<>();
                findContours(resized, processedContours, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);
//                if(processedContours.size() != 1)
//                    throw new AssertionError("There shouldn't be more or less than 1 contour (" + contours.size() + ')');

                final MatOfPoint processedContour = getBiggestContour(processedContours);
                final double processedArea = contourArea(processedContour);

                // Calculate difference with each actual suit
                for(Map.Entry<ReferenceSuit, Card.Suit> entry : referenceSuits.entrySet()) {

                    final ReferenceSuit reference = entry.getKey();
                    final Card.Suit suit = entry.getValue();
                    // Get the per element difference
                    final Mat imgDiff = new Mat();
                    absdiff(resized, reference.mat, imgDiff);

                    final double pxDiff = sumElems(imgDiff).val[0] / 255D;
                    // Get the area difference
                    final double areaDiff = Math.abs(reference.area - processedArea);
                    // Get the match shapes metric
                    final double matchShapes = matchShapes(processedContour, reference.contour, CONTOURS_MATCH_I1, 0) * 1000D;

                    matches.add(new Match(suit, pxDiff, areaDiff, matchShapes, rotation));
                }
            }

            matches.sort((o1, o2) -> Double.compare(o1.diff, o2.diff));

            if(!matches.isEmpty() &&
                    matches.get(0).pxDiff < 1250 &&
                    matches.get(0).diff < 2000) {

                matchedSuits.put(candidateContour, matches.get(0).suit);

                System.out.println(i + " " + j + " " + matches.get(0));
                System.out.println(i + " close " + matches.get(1));
                i++;
            }
            j++;
        }

        return matchedSuits;
    }

    private boolean isolateSuit(Mat roi, Mat suit, float rotation) {
        // Avg white from the 4 pixels to find threshold level
        final int thresholdLevel = (int) IntStream.of(
                (int) roi.get(0, 0)[0],
                (int) roi.get(roi.rows() - 1, 0)[0],
                (int) roi.get(roi.rows() - 1, roi.cols() - 1)[0],
                (int) roi.get(0, roi.cols() - 1)[0])
                .average()
                .orElse(31) - 30;
        final Mat thresholded = new Mat();
        threshold(roi, thresholded, thresholdLevel, 255, THRESH_BINARY_INV);
        // Rotate Image
        final Mat rotated;
        if(rotation == 0) {
            rotated = thresholded;
        } else {
            rotated = new Mat();
            rotate(thresholded, rotated, rotation);
        }
        // Find the suit contour (the biggest one)
        final List<MatOfPoint> contours = new ArrayList<>();
        findContours(rotated, contours, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);
        if(contours.isEmpty())
            return false;
        // TODO: should return all and check each one of them
        final MatOfPoint suitContour = getBiggestContour(contours);
        // Isolate it
        final Mat suit0 = rotated.submat(boundingRect(suitContour));
        suit0.copyTo(suit);

        return true;
    }

    private MatOfPoint getBiggestContour(List<MatOfPoint> contours) {
        if(contours.isEmpty())
            return null;

        MatOfPoint biggest = contours.get(0);
        for(int k = 1; k < contours.size(); k++)
            if(contourArea(contours.get(k)) > contourArea(biggest))
                biggest = contours.get(k);
        return biggest;
    }

    private void warp(Mat toWarp, Mat warped, MatOfPoint contour) {

        final MatOfPoint2f contour2f = new MatOfPoint2f();
        contour.convertTo(contour2f, CvType.CV_32F);

        final RotatedRect minAreaRect = minAreaRect(contour2f);
        Point[] minAreaPoints = new Point[4];
        minAreaRect.points(minAreaPoints);

        minAreaPoints = new Point[] {
                minAreaPoints[1],
                minAreaPoints[0],
                minAreaPoints[2],
                minAreaPoints[3],
        };

        final Rect boundingRect = boundingRect(contour);
        final Point[] boundingRectPoints = new Point[] {
                new Point(boundingRect.x, boundingRect.y),
                new Point(boundingRect.x,boundingRect.y + boundingRect.height),
                new Point(boundingRect.x + boundingRect.width, boundingRect.y),
                new Point(boundingRect.x + boundingRect.width,boundingRect.y + boundingRect.height)
        };

        final Mat matrix = getPerspectiveTransform(
                new MatOfPoint2f(minAreaPoints),
                new MatOfPoint2f(boundingRectPoints));
        warpPerspective(toWarp, warped, matrix, toWarp.size());
    }

    private void rotate(Mat toRotate, Mat rotated, float angle) {
        // https://www.pyimagesearch.com/2017/01/02/rotate-images-correctly-with-opencv-and-python/

        final float width = toRotate.width();
        final float height = toRotate.height();

        final float centerX = width / 2;
        final float centerY = height / 2;

        final Mat rotationMatrix = getRotationMatrix2D(
                new Point(centerX, centerY),
                -angle, 1.0);
        final double cos = Math.abs(rotationMatrix.get(0, 0)[0]);
        final double sin = Math.abs(rotationMatrix.get(0, 1)[0]);

        final int newWidth = (int) (height * sin + width * cos);
        final int newHeigth = (int) (height * cos + width * sin);

        rotationMatrix.put(0, 2, rotationMatrix.get(0, 2)[0] + (newWidth / 2f) - centerX);
        rotationMatrix.put(1, 2, rotationMatrix.get(1, 2)[0] + (newHeigth / 2f) - centerY);

        warpAffine(toRotate, rotated, rotationMatrix, new Size(newWidth, newHeigth));
    }


}
