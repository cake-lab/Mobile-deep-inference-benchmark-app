package wpi.ssogden.deeplearningapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static wpi.ssogden.deeplearningapp.R.*;

public class MobileInference extends AppCompatActivity{

    private double battery_capacity = 2700;

    private final int MODEL_SIZE = 299;

    private String[] preprocessedImages = {};

    SQLiteDatabase inference_db;



    double total_misc_time = 0.0;
    double total_load_time = 0.0;
    double total_infer_time = 0.0;


    private class TestModelInBackground extends AsyncTask<String, Float, Long>  {
        String model_text;
        protected void onPreExecute() {
            Spinner model_spinner = (Spinner) findViewById(id.model_spinner);
            model_text = model_spinner.getSelectedItem().toString();


            ContentValues values = new ContentValues();
            values.put(InferenceContract.InferenceEntry.COLUMN_NAME_MODEL, model_text);
            values.put(InferenceContract.InferenceEntry.COLUMN_NAME_BATTERY, getBattery());
            values.put(InferenceContract.InferenceEntry.COLUMN_NAME_CURRTIME, (System.currentTimeMillis()/1000));

            inference_db.insert(InferenceContract.InferenceEntry.TABLE_NAME, "", values);
        }

        protected Long doInBackground(String... test_files) {
            //if (model_text == "remote") {
            //    RemoteClassifier.classify_picture(getApplicationContext(), model_text, test_files[0]);
            //} else {

            TextView locationTextview = (TextView) findViewById(id.location_textview);
            String location = locationTextview.getText().toString();

            if (preprocessedImages.length == 0) {
                preprocessedImages = new String[test_files.length];
                for (int i = 0; i < test_files.length; i++) {
                    preprocessedImages[i] = preprocessPicture(getApplicationContext(), test_files[i]);
                    publishProgress(((i / (float) test_files.length) * 100));
                }
            }
            for (int i = 0; i < preprocessedImages.length; i++) {
                TestModelOnPicture(getApplicationContext(), model_text, test_files[i], preprocessedImages[i], location);
                publishProgress(((i / (float) preprocessedImages.length) * 100));
            }

            Log.v("Load Time", String.valueOf((total_load_time / preprocessedImages.length)));
            Log.v("Infer Time", String.valueOf((total_infer_time / preprocessedImages.length)));
            Log.v("Misc Time", String.valueOf((total_misc_time / preprocessedImages.length)));

            return null;
        }

        private String getClass(String filepath){
            return filepath.split("/")[0];
        }

        protected void onProgressUpdate(Float... progress){
            updateProgress(String.format("%.02f", progress[0]) + "%");
        }

        protected void onPostExecute(Long result){
            //InferenceDbHelper inference_db_helper = new InferenceDbHelper(getApplicationContext());
            //SQLiteDatabase inference_db = inference_db_helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InferenceContract.InferenceEntry.COLUMN_NAME_MODEL, model_text);
            values.put(InferenceContract.InferenceEntry.COLUMN_NAME_BATTERY, getBattery());
            inference_db.insert(InferenceContract.InferenceEntry.TABLE_NAME, "", values);
            values.put(InferenceContract.InferenceEntry.COLUMN_NAME_CURRTIME, (System.currentTimeMillis()/1000));
            updateProgress("Done!");



        }

    }

    protected void updateProgress(String progress){

        TextView progressText = (TextView)findViewById(id.progressText);
        progressText.setText(progress);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_mobile_inference);

