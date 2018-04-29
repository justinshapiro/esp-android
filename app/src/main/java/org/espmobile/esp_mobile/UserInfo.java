package org.espmobile.esp_mobile;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {
    private String name;
    private String email;
    private String username;
    private String password;
    private String confirmPassword;
    private String oldPassword;

    public UserInfo() {
        this.name = "";
        this.email = "";
        this.username = "";
        this.password = "";
        this.confirmPassword = "";
        this.oldPassword = "";
    }

    public UserInfo(String name,
            String email,
            String username,
            String password,
            String confirmPassword,
            String oldPassword) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.oldPassword = oldPassword;
    }

    UserInfo(JSONObject json) throws JSONException {
        try {
            this.name = json.getJSONArray("ESP-Response").getJSONObject(0).getString("name");
            this.email = json.getJSONArray("ESP-Response").getJSONObject(0).getString("email");
            this.username = json.getJSONArray("ESP-Response").getJSONObject(1).getString("username");
            this.password = json.getJSONArray("ESP-Response").getJSONObject(1).getString("password");
        } catch (JSONException e) {
            this.name = json.getString("name");
            this.email = json.getString("email");
            this.username = json.getString("username");
            this.password = json.getString("password");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
