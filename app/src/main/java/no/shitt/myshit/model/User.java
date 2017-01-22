package no.shitt.myshit.model;


import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

//import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
//import java.util.regex.Pattern;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerAPIListener;
import no.shitt.myshit.helper.ServerAPIParams;

public class User implements ServerAPIListener {
    // Class members
    public static User sharedUser = new User();

    // Instance members
    private String userName;
    private String password;
    private String srvCommonName;
    private String srvFullName;
    private int id = USER_ID_UNKNOWN;

    // Saving parameters, so user name and password can be retrieved when service call completes
    private ServerAPIParams serverParams;

    // JSON tags
    //public static final String JSON_USER_FIRST_NAME = "firstName";
    //public static final String JSON_USER_MIDDLE_NAME = "middleName";
    //public static final String JSON_USER_LAST_NAME = "lastName";
    private static final String JSON_USER_FULL_NAME = "fullName";
    private static final String JSON_USER_COMMON_NAME = "commonName";
    private static final String JSON_USER_ID = "userId";
    //public static final String JSON_USER_FILE_SUFFIX = "fileSuffix";
    //public static final String JSON_USER_DATA_VER_ID = "dataVersionId";
    //public static final String JSON_USER_DATA_VER_TS = "dataVersionTS";
    private static final String JSON_ERROR = "error";

    // Constants
    private static final int USER_ID_UNKNOWN = -1;
    private static final String ID_USER_SEP = ":";

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
                    String idAndName = stringBuilder.toString();
                    String format = "^[1-9][0-9]*" + ID_USER_SEP + ".+";
                    //Log.d("User", "Read user data '" + idAndName + "', format = '" + format + "'");
                    if (idAndName.matches(format)) {
                        int sepPos = idAndName.indexOf(ID_USER_SEP);
                        String idString = idAndName.substring(0, sepPos);
                        //Log.d("User", "Separator pos = " + Integer.toString(sepPos) + ", id = '" + idString + "'");
                        id = Integer.parseInt(idString);
                        userName = idAndName.substring(sepPos + 1);
                        //Log.d("User", "User name = '" + userName + "'");
                    }

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

    private void setUserName(String newName) {
        userName = newName;
        if (newName == null) {
            //try {
                SHiTApplication.getContext().deleteFile(Constants.CRED_U_FILE);
            //}
        } else {
            saveUserName();
            /*
            try {
                FileOutputStream fos = SHiTApplication.getContext().openFileOutput(Constants.CRED_U_FILE, Context.MODE_PRIVATE);
                String fileData = Integer.toString(id) + ID_USER_SEP + newName;
                Log.d("User", "Saved user data '" + fileData + "'");
                fos.write(fileData.getBytes());
                fos.close();

                //Log.d("User", "U saved to file");
            } catch (IOException ioe) {
                //Log.e("User", "Failed to save U due to IO error...");
            }
            */
        }
    }

    private void saveUserName() {
        if (userName == null) {
            //try {
            SHiTApplication.getContext().deleteFile(Constants.CRED_U_FILE);
            //}
        } else {
            try {
                FileOutputStream fos = SHiTApplication.getContext().openFileOutput(Constants.CRED_U_FILE, Context.MODE_PRIVATE);
                String fileData = Integer.toString(id) + ID_USER_SEP + userName;
                //Log.d("User", "Saved user data '" + fileData + "'");
                fos.write(fileData.getBytes());
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
        deregisterFromPushNotifications();
        setPassword(null);
        setUserName(null);
        srvFullName = null;
        srvCommonName = null;
        id = USER_ID_UNKNOWN;
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
            id = response.isNull(User.JSON_USER_ID) ? USER_ID_UNKNOWN : response.optInt(User.JSON_USER_ID);

            saveUserName();
            //Log.d("User", "Common name = " + srvCommonName);
            //Log.d("User", "Full name = " + srvFullName);

            registerForPushNotifications();
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

    private void registerForPushNotifications() {
        if (id != USER_ID_UNKNOWN) {
            String topicUser = Constants.PushNotification.TOPIC_ROOT_USER + id;
            //Log.d("User", "registerForPushNotifications: Register for topic " + topicUser);
            FirebaseMessaging.getInstance().subscribeToTopic(topicUser);
        }
    }

    private void deregisterFromPushNotifications() {
        if (id != USER_ID_UNKNOWN) {
            String topicUser = Constants.PushNotification.TOPIC_ROOT_USER + id;
            //Log.d("User", "registerForPushNotifications: Deregister from topic " + topicUser);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicUser);
        }
    }


}
