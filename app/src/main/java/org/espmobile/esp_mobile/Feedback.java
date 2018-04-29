package org.espmobile.esp_mobile;

import java.io.Serializable;
import java.util.HashMap;

public class Feedback implements Serializable {
    private String platform;
    private String feedbackRound;

    private String _1a; // "Rate our app"
    private String _1b; // "Tell us what you like about the app"
    private String _1c; // "Anything you don't like?"
    private String _1d; // "How useful do you find the app?"
    private String _1e; // "Rate the overall look-and-feel"
    private String _1f; // "Would you recommend this app to a friend?"
    private String _2a; // "How long did it take you to create an account?"
    private String _2b; // "Was creating an account intuitive?"
    private String _2c; // "What did you like about this experience?"
    private String _2d; // "Anything you did't like?"
    private String _2e; // "Rate the look-and-feel"
    private String _3a; // "Did you find it easy to see the nearest safety-zone near you?"
    private String _3b; // "Do you feel like a maximum radius of 20 miles is enough"
    private String _3c; // "Tell us about any safety-zones near you that were not on the map"
    private String _3d; // "Was there enough detail provided about each location?"
    private String _3e; // "Do you feel like the safety-zone categories 'Hospital', 'Police' and 'Fire' are enough?"
    private String _3f; // "What did you like about this experience?"
    private String _3g; // "Anything you didn't like?"
    private String _3h; // "Rate the look-and-feel"
    private String _4a; // "Was adding emergency contains intuitive?"
    private String _4b; // "Was creating alert groups from your emergency contacts intuitive?"
    private String _4c; // "What did you like about this experience?
    private String _4d; // "Anything you didn't like?"
    private String _4e; // "Rate the look-and-feel"
    private String _5a; // "Was adding custom safety-zone locations intuitive?"
    private String _5b; // "What did you like about this experience?"
    private String _5c; // "Anything you don't like?"
    private String _5d; // "Was marking locations as 'non-alertable' an intuitive and smooth experience?"
    private String _5e; // "Briefly describe your experience turing on/off alertable locations"
    private String _5f; // "Rate the look-and-feel"
    private String _6a; // "Did your emergency contacts receive a text message when you entered a safety-zone?"
    private String _6b; // "Do you have any additional feedback regarding the emergency alert functionality?"
    private String _6c; // (user's battery usage)

    public Feedback() {
        this.platform = "Android";
        this.feedbackRound = "1";
        this._1a = ""; this._1b = ""; this._1c = ""; this._1d = ""; this._1e = ""; this._1f = "";
        this._2a = ""; this._2b = ""; this._2c = ""; this._2d = ""; this._2e = "";
        this._3a = ""; this._3b = ""; this._3c = ""; this._3d = ""; this._3e = ""; this._3f = ""; this._3g = ""; this._3h = "";
        this._4a = ""; this._4b = ""; this._4c = ""; this._4d = ""; this._4e = "";
        this._5a = ""; this._5b = ""; this._5c = ""; this._5d = ""; this._5e = ""; this._5f = "";
        this._6a = ""; this._6b = ""; this._6c = "";
    }

    public Feedback(String _1a, String _1b, String _1c, String _1d, String _1e, String _1f,
        String _2a, String _2b, String _2c, String _2d, String _2e,
        String _3a, String _3b, String _3c, String _3d, String _3e, String _3f, String _3g, String _3h,
        String _4a, String _4b, String _4c, String _4d, String _4e,
        String _5a, String _5b, String _5c, String _5d, String _5e, String _5f,
        String _6a, String _6b, String _6c) {
        this.platform = "Android";
        this.feedbackRound = "1";
        this._1a = _1a; this._1b = _1b; this._1c = _1c; this._1d = _1d; this._1e = _1e; this._1f = _1f;
        this._2a = _2a; this._2b = _2b; this._2c = _2c; this._2d = _2d; this._2e = _2e;
        this._3a = _3a; this._3b = _3b; this._3c = _3c; this._3d = _3d; this._3e = _3e; this._3f = _3f; this._3g = _3g; this._3h = _3h;
        this._4a = _4a; this._4b = _4b; this._4c = _4c; this._4d = _4d; this._4e = _4e;
        this._5a = _5a; this._5b = _5b; this._5c = _5c; this._5d = _5d; this._5e = _5e; this._5f = _5f;
        this._6a = _6a; this._6b = _6b; this._6c = _6c;
    }

