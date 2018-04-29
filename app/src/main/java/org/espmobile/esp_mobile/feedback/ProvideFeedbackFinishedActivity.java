package org.espmobile.esp_mobile.feedback;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.Feedback;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.safety_zones.SafetyZonesActivity;
import org.json.JSONObject;

public class ProvideFeedbackFinishedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide_feedback_finished);
    }

    public void exit(View v) {
        Intent returnIntent = new Intent(this, SafetyZonesActivity.class);
        returnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(returnIntent);
        finish();
    }
}
