package no.shitt.myshit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import no.shitt.myshit.helper.AlertDialogueManager;
import no.shitt.myshit.model.User;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity /* implements LoaderCallbacks<Cursor> */ {
    // Alert dialog manager
    private final AlertDialogueManager alert = new AlertDialogueManager();

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private final LogonSuccessHandler logonSuccessHandler = new LogonSuccessHandler();
    private final LogonUnauthorisedHandler logonUnauthorisedHandler = new LogonUnauthorisedHandler();
    private final LogonFailureHandler logonFailureHandler = new LogonFailureHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Constants.DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener( (textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_GO) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener( (view) -> attemptLogin() );

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        LocalBroadcastManager.getInstance(this).registerReceiver(logonSuccessHandler, new IntentFilter(Constants.Notification.LOGON_SUCCEEDED));
        LocalBroadcastManager.getInstance(this).registerReceiver(logonUnauthorisedHandler, new IntentFilter(Constants.Notification.LOGON_UNAUTHORISED));
        LocalBroadcastManager.getInstance(this).registerReceiver(logonFailureHandler, new IntentFilter(Constants.Notification.LOGON_FAILED));
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logonSuccessHandler);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logonUnauthorisedHandler);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(logonFailureHandler);
        super.onDestroy();
    }

    private class LogonSuccessHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showProgress(false);
            finish();
        }
    }

    private class LogonUnauthorisedHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showProgress(false);
            mPasswordView.setError(getString(R.string.error_unauthorised));
            mPasswordView.requestFocus();
        }
    }

    private class LogonFailureHandler extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showProgress(false);
            alert.showAlertDialogue(LoginActivity.this, getString(R.string.msg_logon_error),
                    intent.getStringExtra("message"), false);
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            User.sharedUser.logon(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
    }

}