        Spinner model_spinner = (Spinner) findViewById(id.model_spinner);
        String[] models = getModelNames();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, models);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        model_spinner.setAdapter(dataAdapter);
        updateProgress("Ready");

        isStoragePermissionGranted();

        InferenceDbHelper inference_db_helper = new InferenceDbHelper(getApplicationContext());
        inference_db = inference_db_helper.getWritableDatabase();

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("permissions","Permission is granted");
                return true;
            } else {

                Log.v("permissions","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("permissions","Permission is granted");
            return true;
        }
    }

    public void clickGoButton(View view) {
        Spinner model_spinner = (Spinner) findViewById(id.model_spinner);
        String model_text = model_spinner.getSelectedItem().toString();
        Log.v("Action", "Go!");
        Log.v("spinner", model_text);

        String base_dir = "test_images";

        new TestModelInBackground().execute(getFiles(base_dir));

    }


    protected void TestModelOnPicture(Context context, String model_name, String true_answer, String img_path, String location){
        Log.d("img_path", img_path);
        //InferenceDbHelper inference_db_helper = new InferenceDbHelper(context);
        //SQLiteDatabase inference_db = inference_db_helper.getWritableDatabase();

        Result this_result;

        if (model_name == "remote") {
            this_result = RemoteClassifier.classify_picture(context, model_name, img_path);
        } else {
            this_result = LocalClassifier.classify_picture(context, model_name, img_path);
        }


        total_load_time += Float.parseFloat(this_result.load_time);
        total_infer_time += Float.parseFloat(this_result.inference_time);
        total_misc_time += (Float.parseFloat(this_result.total_time) - ( Float.parseFloat(this_result.inference_time) + Float.parseFloat(this_result.load_time) ));

        ContentValues values = new ContentValues();
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_MODEL, this_result.model);
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_LOADTIME, this_result.load_time);
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_PREPROCESSTIME, this_result.preprocess_time);
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_INFERTIME, this_result.inference_time);
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_TOTALTIME, this_result.total_time);
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_INFERANSWER, this_result.result);
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_CONFIDENCE, this_result.confidence);
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_TRUEANSWER, true_answer);
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_LOCATION, location);
        //values.put(InferenceContract.InferenceEntry.COLUMN_NAME_BATTERY, getBattery());
        inference_db.insert(InferenceContract.InferenceEntry.TABLE_NAME, "", values);

    }
    private String[] getModelNames() {
        String[] dirs = getFiles("models");
        String[] models = new String[dirs.length + 1];
        models[0] = "remote";
        for (int i = 0; i<dirs.length; i++) {
            models[i+1] = dirs[i];
        }
        return models;
    }
    private String[] getTestFiles(){
        return getFiles("test_images");
    }

    private String[] getFirstNFiles(String base_dir, int n) {
        //String base_dir = "test_images";

        ArrayList<String> file_list = new ArrayList<String>();

        String [] dirs;
        try {
            dirs = getAssets().list(base_dir);
            for (String directory : dirs) {
                String this_path = base_dir + "/" + directory;
                String [] files = getAssets().list(this_path);
                int num_files_read = 0;
                for (String picture_file : files) {
                    file_list.add(base_dir + "/" + directory  + "/" + picture_file);
                    num_files_read++;
                    if (num_files_read >= n) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file_list.toArray(new String[0]);
    }

    private String[] getFiles(String base_dir) {
        //String base_dir = "test_images";

        ArrayList<String> file_list = new ArrayList<String>();

        String [] dirs;
        try {
            dirs = getAssets().list(base_dir);
            for (String directory : dirs) {
                String this_path = base_dir + "/" + directory;
                String [] files = getAssets().list(this_path);
                for (String picture_file : files) {
                    file_list.add(base_dir + "/" + directory  + "/" + picture_file);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file_list.toArray(new String[0]);
    }

    private double getBattery() {
        BatteryManager mBatteryManager = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);
        int level = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        double mAh = (battery_capacity * level * 0.01);
        return mAh;
    }

    private String preprocessPicture(Context context, String in_jpeg) {
        long startTime_preprocess = System.currentTimeMillis();

        String processed;
        String basename =  in_jpeg.substring(in_jpeg.lastIndexOf('/'), in_jpeg.lastIndexOf('.')); //FilenameUtils.removeExtension(jpeg_file);
        try {
            Bitmap bMap = BitmapFactory.decodeStream(context.getAssets().open(in_jpeg));
            processed = saveBitmapToExternal(Bitmap.createScaledBitmap(bMap, MODEL_SIZE, MODEL_SIZE, false), basename);
        } catch (IOException e) {
            e.printStackTrace();
            processed = "";
        }

        long preprocess_time = System.currentTimeMillis() - startTime_preprocess;

        //InferenceDbHelper inference_db_helper = new InferenceDbHelper(context);
        //SQLiteDatabase inference_db = inference_db_helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_MODEL, "preprocess");
        values.put(InferenceContract.InferenceEntry.COLUMN_NAME_PREPROCESSTIME, preprocess_time);
        inference_db.insert(InferenceContract.InferenceEntry.TABLE_NAME, "", values);

        return processed;
    }

    private String saveBitmapToExternal(Bitmap finalBitmap, String filename_base) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/preprocessedImages");
        myDir.mkdirs();
        String fname = myDir + filename_base +".jpg";
        File file = new File (fname);
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v("Filesize:", String.valueOf(file.length()));
        return fname;
    }

}
