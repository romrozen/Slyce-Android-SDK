package com.android.slyce;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.slyce.enums.SlyceRequestStage;
import com.android.slyce.listeners.OnSlyceCameraListener;
import org.json.JSONArray;
import org.json.JSONObject;

public class CameraActivity extends Activity implements OnSlyceCameraListener, View.OnClickListener {

    private SlyceCamera slyceCamera;
    private Button snap;
    private Button flash;
    private Button focuseAtPoint;

    private SurfaceView preview;

    private ProgressBar snapProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        initViews();

        slyceCamera = new SlyceCamera(this, Slyce.getInstance(this), preview, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        slyceCamera.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        slyceCamera.stop();
    }

    private void initViews(){

        preview = (SurfaceView) findViewById(R.id.preview);

        snap = (Button) findViewById(R.id.snap);
        snap.setOnClickListener(this);

        flash = (Button) findViewById(R.id.flash);
        flash.setOnClickListener(this);

        focuseAtPoint = (Button) findViewById(R.id.focus_at_point);
        focuseAtPoint.setOnClickListener(this);

        snapProgress = (ProgressBar) findViewById(R.id.snap_progress);
    }

    /* OnSlyceCameraListener */
    @Override
    public void onCameraBarcodeDetected(SlyceBarcode barcode) {

        Toast.makeText(this,
                        "onCameraBarcodeDetected:" + "\n" +
                        "Barcode Type: " + barcode.getType() + "\n" +
                        "Barcode: " + barcode.getBarcode(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraImageDetected(String productInfo) {

        Toast.makeText(this,
                "onCameraImageDetected:" + "\n" +
                        "Product Info: " + productInfo, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraImageInfoReceived(JSONArray products) {

        Toast.makeText(this,
                "onCameraImageInfoReceived:" +
                        "\n" + "Products: " + products, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraSlyceProgress(long progress, String message, String token) {

        Toast.makeText(this,
                "onCameraSlyceProgress: " + progress +
                        "\n" + "Message: " + message +
                        "\n" + "Token: " + token, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraSlyceRequestStage(SlyceRequestStage message) {

        Toast.makeText(this, "onCameraSlyceRequestStage:" + "\n" + "Message: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraResultsReceived(JSONObject products) {

        Toast.makeText(this, "onCameraResultsReceived:" +  "\n" + "Products: " + products, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSlyceCameraError(String message) {

        snapProgress.setVisibility(View.INVISIBLE);

        Toast.makeText(this, "onSlyceCameraError: " + "\n" + "Message: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTap(float x, float y) {

        Toast.makeText(this, "onTap:" + "\n" + "X: " + x + "\n" + "Y:" + y, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraPreviewMode(boolean front) {

    }

    @Override
    public void onSnap(Bitmap bitmap) {

        Toast.makeText(this, "onSnap:" + "\n" + "Width: " + bitmap.getWidth() + "\n" + "Height:" + bitmap.getHeight(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraFinished() {

        Toast.makeText(this, "onCameraFinished", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraBarcodeInfoReceived(JSONObject products) {
        Toast.makeText(this, "onCameraBarcodeInfoReceived: " + "\n" + "Products: " + products, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.snap:

                slyceCamera.snap();
                snapProgress.setVisibility(View.VISIBLE);

                break;

            case R.id.flash:

                slyceCamera.turnFlash();

                break;

            case R.id.focus_at_point:

                slyceCamera.focusAtPoint(250, 250);

                break;
        }
    }
}
