package wpi.ssogden.deeplearningapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * Created by samuelogden on 3/19/18.
 */

public class LocalClassifier {
    private static Classifier classifier;

    public static Result classify_picture(Context context, String model_name, String mCurrentPhotoPath) {

        return run_classifier(context, mCurrentPhotoPath,
                "file:///android_asset/" + model_name,
                "file:///android_asset/models/labels.txt",
                299,
                0,
                255.0f,
                "Mul",
                "final_result");
    }

    public static Result run_classifier(Context context, String mCurrentPhotoPath, String model_file, String label_file, int input_size, int image_mean, float image_std, String input_name, String output_name) {

        long startTotalTime = System.currentTimeMillis();

        //Init Classifier
        long startTime_loading = System.currentTimeMillis();
        classifier =
                TensorFlowImageClassifier.create(context.getAssets(), model_file, label_file, input_size, image_mean, image_std, input_name, output_name);
        long loadingDuration = System.currentTimeMillis() - startTime_loading;

        // Preprocess image
        long startPreprocess = System.currentTimeMillis();

        Bitmap bMap = BitmapFactory.decodeFile(mCurrentPhotoPath); //context.getAssets().open(in_jpeg));

        long preprocessDuration = System.currentTimeMillis() - startPreprocess;

        // Run inference
        long startTime_inference = System.currentTimeMillis();
        final List<Classifier.Recognition> results = classifier.recognizeImage(bMap);
        long inferenceDuration = System.currentTimeMillis() - startTime_inference;

        double total_time = System.currentTimeMillis() - startTotalTime;
        Log.v("load time", String.valueOf(loadingDuration));
        Log.v("infer time", String.valueOf(inferenceDuration));
        Log.v("total time", String.valueOf(total_time));

        return new Result(results.get(0).getTitle(), results.get(0).getConfidence(), loadingDuration, preprocessDuration, inferenceDuration, (float) total_time, model_file);
    }
}
