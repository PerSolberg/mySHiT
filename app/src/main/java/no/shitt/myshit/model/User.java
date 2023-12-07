package no.shitt.myshit.model;

import android.accounts.AuthenticatorException;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerAPI;

public class User implements ServerAPI.Listener {
    // Class members
    private static final String LOG_TAG = User.class.getSimpleName();
    public static final User sharedUser = new User();

    // Instance members
    private String userName;
    private String password;
    private String srvCommonName;
    private String srvFullName;
    private String srvInitials;
    private String srvShortName;
    private int id = USER_ID_UNKNOWN;

    // Saving parameters, so user name and password can be retrieved when service call completes
    private ServerAPI.Params serverParams;

    // JSON tags
    private static final String JSON_USER_FULL_NAME = "fullName";
    private static final String JSON_USER_COMMON_NAME = "commonName";
    private static final String JSON_USER_SHORT_NAME = "shortName";
    private static final String JSON_USER_INITIALS = "initials";
    private static final String JSON_USER_ID = "userId";
    private static final String JSON_ERROR = "error";

    // Constants
    private static final int USER_ID_UNKNOWN = -1;
    private static final String ID_USER_SEP = ":";

    // Prevent other classes from instantiating - User is singleton!
    private User() {
        final FirebaseApp firebaseApp = FirebaseApp.initializeApp(SHiTApplication.getContext());
        if (firebaseApp == null) {
            Log.e(LOG_TAG, "FirebaseApp not initialised");
        }
    }

    public String getUserName() {
        if (userName == null) {
            try {
                InputStream inputStream = SHiTApplication.getContext().openFileInput(Constants.CRED_U_FILE);

                if (inputStream != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String receiveString;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    String idAndName = stringBuilder.toString();
                    String format = "^[1-9][0-9]*" + ID_USER_SEP + ".+";
                    if (idAndName.matches(format)) {
                        int sepPos = idAndName.indexOf(ID_USER_SEP);
                        String idString = idAndName.substring(0, sepPos);
                        id = Integer.parseInt(idString);
                        userName = idAndName.substring(sepPos + 1);
                    }
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
            srvShortName = null;
            srvCommonName = null;
            srvFullName = null;
            srvInitials = null;
            SHiTApplication.getContext().deleteFile(Constants.CRED_U_FILE);
        } else {
            saveUserName();
        }
    }

    private void saveUserName() {
        if (userName == null) {
            SHiTApplication.getContext().deleteFile(Constants.CRED_U_FILE);
        } else {
            try {
                FileOutputStream fos = SHiTApplication.getContext().openFileOutput(Constants.CRED_U_FILE, Context.MODE_PRIVATE);
                String fileData = id + ID_USER_SEP + userName;
                fos.write(fileData.getBytes());
                fos.close();
            } catch (IOException ioe) {
                //Log.e("User", "Failed to save U due to IO error...");
            }
        }
    }

    String getPassword() {
        if (password == null) {
            try {
                InputStream inputStream = SHiTApplication.getContext().openFileInput(Constants.CRED_P_FILE);

                if (inputStream != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String receiveString;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    password = stringBuilder.toString();
                }
            } catch (FileNotFoundException e) {
                //Log.d("User", "File not found: " + e.toString());
            } catch (IOException ioe) {
                //Log.e("User", "Failed to load P due to IO error...");
            }
        }
        return password;
    }

    private void setPassword(String newPassword) {
        password = newPassword;
        if (newPassword == null) {
            SHiTApplication.getContext().deleteFile(Constants.CRED_P_FILE);
        } else {
            try {
                FileOutputStream fos = SHiTApplication.getContext().openFileOutput(Constants.CRED_P_FILE, Context.MODE_PRIVATE);
                fos.write(newPassword.getBytes());
                fos.close();
            } catch (IOException ioe) {
                //Log.e("User", "Failed to save P due to IO error...");
            }
        }
    }

    public int getId() { return id; }

    @SuppressWarnings("unused")
    public String getCommonName() { return srvCommonName; }

    @SuppressWarnings("unused")
    public String getFullName() { return srvFullName; }

    String getInitials() {
        return srvInitials;
    }

    String getShortName() {
        return srvShortName;
    }

    boolean hasCredentials() {
        return getUserName() != null && !getUserName().isEmpty() && getPassword() != null && !getPassword().isEmpty();
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
        serverParams = new ServerAPI.Params(ServerAPI.URL_USER_VERIFY);
        serverParams.addParameter(ServerAPI.Param.USER_NAME, userName);
        serverParams.addParameter(ServerAPI.Param.PASSWORD, password);

        new ServerAPI(this).execute(serverParams);
    }

    public void onRemoteCallComplete(JSONObject response) {
        if (response.isNull(User.JSON_ERROR) && !response.isNull(ServerAPI.ResultItem.USER)) {
            Log.d(LOG_TAG, "User authenticated");
            JSONObject jsonUser = Objects.requireNonNull( response.optJSONObject(ServerAPI.ResultItem.USER) );
            setUserName(serverParams.parameters.get(ServerAPI.Param.USER_NAME));
            setPassword(serverParams.parameters.get(ServerAPI.Param.PASSWORD));

            srvCommonName = jsonUser.isNull(User.JSON_USER_COMMON_NAME) ? null : jsonUser.optString(User.JSON_USER_COMMON_NAME);
            srvFullName = jsonUser.isNull(User.JSON_USER_FULL_NAME) ? null : jsonUser.optString(User.JSON_USER_FULL_NAME);
            srvShortName = jsonUser.isNull(User.JSON_USER_SHORT_NAME) ? null : jsonUser.optString(User.JSON_USER_SHORT_NAME);
            srvInitials = jsonUser.isNull(User.JSON_USER_INITIALS) ? null : jsonUser.optString(User.JSON_USER_INITIALS);
            id = jsonUser.isNull(User.JSON_USER_ID) ? USER_ID_UNKNOWN : jsonUser.optInt(User.JSON_USER_ID);

            saveUserName();

            registerForPushNotifications();
            Intent intent = new Intent(Constants.Notification.LOGON_SUCCEEDED);
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        } else {
            Log.e(LOG_TAG,"Old authentication failure invoked - FIX!");
        }
    }

    public void onRemoteCallFailed() {
        Log.d(LOG_TAG, "User authentication call failed with unknown exception");
        Intent intent = new Intent(Constants.Notification.LOGON_FAILED);
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }


    public void onRemoteCallFailed(Exception e) {
        if (e instanceof AuthenticatorException) {
            Log.d(LOG_TAG, "User authentication failed");
            Context ctx = SHiTApplication.getContext();
            Intent intent = new Intent(Constants.Notification.LOGON_UNAUTHORISED);
            LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
        } else {
            Log.d(LOG_TAG, "User authentication call failed with exception");
            Intent intent = new Intent(Constants.Notification.LOGON_FAILED);
            intent.putExtra("message", e.getMessage());
            LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
        }
    }

    private void registerForPushNotifications() {
        if (id != USER_ID_UNKNOWN) {
            String topicUser = Constants.PushNotification.TOPIC_ROOT_USER + id;
            FirebaseMessaging.getInstance().subscribeToTopic(topicUser);
        }
    }

    private void deregisterFromPushNotifications() {
        if (id != USER_ID_UNKNOWN) {
            String topicUser = Constants.PushNotification.TOPIC_ROOT_USER + id;
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topicUser);
        }
    }

}