    public Feedback(HashMap<String, String> tabularRepresentation) {
        this.platform = "Android";
        this.feedbackRound = "1";
        this._1a = tabularRepresentation.get("1a");
        this._1b = tabularRepresentation.get("1b");
        this._1c = tabularRepresentation.get("1c");
        this._1d = tabularRepresentation.get("1d");
        this._1e = tabularRepresentation.get("1e");
        this._1f = tabularRepresentation.get("1f");
        this._2a = tabularRepresentation.get("2a");
        this._2b = tabularRepresentation.get("2b");
        this._2c = tabularRepresentation.get("2c");
        this._2d = tabularRepresentation.get("2d");
        this._2e = tabularRepresentation.get("2e");
        this._3a = tabularRepresentation.get("3a");
        this._3b = tabularRepresentation.get("3b");
        this._3c = tabularRepresentation.get("3c");
        this._3d = tabularRepresentation.get("3d");
        this._3e = tabularRepresentation.get("3e");
        this._3f = tabularRepresentation.get("3f");
        this._3g = tabularRepresentation.get("3g");
        this._3h = tabularRepresentation.get("3h");
        this._4a = tabularRepresentation.get("4a");
        this._4b = tabularRepresentation.get("4b");
        this._4c = tabularRepresentation.get("4c");
        this._4d = tabularRepresentation.get("4d");
        this._4e = tabularRepresentation.get("4e");
        this._5a = tabularRepresentation.get("5a");
        this._5b = tabularRepresentation.get("5b");
        this._5c = tabularRepresentation.get("5c");
        this._5d = tabularRepresentation.get("5d");
        this._5e = tabularRepresentation.get("5e");
        this._5f = tabularRepresentation.get("5f");
        this._6a = tabularRepresentation.get("6a");
        this._6b = tabularRepresentation.get("6b");
        this._6c = tabularRepresentation.get("6c");
    }

    public String getPlatform() {
        return platform;
    }

    public String getFeedbackRound() {
        return feedbackRound;
    }

    public String getQuestion1a() {
        return _1a;
    }

    public String getQuestion1b() {
        return _1b;
    }

    public String getQuestion1c() {
        return _1c;
    }

    public String getQuestion1d() {
        return _1d;
    }

    public String getQuestion1e() {
        return _1e;
    }

    public String getQuestion1f() {
        return _1f;
    }

    public String getQuestion2a() {
        return _2a;
    }

    public String getQuestion2b() {
        return _2b;
    }

    public String getQuestion2c() {
        return _2c;
    }

    public String getQuestion2d() {
        return _2d;
    }

    public String getQuestion2e() {
        return _2e;
    }

    public String getQuestion3a() {
        return _3a;
    }

    public String getQuestion3b() {
        return _3b;
    }

    public String getQuestion3c() {
        return _3c;
    }

    public String getQuestion3d() {
        return _3d;
    }

    public String getQuestion3e() {
        return _3e;
    }

    public String getQuestion3f() {
        return _3f;
    }

    public String getQuestion3g() {
        return _3g;
    }

    public String getQuestion3h() {
        return _3h;
    }

    public String getQuestion4a() {
        return _4a;
    }

    public String getQuestion4b() {
        return _4b;
    }

    public String getQuestion4c() {
        return _4c;
    }

    public String getQuestion4d() {
        return _4d;
    }

    public String getQuestion4e() {
        return _4e;
    }

    public String getQuestion5a() {
        return _5a;
    }

    public String getQuestion5b() {
        return _5b;
    }

    public String getQuestion5c() {
        return _5c;
    }

    public String getQuestion5d() {
        return _5d;
    }

    public String getQuestion5e() {
        return _5e;
    }

    public String getQuestion5f() {
        return _5f;
    }

    public String getQuestion6a() {
        return _6a;
    }

    public String getQuestion6b() {
        return _6b;
    }

    public String getQuestion6c() {
        return _6c;
    }

    public void setQuestion1a(String _1a) { this._1a = _1a; }

    public void setQuestion1b(String _1b) { this._1b = _1b; }

    public void setQuestion1c(String _1c) { this._1c = _1c; }

    public void setQuestion1d(String _1d) { this._1d = _1d; }

    public void setQuestion1e(String _1e) { this._1e = _1e; }

    public void setQuestion1f(String _1f) { this._1f = _1f; }

    public void setQuestion2a(String _2a) { this._2a = _2a; }

    public void setQuestion2b(String _2b) { this._2b = _2b; }

    public void setQuestion2c(String _2c) { this._2c = _2c; }

