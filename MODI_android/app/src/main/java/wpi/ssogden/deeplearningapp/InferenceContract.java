package wpi.ssogden.deeplearningapp;

import android.provider.BaseColumns;

/**
 * Created by samuelogden on 3/14/18.
 */

public class InferenceContract {
    private InferenceContract() {

    }
    public static class InferenceEntry implements BaseColumns {
        public static final String TABLE_NAME = "inference_results";
        public static final String COLUMN_NAME_CURRTIME = "curr_time";
        public static final String COLUMN_NAME_MODEL = "model_path";
        public static final String COLUMN_NAME_LOADTIME = "load_time";
        public static final String COLUMN_NAME_PREPROCESSTIME = "preprocess_time";
        public static final String COLUMN_NAME_INFERTIME = "infer_time";
        public static final String COLUMN_NAME_TOTALTIME = "total_time";
        public static final String COLUMN_NAME_INFERANSWER = "infer_answer";
        public static final String COLUMN_NAME_TRUEANSWER = "true_answer";
        public static final String COLUMN_NAME_CONFIDENCE = "confidence";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_BATTERY = "battery_level";
    }

    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + InferenceEntry.TABLE_NAME + " (" +
                    InferenceEntry._ID + " INTEGER PRIMARY KEY," +
                    InferenceEntry.COLUMN_NAME_CURRTIME + " TEXT DEFAULT ''," +
                    InferenceEntry.COLUMN_NAME_MODEL + " TEXT DEFAULT ''," +
                    InferenceEntry.COLUMN_NAME_LOADTIME + " TEXT DEFAULT ''," +
                    InferenceEntry.COLUMN_NAME_PREPROCESSTIME + " TEXT DEFAULT ''," +
                    InferenceEntry.COLUMN_NAME_INFERTIME + " TEXT DEFAULT ''," +
                    InferenceEntry.COLUMN_NAME_TOTALTIME + " TEXT DEFAULT ''," +
                    InferenceEntry.COLUMN_NAME_INFERANSWER + " TEXT DEFAULT ''," +
                    InferenceEntry.COLUMN_NAME_TRUEANSWER + " TEXT DEFAULT ''," +
                    InferenceEntry.COLUMN_NAME_CONFIDENCE + " TEXT DEFAULT '',"+
                    InferenceEntry.COLUMN_NAME_LOCATION + " TEXT DEFAULT '',"+
                    InferenceEntry.COLUMN_NAME_BATTERY + " TEXT DEFAULT '')";

    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + InferenceEntry.TABLE_NAME;
}
