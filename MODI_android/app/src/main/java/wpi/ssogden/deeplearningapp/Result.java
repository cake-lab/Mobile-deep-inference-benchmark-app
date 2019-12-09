package wpi.ssogden.deeplearningapp;

public class Result {
    public final String result;
    public final String load_time;
    public final String preprocess_time;
    public final String inference_time;
    public final String total_time;
    public final String confidence;
    public final String model;
    public Result(String result, float confidence, float load_time, float preprocess_time, float inference_time, float total_time, String model) {
        this.result = result;
        this.confidence = String.valueOf(confidence);
        this.load_time = String.valueOf(load_time);
        this.preprocess_time = String.valueOf(preprocess_time);
        this.inference_time = String.valueOf(inference_time);
        this.total_time = String.valueOf(total_time);
        this.model = model;
    }
    public Result(String result, String confidence, String load_time, String preprocess_time, String inference_time, String total_time, String model) {
        this.result = result;
        this.confidence = String.valueOf(confidence);
        this.load_time = String.valueOf(load_time);
        this.preprocess_time = String.valueOf(preprocess_time);
        this.inference_time = String.valueOf(inference_time);
        this.total_time = String.valueOf(total_time);
        this.model = model;
    }
    public Result() {
        this.result = "";
        this.confidence = "";
        this.load_time = "";
        this.preprocess_time = "";
        this.inference_time = "";
        this.total_time = "";
        this.model = "";
    }
}
