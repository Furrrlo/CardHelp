package gov.ismonnet.cardhelp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

import androidx.exifinterface.media.ExifInterface;

public class BitmapUtils {

    private BitmapUtils() {}

    public static Bitmap loadBitmap(Context context, File file) throws IOException {
        return loadBitmap(context, Uri.fromFile(file));
    }

    public static Bitmap loadBitmap(Context context, Uri photoUri) throws IOException {
        final Bitmap orig = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
        final float angle = getExifAngle(context, photoUri);
        if(angle == -1f)
            return orig;
        return rotateImage(orig, angle);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        final Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source,
                0, 0,
                source.getWidth(), source.getHeight(),
                matrix, true);
    }


    public static float getExifAngle(Context context, Uri uri) throws IOException {
        ExifInterface exifInterface = getExifInterface(context, uri);
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

    public static ExifInterface getExifInterface(Context context, Uri uri) throws IOException {
        return new ExifInterface(context.getContentResolver().openInputStream(uri));
    }
}
