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
import java.util.HashMap;
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

    private Map<Card.Suit, Mat> suits;

    @Inject OpenCvCardsDetector(Context context, OpenCvLoader loader) {
        this.context = context;
        this.loader = loader;
    }

    @Override
    public void onCreate() {
        // Need to make sure this is loaded
        loader.onCreate();

        try {
            final Map<Card.Suit, Mat> temp = new ArrayMap<>();
            temp.put(Card.Suit.HEART, Utils.loadResource(context, R.drawable.hearts, IMREAD_GRAYSCALE));
            temp.put(Card.Suit.CLUB, Utils.loadResource(context, R.drawable.clubs, IMREAD_GRAYSCALE));
            temp.put(Card.Suit.SPADE, Utils.loadResource(context, R.drawable.spades, IMREAD_GRAYSCALE));
            temp.put(Card.Suit.DIAMOND, Utils.loadResource(context, R.drawable.diamonds, IMREAD_GRAYSCALE));
            suits = Collections.unmodifiableMap(temp);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public Bitmap detectCards(Bitmap input, Collection<Card> cards) {

        final Mat bgr = new Mat();
        Utils.bitmapToMat(input, bgr);

        final Mat processed = new Mat();
        preprocessImage(bgr, processed);
        // Get all the possible contours
        final List<MatOfPoint> allContours = new ArrayList<>();
        final Mat allHierarchy = new Mat();
        findContours(processed, allContours, allHierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);
        // Get a first list of contours that might be card suits
        // and put them on a mask
        final Mat candidatesMask = new Mat(bgr.size(), CvType.CV_8U, new Scalar(0));
        drawCandidatesMask(candidatesMask, findSuitCandidates(allContours, allHierarchy));
        // Get only candidates contours
        final List<MatOfPoint> contours = new ArrayList<>();
        final Mat hierarchy = new Mat();
        findContours(candidatesMask, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);
        // Find matching suits
        final Map<MatOfPoint, Card.Suit> matchedSuits = matchSuits(bgr, filterSuits(contours, hierarchy));
        for(Map.Entry<MatOfPoint, Card.Suit> entry : matchedSuits.entrySet()) {
            final Rect boundingRect = boundingRect(entry.getKey());
            rectangle(bgr, boundingRect, new Scalar(255, 0, 0), 10);
            putText(bgr,
                    entry.getValue().name().toLowerCase(),
                    new Point(boundingRect.x, boundingRect.y - 1),
                    FONT_HERSHEY_PLAIN,
                    5, new Scalar(255, 0, 0), 10);
        }

//        final Collection<MatOfPoint> points = filterSuits(contours, hierarchy);
//        int i = 0;
//        for(MatOfPoint contour : points) {
//            drawContours(bgr,
//                    Collections.singletonList(contour),
//                    -1,
//                    new Scalar(
//                            i == 0 ? 255 : 0,
//                            i == 1 ? 255 : 0,
//                            i == 2 ? 255 : 0),
//                    FILLED);
//            i = (i + 1) % 3;
//        }

//        final MatOfPoint cardContour = allContours.get(919);
//        for(MatOfPoint cardContour : contours) {
//        final MatOfPoint2f contour2f = new MatOfPoint2f();
//        cardContour.convertTo(contour2f, CvType.CV_32F);
//
//            final MatOfPoint2f approx2f = new MatOfPoint2f();
//            approxPolyDP(contour2f,
//                    approx2f,
//                    0.01D * arcLength(contour2f, true),
//                    true);
//
//            final Mat approx = new Mat();
//            cardContour.convertTo(approx, CvType.CV_32S);
//
//        final RotatedRect rect = minAreaRect(contour2f);
//        final Point[] points = new Point[4];
//        rect.points(points);
//
//            drawContours(bgr,
//                    Collections.singletonList(cardContour),
//                    -1,
//                    new Scalar(255), FILLED);
//        for(int i = 0; i < 4; i++)
//            line(bgr,
//                    points[i],
//                    points[(i + 1) % 4],
//                    new Scalar(255, 255, 0),
//                    5);
//
//            final Rect br = boundingRect(cardContour);
//            rectangle(bgr, br, new Scalar(0, 255, 0), 5);
//
//            fillConvexPoly(mask, new MatOfPoint(points), new Scalar(255));
//        }

        final Bitmap output = Bitmap.createBitmap(bgr.width(),
                bgr.height(),
                input.getConfig(),
                input.hasAlpha());
        Utils.matToBitmap(bgr, output);

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

        final Map<MatOfPoint, Card.Suit> matchedSuits = new HashMap<>();

        final Mat grey = new Mat();
        cvtColor(bgr, grey, COLOR_BGR2GRAY);

        for(MatOfPoint suitContour : contours) {
            final Rect boundingRect = boundingRect(suitContour);

            final Mat warped = new Mat();
            warp(grey, warped, suitContour);

            double bestMatch = -1;
            double matchRotation = -1;
            Card.Suit matchSuit = null;

            for(float rotation : new float[] { 0, 45, 90, 135, 180, 225, 270, 315 }) {
                // Isolate suit
                final Mat suit = new Mat();
                if(!isolateSuit(warped.submat(boundingRect), rotation, suit))
                    continue;
                // Resize to the comparison img dimensions
                final Mat resized = new Mat();
                resize(suit, resized, new Size(SUIT_WIDTH, SUIT_HEIGHT), 0, 0);
                // Calculate difference with each actual suit
                for(Map.Entry<Card.Suit, Mat> entry : suits.entrySet()) {
                    final Mat imgDiff = new Mat();
                    absdiff(resized, entry.getValue(), imgDiff);

                    final double diff = sumElems(imgDiff).val[0] / 255;
                    if(bestMatch == -1 || diff < bestMatch) {
                        bestMatch = diff;
                        matchRotation = rotation;
                        matchSuit = entry.getKey();
                    }
                }
            }

            if(matchSuit != null && bestMatch < 1250)
                matchedSuits.put(suitContour, matchSuit);
            System.out.println(bestMatch + " " + matchRotation + " " + matchSuit);
        }

        return matchedSuits;
    }

    private boolean isolateSuit(Mat roi, float rotation, Mat suit) {
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
        // Find the suit contour (the biggest one) and
        final List<MatOfPoint> contours = new ArrayList<>();
        findContours(rotated, contours, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);
        if(contours.isEmpty())
            return false;

        MatOfPoint suitContour = contours.get(0);
        for(int k = 1; k < contours.size(); k++)
            if(contourArea(contours.get(k)) > contourArea(suitContour))
                suitContour = contours.get(k);
        // Isolate it
        final Mat suit0 = rotated.submat(boundingRect(suitContour));
        suit0.copyTo(suit);

        return true;
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
