package no.shitt.myshit.model;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerAPIListener;
import no.shitt.myshit.helper.ServerAPIParams;

public class User implements ServerAPIListener {
    public static User sharedUser = new User();
    private String userName;
    private String password;
    private String srvCommonName;
    private String srvFullName;

    // Saving parameters, so user name and password can be retrieved when service call completes
    private ServerAPIParams serverParams;

    // JSON tags
    public static final String JSON_USER_FIRST_NAME = "firstName";
    public static final String JSON_USER_MIDDLE_NAME = "middleName";
    public static final String JSON_USER_LAST_NAME = "lastName";
    public static final String JSON_USER_FULL_NAME = "fullName";
    public static final String JSON_USER_COMMON_NAME = "commonName";
    public static final String JSON_USER_FILE_SUFFIX = "fileSuffix";
    public static final String JSON_USER_DATA_VER_ID = "dataVersionId";
    public static final String JSON_USER_DATA_VER_TS = "dataVersionTS";
    public static final String JSON_ERROR = "error";

    // Prevent other classes from instantiating - User is singleton!
    private User() {
    }

    public String getUserName() {
        if (userName == null) {
            //Log.d("User", "Loading U from local file");
            try {
                InputStream inputStream = SHiTApplication.getContext().openFileInput(Constants.CRED_U_FILE);

                if (inputStream != null) {
                    //Log.d("User", "Input file open");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String receiveString;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    userName = stringBuilder.toString();

                    //Log.d("User", "Loaded U");
                }
            } catch (FileNotFoundException e) {
                //Log.d("User", "File not found: " + e.toString());
            } catch (IOException ioe) {
                //Log.e("User", "Failed to load U due to IO error...");
            }
        }
        return userName;
    }

    public void setUserName(String newName) {
        userName = newName;
        if (newName == null) {
            //try {
                SHiTApplication.getContext().deleteFile(Constants.CRED_U_FILE);
            //}
        } else {
            try {
                FileOutputStream fos = SHiTApplication.getContext().openFileOutput(Constants.CRED_U_FILE, Context.MODE_PRIVATE);
                fos.write(newName.getBytes());
                fos.close();

                //Log.d("User", "U saved to file");
            } catch (IOException ioe) {
                //Log.e("User", "Failed to save U due to IO error...");
            }
        }
    }

    String getPassword() {
        if (password == null) {
            //Log.d("User", "Loading P from local file");
            try {
                InputStream inputStream = SHiTApplication.getContext().openFileInput(Constants.CRED_P_FILE);

                if (inputStream != null) {
                    //Log.d("User", "Input file open");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String receiveString;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    password = stringBuilder.toString();

                    //Log.d("User", "Loaded P");
                }
            } catch (FileNotFoundException e) {
                //Log.d("User", "File not found: " + e.toString());
            } catch (IOException ioe) {
                //Log.e("User", "Failed to load P due to IO error...");
            }
        }
        return password;
    }

    void setPassword(String newPassword) {
        password = newPassword;
        if (newPassword == null) {
            SHiTApplication.getContext().deleteFile(Constants.CRED_P_FILE);
        } else {
            try {
                FileOutputStream fos = SHiTApplication.getContext().openFileOutput(Constants.CRED_P_FILE, Context.MODE_PRIVATE);
                fos.write(newPassword.getBytes());
                fos.close();

                //Log.d("User", "P saved to file");
            } catch (IOException ioe) {
                //Log.e("User", "Failed to save P due to IO error...");
            }
        }
    }

    public String getCommonName() {
        return srvCommonName;
    }

    public String getFullName() {
        return srvFullName;
    }

    public boolean hasCredentials() {
        if ( getUserName() == null || getUserName().isEmpty() || getPassword() == null || getPassword().isEmpty() ) {
            return false;
        }
        return true;
    }

    public void logout() {
        // Must clear password first, otherwise the missing user name will prevent deleting the password
        setPassword(null);
        setUserName(null);
        srvFullName = null;
        srvCommonName = null;
    }

    public void logon(String userName, String password) {
        try {
           String urlSafePassword = URLEncoder.encode(password, Constants.URL_ENCODE_CHARSET);
        }
        catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee.getMessage());
        }

        serverParams = new ServerAPIParams(ServerAPI.URL_USER_VERIFY);
        serverParams.addParameter(ServerAPI.PARAM_USER_NAME, userName);
        serverParams.addParameter(ServerAPI.PARAM_PASSWORD, password);

        new ServerAPI(this).execute(serverParams);
    }

    public void onRemoteCallComplete(JSONObject response) {
        if (response.isNull(User.JSON_ERROR)) {
            //Log.d("User", "User authenticated");
            setUserName(serverParams.parameters.get(ServerAPI.PARAM_USER_NAME));
            setPassword(serverParams.parameters.get(ServerAPI.PARAM_PASSWORD));

            srvCommonName = response.isNull(User.JSON_USER_COMMON_NAME) ? null : response.optString(User.JSON_USER_COMMON_NAME);
            srvFullName = response.isNull(User.JSON_USER_FULL_NAME) ? null : response.optString(User.JSON_USER_FULL_NAME);

            //Log.d("User", "Common name = " + srvCommonName);
            //Log.d("User", "Full name = " + srvFullName);

            Intent intent = new Intent(Constants.Notification.LOGON_SUCCEEDED);
            //intent.putExtra("message", "SHiT trips loaded");
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        } else {
            //Log.d("User", "User authentication failed");
            Intent intent = new Intent(Constants.Notification.LOGON_UNAUTHORISED);
            intent.putExtra("message", response.optString(User.JSON_ERROR));
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        }
    }

    public void onRemoteCallFailed() {
        //Log.d("User", "User authentication call failed");
        Intent intent = new Intent(Constants.Notification.LOGON_FAILED);
        //intent.putExtra("message", "SHiT trips loaded");
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    public void onRemoteCallFailed(Exception e) {
        //Log.d("User", "User authentication call failed with exception");
        Intent intent = new Intent(Constants.Notification.LOGON_FAILED);
        intent.putExtra("message", e.getMessage());
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }

}
