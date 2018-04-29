package org.espmobile.esp_mobile;

import android.app.Activity;

import com.tapadoo.alerter.Alerter;

/*
This class is meant to be used for message banners throughout the app.
To use, just add code like this anywhere:
MessageBanner.show(this, R.string.some_string, R.color.espRed, R.drawable.info_icon);
 */

public final class MessageBanner {
    public static void show(Activity activity, int textResource, int color, int icon) {
        createAlert(activity, textResource, color)
                .setIcon(icon)
                .show();
    }

    public static void show(Activity activity, int textResource, int color) {
        createAlert(activity, textResource, color).show();
    }

    private static Alerter createAlert(Activity activity, int textResource, int color) {
        return Alerter.create(activity)
                .enableIconPulse(false)
                .setText(textResource)
                .setBackgroundColorRes(color)
                .enableSwipeToDismiss()
                .setIconColorFilter(0)
                .setTextAppearance(R.style.AlertText)
                .setDuration(1000);
    }
}
