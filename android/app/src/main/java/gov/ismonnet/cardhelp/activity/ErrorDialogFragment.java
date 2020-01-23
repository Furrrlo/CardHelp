package gov.ismonnet.cardhelp.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import gov.ismonnet.cardhelp.R;

public class ErrorDialogFragment extends DialogFragment {

    private final Set<DialogInterface.OnCancelListener> listeners;

    @Nullable
    private final String error;
    @Nullable
    private final Throwable exception;

    public ErrorDialogFragment(String error, Throwable exception) {
        this.error = error;
        this.exception = exception;
        listeners = new HashSet<>();
    }

    public ErrorDialogFragment(Exception ex) {
        this(null, ex);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listeners.forEach(l -> l.onCancel(dialog));
    }

    public ErrorDialogFragment addCancelListener(DialogInterface.OnCancelListener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final StringBuilder msgBuilder = new StringBuilder();
        if(error != null)
            msgBuilder
                    .append(error)
                    .append("\n");

        if(exception != null) {
            msgBuilder
                    .append(exception)
                    .append("\n");

            for (StackTraceElement traceElement : exception.getStackTrace())
                msgBuilder
                        .append("\tat ")
                        .append(traceElement)
                        .append("\n");
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.error_title)
                .setMessage(msgBuilder.toString())
                .setCancelable(true)
                .setNegativeButton(R.string.cancel_btn, (dialog, id) -> dialog.cancel())
                .create();
    }
}