    public void setQuestion2d(String _2d) { this._2d = _2d; }

    public void setQuestion2e(String _2e) { this._2e = _2e; }

    public void setQuestion3a(String _3a) { this._3a = _3a; }

    public void setQuestion3b(String _3b) { this._3b = _3b; }

    public void setQuestion3c(String _3c) { this._3c = _3c; }

    public void setQuestion3d(String _3d) { this._3d = _3d; }

    public void setQuestion3e(String _3e) { this._3e = _3e; }

    public void setQuestion3f(String _3f) { this._3f = _3f; }

    public void setQuestion3g(String _3g) { this._3g = _3g; }

    public void setQuestion3h(String _3h) { this._3h = _3h; }

    public void setQuestion4a(String _4a) { this._4a = _4a; }

    public void setQuestion4b(String _4b) { this._4b = _4b; }

    public void setQuestion4c(String _4c) { this._4c = _4c; }

    public void setQuestion4d(String _4d) { this._4d = _4d; }

    public void setQuestion4e(String _4e) { this._4e = _4e; }

    public void setQuestion5a(String _5a) { this._5a = _5a; }

    public void setQuestion5b(String _5b) { this._5b = _5b; }

    public void setQuestion5c(String _5c) { this._5c = _5c; }

    public void setQuestion5d(String _5d) { this._5d = _5d; }

    public void setQuestion5e(String _5e) { this._5e = _5e; }

    public void setQuestion5f(String _5f) { this._5f = _5f; }

    public void setQuestion6a(String _6a) { this._6a = _6a; }

    public void setQuestion6b(String _6b) { this._6b = _6b; }

    public void setQuestion6c(String _6c) { this._6c = _6c; }


    public HashMap<String, String> tabularRepresentation(Boolean safe) {
        HashMap<String, String> tabularRepresentation = new HashMap<>();

        tabularRepresentation.put("platform", platform);
        tabularRepresentation.put("feedback_round", feedbackRound);
        tabularRepresentation.put("1a", clean(_1a, safe));
        tabularRepresentation.put("1b", clean(_1b, safe));
        tabularRepresentation.put("1c", clean(_1c, safe));
        tabularRepresentation.put("1d", clean(_1d, safe));
        tabularRepresentation.put("1e", clean(_1e, safe));
        tabularRepresentation.put("1f", clean(_1f, safe));
        tabularRepresentation.put("2a", clean(_2a, safe));
        tabularRepresentation.put("2b", clean(_2b, safe));
        tabularRepresentation.put("2c", clean(_2c, safe));
        tabularRepresentation.put("2d", clean(_2d, safe));
        tabularRepresentation.put("2e", clean(_2e, safe));
        tabularRepresentation.put("3a", clean(_3a, safe));
        tabularRepresentation.put("3b", clean(_3b, safe));
        tabularRepresentation.put("3c", clean(_3c, safe));
        tabularRepresentation.put("3d", clean(_3d, safe));
        tabularRepresentation.put("3e", clean(_3e, safe));
        tabularRepresentation.put("3f", clean(_3f, safe));
        tabularRepresentation.put("3g", clean(_3g, safe));
        tabularRepresentation.put("3h", clean(_3h, safe));
        tabularRepresentation.put("4a", clean(_4a, safe));
        tabularRepresentation.put("4b", clean(_4b, safe));
        tabularRepresentation.put("4c", clean(_4c, safe));
        tabularRepresentation.put("4d", clean(_4d, safe));
        tabularRepresentation.put("4e", clean(_4e, safe));
        tabularRepresentation.put("5a", clean(_5a, safe));
        tabularRepresentation.put("5b", clean(_5b, safe));
        tabularRepresentation.put("5c", clean(_5c, safe));
        tabularRepresentation.put("5d", clean(_5d, safe));
        tabularRepresentation.put("5e", clean(_5e, safe));
        tabularRepresentation.put("5f", clean(_5f, safe));
        tabularRepresentation.put("6a", clean(_6a, safe));
        tabularRepresentation.put("6b", clean(_6b, safe));
        tabularRepresentation.put("6c", clean(_6c, safe));

        return  tabularRepresentation;
    }

    private String clean(String s, Boolean safe) {
        if (safe) {
            return s
                    .replaceAll("&", "and")
                    .replaceAll("=","equals")
                    .replaceAll("\\?","[question mark]")
                    .replaceAll("\"", "[quote]");
        } else {
            return s;
        }
    }
}
