package no.shitt.myshit.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;

import no.shitt.myshit.R;

public class AlertDialogueManager {
    /**
     * Function to display simple Alert Dialog
     * @param context - application context
     * @param title - alert dialog title
     * @param message - alert message
     * @param status - success/failure (used to set icon)
     *               - pass null if you don't want icon
     * */
    public void showAlertDialogue(Context context, String title, String message,
                                Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        if(status != null) {
            alertDialog.setIcon((status) ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert);
        }

        // Setting OK Button
        alertDialog.setButton( DialogInterface.BUTTON_POSITIVE
                             , context.getString(R.string.btn_ok)
                             , (Message) null);

        // Showing Alert Message
        alertDialog.show();
    }
}