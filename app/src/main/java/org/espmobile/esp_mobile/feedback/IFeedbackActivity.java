package org.espmobile.esp_mobile.feedback;

import android.view.View;

import org.espmobile.esp_mobile.Feedback;

public interface IFeedbackActivity {
    Feedback recordFeedback();
    void saveAndExit(View v);
}
