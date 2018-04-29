package org.espmobile.esp_mobile.feedback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.ParsedRequestListener;

import org.espmobile.esp_mobile.ESPMobileAPI;
import org.espmobile.esp_mobile.Feedback;
import org.espmobile.esp_mobile.MessageBanner;
import org.espmobile.esp_mobile.R;
import org.json.JSONObject;

import java.util.Stack;

import static android.app.Activity.RESULT_FIRST_USER;

public class FeedbackBottomButtonFragment extends Fragment {
    public static FeedbackBottomButtonFragment newInstance(String button_title, int next_feedback_page) {
        FeedbackBottomButtonFragment f = new FeedbackBottomButtonFragment();
        Bundle args = new Bundle();
        args.putString("button_title", button_title);
        args.putInt("next_feedback_page", next_feedback_page);
        f.setArguments(args);
        return f;
    }

    static Stack<Activity> activity_stack = new Stack<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback_bottom_button, container, false);
        final Button b = v.findViewById(R.id.feedback_bottom_button);
        String button_text = getArguments().getString("button_title");
        b.setText(button_text);
        b.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent feedbackIntent;
                Feedback cumulativeFeedback= null;
                int next_feedback_page = getArguments().getInt("next_feedback_page");
                switch (next_feedback_page) {
                    case 2:
                        cumulativeFeedback = ((ProvideFeedbackPage1Activity) getActivity()).recordFeedback();
                        feedbackIntent = new Intent(getContext(), ProvideFeedbackPage2Activity.class);
                        feedbackIntent.putExtra("feedback", cumulativeFeedback);
                        break;
                    case 3:
                        cumulativeFeedback = ((ProvideFeedbackPage2Activity) getActivity()).recordFeedback();
                        feedbackIntent = new Intent(getContext(), ProvideFeedbackPage3Activity.class);
                        feedbackIntent.putExtra("feedback", cumulativeFeedback);
                        break;
                    case 4:
                        cumulativeFeedback = ((ProvideFeedbackPage3Activity) getActivity()).recordFeedback();
                        feedbackIntent = new Intent(getContext(), ProvideFeedbackPage4Activity.class);
                        feedbackIntent.putExtra("feedback", cumulativeFeedback);
                        break;
                    case 5:
                        cumulativeFeedback = ((ProvideFeedbackPage4Activity) getActivity()).recordFeedback();
                        feedbackIntent = new Intent(getContext(), ProvideFeedbackPage5Activity.class);
                        feedbackIntent.putExtra("feedback", cumulativeFeedback);
                        break;
                    case 6:
                        cumulativeFeedback = ((ProvideFeedbackPage5Activity) getActivity()).recordFeedback();
                        feedbackIntent = new Intent(getContext(), ProvideFeedbackPage6Activity.class);
                        feedbackIntent.putExtra("feedback", cumulativeFeedback);
                        break;
                    case 7:
                        cumulativeFeedback = ((ProvideFeedbackPage6Activity) getActivity()).recordFeedback();
                        feedbackIntent = new Intent(getContext(), ProvideFeedbackFinishedActivity.class);
                        break;
                    default: feedbackIntent = null;
                }

                if (feedbackIntent != null) {
                    if (!activity_stack.contains(getActivity())) {
                        activity_stack.push(getActivity());
                    }

                    if (next_feedback_page == 7) {
                        if (cumulativeFeedback != null) {
                            b.setEnabled(false);
                            b.setAlpha((float) 0.75);
                            getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.save_button).setEnabled(false);
                            getActivity().findViewById(R.id.save_button).setAlpha((float) 0.5);
                            ESPMobileAPI.sendFeedback(getActivity(), cumulativeFeedback, new ParsedRequestListener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    startActivity(feedbackIntent);
                                    while (!activity_stack.empty()) {
                                        activity_stack.peek().finish();
                                        activity_stack.pop();
                                    }

                                    SharedPreferences.Editor ed = getActivity().getSharedPreferences("feedback", Context.MODE_PRIVATE).edit();
                                    ed.putBoolean("feedback_received", true);
                                    ed.apply();
                                }

                                @Override
                                public void onError(ANError anError) {
                                    b.setEnabled(true);
                                    b.setAlpha(1);
                                    getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    getActivity().findViewById(R.id.save_button).setEnabled(true);
                                    getActivity().findViewById(R.id.save_button).setAlpha(1);
                                    MessageBanner.show(getActivity(), R.string.error_message, R.color.espRed);
                                }
                            });
                        }
                    } else {
                        getActivity().setResult(RESULT_FIRST_USER, feedbackIntent);
                        getActivity().startActivityForResult(feedbackIntent, RESULT_FIRST_USER);
                    }
                }
            }
        });

        if (button_text != null && !button_text.equals("Next Page")) {
            b.setTypeface(null, Typeface.BOLD);
            b.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            5,
                            getResources().getDisplayMetrics()
                    )
            );
            b.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            50,
                            getResources().getDisplayMetrics()
                    )
            ));
        }

        return v;
    }
}