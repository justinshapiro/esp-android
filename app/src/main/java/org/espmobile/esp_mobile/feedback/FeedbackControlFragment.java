package org.espmobile.esp_mobile.feedback;

import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.espmobile.esp_mobile.R;

public class FeedbackControlFragment extends Fragment implements IFeedbackFragment {
    public static FeedbackControlFragment newInstance(String question_title, String[] button_names) {
        FeedbackControlFragment f = new FeedbackControlFragment();
        Bundle args = new Bundle();
        args.putString("question_title", question_title);
        args.putStringArray("button_names", button_names);
        f.setArguments(args);
        return f;
    }

    Button[] buttons;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback_control, container, false);

        ((TextView) v.findViewById(R.id.yes_no_title)).setText(getArguments().getString("question_title"));

        final String[] button_names = getArguments().getStringArray("button_names");

        if (button_names != null) {
            buttons = new Button[button_names.length];

            for (int i = 0; i < button_names.length; i++) {
                Button b = new Button(getContext());
                b.setText(button_names[i]);
                b.setTransformationMethod(null);
                buttons[i] = b;
            }

            for (int i = 0; i < buttons.length; i++) {
                final int curr_i = i;

                if (i == 0) {
                    buttons[i].setTypeface(null, Typeface.BOLD);
                    buttons[i].setBackgroundResource(R.drawable.rounded_button_orange);
                } else {
                    buttons[i].setTypeface(null, Typeface.NORMAL);
                    buttons[i].setBackgroundResource(R.drawable.rounded_button_white);
                }

                buttons[i].setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buttons[curr_i].setTypeface(null, Typeface.BOLD);
                        buttons[curr_i].setBackgroundResource(R.drawable.rounded_button_orange);

                        for (int j = 0; j < buttons.length; j++) {
                            if (j != curr_i) {
                                buttons[j].setTypeface(null, Typeface.NORMAL);
                                buttons[j].setBackgroundResource(R.drawable.rounded_button_white);
                            }
                        }
                    }
                });
            }
        }

        LinearLayout button_container = v.findViewById(R.id.button_container);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                getResources().getDisplayMetrics()
        );

        lp.setMargins(margin, 0, margin, 0);
        lp.weight = (float) 1 /  (float) buttons.length;

        for (Button b : buttons) {
            b.setLayoutParams(lp);
            button_container.addView(b);
        }

        return v;
    }

    public String getData() {
        String buttonResult = null;
        for (Button button : buttons) {
            Typeface buttonTypeface = button.getTypeface(); // for a lack of a better key
            if (buttonTypeface != null && buttonTypeface.isBold()) {
                buttonResult = button.getText().toString();
                break;
            }
        }

        return buttonResult;
    }
}