package com.android.slyce.listeners;

import android.graphics.Bitmap;

import com.android.slyce.models.SlyceBarcode;

import org.json.JSONArray;

/**
 * Created by davidsvilem on 5/17/15.
 */
public interface OnSlyceCameraFragmentListener {

    /* Standart/Premium */
    void onCameraFragment3DRecognition(JSONArray products);
    void onCameraFragmentBarcodeRecognition(SlyceBarcode barcode);

    /* Premium */
    void onCameraFragment2DRecognition(String irId, String productInfo);
    void onCameraFragment2DExtendedRecognition(JSONArray products);

    /* Error */
    void onSlyceCameraFragmentError(String message);
}
