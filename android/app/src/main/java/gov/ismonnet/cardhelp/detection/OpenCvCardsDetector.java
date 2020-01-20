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
import java.util.List;
import java.util.Map;

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
import static org.opencv.imgproc.Imgproc.CV_SHAPE_RECT;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
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
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.minAreaRect;
import static org.opencv.imgproc.Imgproc.morphologyEx;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.warpAffine;
import static org.opencv.imgproc.Imgproc.warpPerspective;

class OpenCvCardsDetector implements CardsDetector, ActivityLifeCycle {

    private static final int SYMBOL_MIN_AREA = 100;

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
        suits.clear();
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
        drawCandidatesMask(candidatesMask, findSymbolCandidates(allContours, allHierarchy));
        // Get only candidates contours
        final List<MatOfPoint> contours = new ArrayList<>();
        findContours(candidatesMask, contours, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);
        // Find matching suits
        matchSuits(bgr, contours);

        final MatOfPoint cardContour = allContours.get(919);
//        for(MatOfPoint cardContour : contours) {
        final MatOfPoint2f contour2f = new MatOfPoint2f();
        cardContour.convertTo(contour2f, CvType.CV_32F);
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
        final RotatedRect rect = minAreaRect(contour2f);
        final Point[] points = new Point[4];
        rect.points(points);
//
//            drawContours(bgr,
//                    Collections.singletonList(cardContour),
//                    -1,
//                    new Scalar(255), FILLED);
        for(int i = 0; i < 4; i++)
            line(bgr,
                    points[i],
                    points[(i + 1) % 4],
                    new Scalar(255, 255, 0),
                    5);
//
//            final Rect br = boundingRect(cardContour);
//            rectangle(bgr, br, new Scalar(0, 255, 0), 5);
//
//            fillConvexPoly(mask, new MatOfPoint(points), new Scalar(255));
//        }

        final Mat warped = new Mat();
        warp(bgr, warped, cardContour);

        final Mat rotated = new Mat();
        rotate(warped, rotated, -90 + 45);

        final Bitmap output = Bitmap.createBitmap(rotated.width(),
                rotated.height(),
                input.getConfig(),
                input.hasAlpha());
        Utils.matToBitmap(rotated, output);

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

    private Collection<MatOfPoint> findSymbolCandidates(List<MatOfPoint> contours, Mat hierarchy) {

        // Search possible cards in the contours

        final List<MatOfPoint> cards = new ArrayList<>();
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
            if(area < SYMBOL_MIN_AREA)
                continue;
//            if(area > SYMBOL_MAX_AREA)
//                continue;

            cards.add(contour);
        }

        return cards;
    }

    private void drawCandidatesMask(final Mat mask,
                                    final Collection<MatOfPoint> candidates) {
        for (MatOfPoint suitContour : candidates)
            drawContours(mask,
                    Collections.singletonList(suitContour),
                    -1,
                    new Scalar(255));
        // Dilate to merge some of the contours
        final Mat dilated = new Mat();
        dilate(mask, dilated,
                getStructuringElement(CV_SHAPE_RECT, new Size(5, 5)),
                new Point(-1, -1), // Default, means center of the image
                1);
    }

    private Collection<String> matchSuits(Mat bgr, List<MatOfPoint> contours) {
        System.out.println(contours.size());
        for(MatOfPoint suitContour : contours) {

            final Mat warped = new Mat();
            warp(bgr, warped, suitContour);

            final Rect boundingRect = boundingRect(suitContour);
            final Mat roi = bgr.submat(boundingRect);

            double bestMatch = -1;
            double matchRotation = -1;
            Card.Suit matchSuit = null;

            for(float rotation : new float[] { 0, 45, 90, 135, 180, 225, 270, 315 }) {

                final Mat rotated;
                if(rotation == 0) {
                    rotated = roi.clone();
                } else {
                    rotated = new Mat();
                    rotate(roi, rotated, rotation);
                }

                final Mat resized = new Mat();
                resize(rotated, resized, new Size(SUIT_WIDTH, SUIT_HEIGHT), 0, 0);

                final Mat grey = new Mat();
                cvtColor(resized, grey, COLOR_BGR2GRAY);

                for(Map.Entry<Card.Suit, Mat> entry : suits.entrySet()) {

                    final Mat imgDiff = new Mat();
                    absdiff(grey, entry.getValue(), imgDiff);

                    final double diff = sumElems(imgDiff).val[0] / 255;
                    if(bestMatch == -1 || diff < bestMatch) {
                        bestMatch = diff;
                        matchRotation = rotation;
                        matchSuit = entry.getKey();
                    }
                }
            }

            System.out.println(bestMatch + " " + matchRotation + " " + matchSuit);
        }

        return Collections.emptyList();
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
