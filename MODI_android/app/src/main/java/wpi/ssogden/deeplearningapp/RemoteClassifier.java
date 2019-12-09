package wpi.ssogden.deeplearningapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import wpi.ssogden.deeplearningapp.LocalClassifier;
import wpi.ssogden.deeplearningapp.Result;


import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

/**
 * Created by samuelogden on 3/20/18.
 */

public class RemoteClassifier {
    private static String server_ip = "127.0.0.1"; // Add your IP Here!
    
    private static String server_url = "http://" + server_ip + "/infer";

    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    public static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    private static OkHttpClient client = new OkHttpClient();

    //protected final OkHttpClient client = new OkHttpClient();


    public static Result classify_picture(Context context, String model_name, String mCurrentPhotoPath) {
        Result result = new Result();
        try {
            long startTime = System.currentTimeMillis();
            String response_str = doPostRequest(context, mCurrentPhotoPath);
            Log.d("response", response_str);
            String[] results = response_str.split(" ");
            double total_time = (System.currentTimeMillis() - startTime);
            result = new Result(results[1], results[2], results[3], results[4], results[5], String.valueOf(total_time), "remote."+results[0]);
            Log.v("total time", String.valueOf(total_time));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected static String doPostRequest(Context context, String mCurrentPhotoPath) throws IOException {

        String filename = "test_picture.png";
        File file = new File(mCurrentPhotoPath);

        //writeBytesToFile(context.getAssets().open(mCurrentPhotoPath), file);

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", filename, RequestBody.create(MEDIA_TYPE_JPEG, file))
                .build();

        Request request = new Request.Builder()
                .url(server_url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static void writeBytesToFile(InputStream is, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            byte[] data = new byte[2048];
            int nbread = 0;
            fos = new FileOutputStream(file);
            while ((nbread = is.read(data)) > -1) {
                fos.write(data, 0, nbread);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}
