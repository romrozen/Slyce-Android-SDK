package com.android.slyce.utils;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import javax.net.ssl.HttpsURLConnection;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static String decodeBase64(String base64) {

        String result = null;

        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        try {
            result = new String(bytes, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            SlyceLog.i(TAG, "decodeBase64: UnsupportedEncodingException");
        }
        return result;
    }

    public static Bitmap scaleDown(Bitmap realImage) {

        int maxImageSize = Constants.IMAGE_RESIZE;

        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());

        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, true);
        return newBitmap;
    }

    public static int uploadBitmapToSlyce(Bitmap bitmap, String requestUrl) {

        int serverResponseCode = 0;

        HttpsURLConnection conn = null;
        DataOutputStream dos = null;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        if (bitmap == null) {
            SlyceLog.e("uploadFile", "Bitmap Does not exist");
            return 0;
        }

        // Bitmap to input stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        byte[] bitmapData = bos.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bitmapData);

        try { // open a URL connection to the Servlet

            URL url = new URL(requestUrl);
            conn = (HttpsURLConnection) url.openConnection(); // Open a HTTP

            // connection to
            // the URL
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("PUT");

            conn.setRequestProperty("Content-Type", "image/jpeg");

            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            dos = new DataOutputStream(conn.getOutputStream());

            bytesAvailable = byteArrayInputStream.available(); // create a buffer of maximum size

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = byteArrayInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);
            }

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            SlyceLog.i(TAG, "uploadFile HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            if (serverResponseCode == 200) {
                SlyceLog.i(TAG, "uploadFile File Upload Complete.");
            }

            // close the streams //
            byteArrayInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException e) {
            SlyceLog.e(TAG, "Upload file to server error: " + e.getMessage());
        } catch (Exception e) {
            SlyceLog.e(TAG, "Upload file to server Exception : " + e.getMessage());
        }

        return serverResponseCode;
    }

    public static JSONObject uploadBitmapToMS(String serverUrl, Bitmap bitmap) {

        JSONObject response = null;

        int serverResponseCode;

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {

            // open a URL connection to the Servlet
            URL url = new URL(serverUrl);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            String digest = "Digest username=\"3jygvjimebpivrohfxyf\", realm=\"Moodstocks API\", nonce=\"MTQyOTQzNDY2OCBlMWFlZTg1Y2Y4NjM2ODgxYWEzOTQxODExYjc0NmI2NA==\", uri=\"/v2/search\", response=\"36b171db79e93cd5ffa2fdbee85481a0\", opaque=\"b1a8d1044b0de768f7905b15aa7f95de\", qop=auth, nc=00000001, cnonce=\"8712f44508f59394\"";
            conn.setRequestProperty("Authorization", digest);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"image_file\"; filename=\"ms_image.jpg\"\n" + lineEnd);
            dos.writeBytes(lineEnd);

            // Bitmap to input stream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bitmapdata);

            // create a buffer of maximum size
            bytesAvailable = byteArrayInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = byteArrayInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            if (serverResponseCode == 200) {

                SlyceLog.i(TAG, "uploadFile File Upload Complete.");

                String result = readInputStreamToString(conn);

                response = new JSONObject(result);
            }

            //close the streams //
            byteArrayInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException e) {
            SlyceLog.e(TAG, "Upload file to server error: " + e.getMessage());
        } catch (Exception e) {
            SlyceLog.e(TAG, "Upload file to server Exception : " + e.getMessage());
        }

        return response;

    } // End else block


    private static String readInputStreamToString(HttpURLConnection connection) {
        String result = null;
        StringBuffer sb = new StringBuffer();
        BufferedInputStream bis = null;

        try {

            bis = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(bis));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        } catch (Exception e) {
            SlyceLog.i(TAG, "Error reading InputStream");
            result = null;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    SlyceLog.i(TAG, "Error closing InputStream");
                }
            }
        }

        return result;
    }

    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getHostAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {

            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);

        } catch (final PackageManager.NameNotFoundException e) {

        }

        String title = applicationInfo != null ?
                (String) packageManager.getApplicationLabel(applicationInfo) :
                "App Name Not Found";

        return title;
    }

    public static String getHostAppVersion(Context context) {

        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {

            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

        } catch (final PackageManager.NameNotFoundException e) {

        }

        return packageInfo.versionName;
    }

    public static String getTimestamp() {

        SimpleDateFormat noMilliSecondsFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = noMilliSecondsFormatter.format(System.currentTimeMillis());

        return time;
    }

    public static String getAndroidID(Context context) {
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return android_id;
    }

    public static String getDeviceType() {

        String deviceType;

        deviceType = Devices.getDeviceName(Build.DEVICE);

        if (TextUtils.isEmpty(deviceType)) {
            deviceType = "No Device Type";
        }

        return deviceType;
    }

    public interface CallBack{
        void onReady(String value);
    }

    public static void getGoogleAdvertisingID(final Context context, final CallBack listener){

        new Thread(new Runnable() {
            @Override
            public void run() {

                String advertisingId = null;

                Exception exception = null;
                try {

                    Class<?> mAdvertisingIdClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");

                    Method getAdvertisingIdInfoMethod = mAdvertisingIdClientClass.getMethod("getAdvertisingIdInfo", new Class[]{Context.class});
                    Object mInfoClass = getAdvertisingIdInfoMethod.invoke(null, new Object[]{context});

                    Method getIdMethod = mInfoClass.getClass().getMethod("getId", new Class[0]);
                    Method isLimitAdTrackingEnabledMethod = mInfoClass.getClass().getMethod("isLimitAdTrackingEnabled", new Class[0]);

                    advertisingId = getIdMethod.invoke(mInfoClass, new Object[0]).toString();
//                    boolean mIsLimitedTrackingEnabled = ((Boolean)isLimitAdTrackingEnabledMethod.invoke(mInfoClass, new Object[0])).booleanValue();

                    final String id = advertisingId;

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onReady(id);
                        }
                    });

                } catch (ClassNotFoundException e) {
                    exception = e;
                } catch (NoSuchMethodException e) {
                    exception = e;
                } catch (IllegalAccessException e) {
                    exception = e;
                } catch (IllegalArgumentException e) {
                    exception = e;
                } catch (InvocationTargetException e) {
                    exception = e;
                } finally{

                    if(exception != null){
                        if(exception.getMessage() != null){
                            SlyceLog.i(TAG, exception.getClass().getSimpleName() + ": " + exception.getMessage());
                        }
                        if(exception.getCause() != null){
                            SlyceLog.i(TAG, exception.getClass().getSimpleName() + ": " + exception.getCause());
                        }
                    }else{
                        SlyceLog.i(TAG, "Google Advertising Id: " + advertisingId);
                    }
                }
            }
        }).start();
    }

    public static void performAlphaAnimation(View view, float x, float y){
        view.setVisibility(View.VISIBLE);
        view.setX(x);
        view.setY(y);

        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(1000);
        animation.setStartOffset(0);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    public static void loadImageFromGallery(Fragment fragment, int result){

        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Start the Intent
        fragment.startActivityForResult(galleryIntent, result);
    }

    public static String getImageDecodableString(Intent data, Context context){

        // Get the Image from data
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        // Get the cursor
        Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        // Move to first row
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String imgDecodableString = cursor.getString(columnIndex);
        cursor.close();

        return imgDecodableString;
    }

    public static Uri getImageUri(Intent data, Context context){
        Uri selectedImage = data.getData();
        return selectedImage;
    }

}


