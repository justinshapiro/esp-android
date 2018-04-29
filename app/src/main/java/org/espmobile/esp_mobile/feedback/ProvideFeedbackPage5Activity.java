package org.espmobile.esp_mobile.feedback;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import org.espmobile.esp_mobile.Feedback;
import org.espmobile.esp_mobile.R;
import org.espmobile.esp_mobile.safety_zones.SafetyZonesActivity;

import java.util.List;

public class ProvideFeedbackPage5Activity extends AppCompatActivity implements IFeedbackActivity {

    Feedback cumulativeFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide_feedback_page5);

        Toolbar toolbar = findViewById(R.id.feedback_page5_toolbar);
        setSupportActionBar(toolbar);

        ActionBar currentActionBar = getSupportActionBar();
        if (currentActionBar != null) {
            currentActionBar.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayout page2Container = findViewById(R.id.page5_container);

        getFragmentManager().beginTransaction().add(
                page2Container.getId(),
                FeedbackControlFragment.newInstance(
                        getResources().getString(R.string._5a),
                        new String[]{"Yes", "No"}
                ),
                "5a"
        ).commit();
        getFragmentManager().beginTransaction().add(
                page2Container.getId(),
                FeedbackTextEntryFragment.newInstance(getResources().getString(R.string._5b)),
                "5b"
        ).commit();
        getFragmentManager().beginTransaction().add(
                page2Container.getId(),
                FeedbackTextEntryFragment.newInstance(getResources().getString(R.string._5c)),
                "5c"
        ).commit();
        getFragmentManager().beginTransaction().add(
                page2Container.getId(),
                FeedbackControlFragment.newInstance(
                        getResources().getString(R.string._5d),
                        new String[]{"Yes", "No"}
                ),
                "5d"
        ).commit();
        getFragmentManager().beginTransaction().add(
                page2Container.getId(),
                FeedbackTextEntryFragment.newInstance(getResources().getString(R.string._5e)),
                "5e"
        ).commit();
        getFragmentManager().beginTransaction().add(
                page2Container.getId(),
                FeedbackSliderFragment.newInstance(
                        getResources().getString(R.string._5f),
                        false
                ),
                "5f"
        ).commit();
        getFragmentManager().beginTransaction().add(
                page2Container.getId(),
                FeedbackBottomButtonFragment.newInstance(
                        "Next Page",
                        6
                ),
                "bottom_button"
        ).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data.getSerializableExtra("feedback") != null) {
            cumulativeFeedback = (Feedback) getIntent().getSerializableExtra("feedback");
        }
    }

    public void saveAndExit(View v) {
        recordFeedback();
        Intent exitIntent = new Intent(this, SafetyZonesActivity.class);
        exitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(exitIntent);
        while (!FeedbackBottomButtonFragment.activity_stack.empty()) {
            FeedbackBottomButtonFragment.activity_stack.peek().finish();
            FeedbackBottomButtonFragment.activity_stack.pop();
        }

        finish();
    }

    public Feedback recordFeedback() {
        cumulativeFeedback = (Feedback) getIntent().getSerializableExtra("feedback");
        if (cumulativeFeedback == null) {
            cumulativeFeedback = new Feedback();
        }

        List<Fragment> allFragments = getFragmentManager().getFragments();
        for (int i = 0; i < allFragments.size(); i++) {
            switch (i) {
                case 0: break; // the first fragment is system-related, not user created
                case 1:
                    cumulativeFeedback.setQuestion5a(((IFeedbackFragment) allFragments.get(i)).getData());
                    break;
                case 2:
                    cumulativeFeedback.setQuestion5b(((IFeedbackFragment) allFragments.get(i)).getData());
                    break;
                case 3:
                    cumulativeFeedback.setQuestion5c(((IFeedbackFragment) allFragments.get(i)).getData());
                    break;
                case 4:
                    cumulativeFeedback.setQuestion5d(((IFeedbackFragment) allFragments.get(i)).getData());
                    break;
                case 5:
                    cumulativeFeedback.setQuestion5e(((IFeedbackFragment) allFragments.get(i)).getData());
                    break;
                case 6:
                    cumulativeFeedback.setQuestion5f(((IFeedbackFragment) allFragments.get(i)).getData());
                    break;
                default: break;
            }
        }

        return cumulativeFeedback;
    }
}
