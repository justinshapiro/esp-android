package org.espmobile.esp_mobile.feedback;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import org.espmobile.esp_mobile.R;

public class FeedbackRatingFragment extends Fragment implements IFeedbackFragment {
    public static FeedbackRatingFragment newInstance(String question_title) {
        FeedbackRatingFragment f = new FeedbackRatingFragment();
        Bundle args = new Bundle();
        args.putString("rating_title", question_title);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback_rating, container, false);
        ((TextView) v.findViewById(R.id.rating_title)).setText(getArguments().getString("rating_title"));
        return v;
    }

    public String getData() {
        return Integer.toString(((int) ((RatingBar) getActivity().findViewById(R.id.ratingBar)).getRating()));
    }
}