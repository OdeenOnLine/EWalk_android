package milestone.ewalk.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class Util {

    // 是否显示debug的内容
    public static boolean m_debug = true;

    /**
     * 单纯的日志
     *
     * @param tag
     * @param str
     */
    public static void Log(String tag, String str) {
        if (m_debug) {
            Log.d(tag, str);
        }
    }

    public static void Tip(Context context, String str) {
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    /**
     * 读取照片exif信息中的旋转角度
     *
     * @param path
     *            照片路径
     * @return角度
     */
    public static Bitmap readPictureDegree(String path, Bitmap bmp)
            throws OutOfMemoryError {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            if (exifInterface != null) {
                int orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
                return adjustPhotoRotation(bmp, degree, 2);
            } else {
                return bmp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    private static Bitmap adjustPhotoRotation(Bitmap bm,
                                              final int orientationDegree, int inSampleSize)
            throws OutOfMemoryError {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2,
                (float) bm.getHeight() / 2);
        Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                bm.getHeight(), m, true);
        return bm1;
    }

    public static List<Camera.Size> getResolutionList(Camera camera)
    {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        return previewSizes;
    }

    public static class ResolutionComparator implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if(lhs.height!=rhs.height)
                return lhs.height-rhs.height;
            else
                return lhs.width-rhs.width;
        }

    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return "";
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static boolean checkMobile(String mobile) {
        String regex = "(\\+\\d+)?1[23456789]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }

    /** 检查是否有网络 */
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null) {
            boolean available = info.isAvailable();
            if (!available) {
                Tip(context,"无网络可用！");
            }
            return available;
        }
        Tip(context,"无网络可用！");
        return false;
    }

    public static NetworkInfo getNetworkInfo(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static double getCalory(double weight,double mile){
        double calory = 0;
        calory = weight*mile*1.036;
        return calory;
    }

}
