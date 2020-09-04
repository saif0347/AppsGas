package com.app.ahgas_004b8c8.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/*
ImageUtil.showSourcePopup(this);

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Bitmap bmp;
    switch (requestCode) {
        case ImageUtil.GALL_CODE:
            bmp = ImageUtil.getGalleryImage(Test.this, data);
            if(bmp != null){
                imageView.setImageBitmap(bmp);
            }
            break;
        case ImageUtil.CAM_CODE:
            bmp = ImageUtil.getCameraImage(Test.this, data);
            if(bmp != null){
                imageView.setImageBitmap(bmp);
            }
            break;
        default:
            break;
    }
}
 */

public class ImageUtil {

    //-------------------------------------------------Image Selection--------------------------------------------
    public static final int GALL_CODE = 100;
    public static String filePath;
    public static final int CAM_CODE = 200;
    private static File cameraFile;
    public static Uri fileUri;

    public static void showSourcePopup(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }
        final CharSequence options[] = new CharSequence[] {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Source");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Camera")){
                    openCamera(context);
                }
                else{
                    openGallery(context);
                }
            }
        });
        builder.show();
    }

    // call this function
    public static void openGallery(Activity context) {
        // gallery code
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        // optional paramters
        /*intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);*/
        try {
            context.startActivityForResult(intent, GALL_CODE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap getGalleryImage(Activity context, Intent data) {
        if(data != null) {
            Log.e("tag", "uri: "+data.getData());
            fileUri = data.getData();
            return handleGallerySelection(context, data.getData());
        }
        return null;
    }

    private static Bitmap handleGallerySelection(Activity context, Uri uri){
        filePath = getPath(context, uri);;
        Log.e("File path", "File Path: " + filePath);
        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        bmp = bmpAfterRotationCheck(bmp, filePath);
        return bmp;
    }

    // call this function
    public static void openCamera(Activity context) {
        File extDir = Environment.getExternalStorageDirectory();
        Random r = new Random();
        String name = (r.nextInt(100000000)+r.nextInt(100000000))+".";
        cameraFile = new File(extDir, name);
        if(checkExternalStorage()){
            String text ="writing into externanal storage";
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(cameraFile);
                fos.write(text.getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(cameraFile.isFile()){
                Log.e("tag", "is file");
                Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
                context.startActivityForResult(cameraintent, CAM_CODE);
            }
            else{
                Log.e("tag", "oops not file");
            }
        }
        else{
            Log.e("tag", "External storage not available!");
        }
    }

    public static boolean checkExternalStorage() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    private static Bitmap bmpAfterRotationCheck(Bitmap bmp, String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.e("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true); // rotating bitmap
            return bmp;
        }
        catch (Exception e) {
        }
        return bmp;
    }

    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri){
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static Bitmap getCameraImage(Activity context, Intent data) {
        if(data != null) {
            Uri uri = null;
            if (data.getData() == null) {
                //from camera
                if(cameraFile == null) {
                    Log.e("tag", "Cannot fetch photo!");
                    return null;
                }
                uri = Uri.fromFile(cameraFile);
            }
            else{
                uri = data.getData();
            }
            fileUri = uri;
            return handleCameraSelection(context, uri);
        }
        else{
            Uri uri = null;
            if(cameraFile == null) {
                Log.e("tag", "unable to fetch photo!");
                return null;
            }
            uri = Uri.fromFile(cameraFile);
            fileUri = uri;
            return handleCameraSelection(context, uri);
        }
    }

    private static Bitmap handleCameraSelection(Activity context, Uri uri){
        Log.e("tag", "uri: "+uri);
        filePath = getPath(context, uri);
        Log.e("File path", "File Path: " + filePath);
        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        bmp = bmpAfterRotationCheck(bmp, filePath);
        if(bmp != null) {
            return bmp;
        }
        else{
            Log.e("tag", "Cannot retrieve photo!");
            return null;
        }
    }

    //--------------------------------------------------Cache Image-------------------------------------------------

    private static LruCache<String, Bitmap> mMemoryCache;

    private static void setMemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private static void addBitmapToCache(String key, Bitmap bitmap) {
        if(mMemoryCache == null)
            setMemoryCache();
        if(key == null)
            return;
        if (getBitmapFromCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromCache(String key) {
        if(mMemoryCache == null)
            setMemoryCache();
        if(key == null)
            return null;
        return mMemoryCache.get(key);
    }

    //------------------------------------------------Image Resolution--------------------------------------------

    public static Bitmap lessResolution(String filePath, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static String convertToBase64(Bitmap bitmapOrg) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        byte [] ba = bao.toByteArray();
        String ba1 = Base64.encodeToString(ba,Base64.DEFAULT);
        return ba1;
    }

}
