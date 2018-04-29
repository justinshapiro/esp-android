package org.espmobile.esp_mobile.feedback;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.espmobile.esp_mobile.R;

public class FeedbackSliderFragment extends Fragment implements IFeedbackFragment {
    public static FeedbackSliderFragment newInstance(String question_title, Boolean alternate_config) {
        FeedbackSliderFragment f = new FeedbackSliderFragment();
        Bundle args = new Bundle();
        args.putString("question_title", question_title);
        args.putBoolean("alternate_config", alternate_config);
        f.setArguments(args);
        return f;
    }

    SeekBar seekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback_slider, container, false);

        TextView title = v.findViewById(R.id.slider_title);
        title.setText(getArguments().getString("question_title"));

        final TextView t = v.findViewById(R.id.slider_value_text);
        seekBar = v.findViewById(R.id.feedback_slider);

        if (getArguments().getBoolean("alternate_config")) {
            seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color.espRed), PorterDuff.Mode.SRC_IN);

            seekBar.setMin(0);
            seekBar.setMax(100);
            seekBar.setProgress(0);

            title.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP,
                            4.5f,
                            getResources().getDisplayMetrics()
                    )
            );

            title.setTypeface(null, Typeface.NORMAL);
            v.findViewById(R.id.feedback_slider_sep).setVisibility(View.VISIBLE);
            v.findViewById(R.id.feedback_slider_sep2).setVisibility(View.VISIBLE);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    String progress_text = Integer.toString(i) + "%";
                    t.setText(progress_text);
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        } else {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    String progress_text = Integer.toString(i + 1) + " out of 10";
                    t.setText(progress_text);
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        return v;
    }

    public String getData() {
        return Integer.toString(seekBar.getProgress() + 1);
    }
}