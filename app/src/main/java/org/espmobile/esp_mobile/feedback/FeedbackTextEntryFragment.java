package org.espmobile.esp_mobile.feedback;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.espmobile.esp_mobile.R;

public class FeedbackTextEntryFragment extends Fragment implements IFeedbackFragment {
    public static FeedbackTextEntryFragment newInstance(String question_title) {
        FeedbackTextEntryFragment f = new FeedbackTextEntryFragment();
        Bundle args = new Bundle();
        args.putString("text_entry_title", question_title);
        f.setArguments(args);
        return f;
    }

    TextView textBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback_text_entry, container, false);
        ((TextView) v.findViewById(R.id.slider_title)).setText(getArguments().getString("text_entry_title"));
        textBox = v.findViewById(R.id.feedback_text_field);
        return v;
    }

    public String getData() {
        return textBox.getText().toString();
    }
}