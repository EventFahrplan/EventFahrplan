package nerd.tuxmobil.fahrplan.congress.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import nerd.tuxmobil.fahrplan.congress.R;

public abstract class AlertDialogHelper {

    public static void showErrorDialog(
            @NonNull final Context context,
            @StringRes final int title,
            @StringRes final int message,
            @Nullable final Object... messageArguments) {
        createErrorDialog(
                context,
                title,
                message,
                messageArguments)
                .show();
    }

    public static AlertDialog createErrorDialog(
            @NonNull final Context context,
            @StringRes final int title,
            @StringRes final int message,
            @Nullable final Object... messageArguments) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(context.getString(message, messageArguments))
                .setPositiveButton(R.string.OK, null)
                .create();
    }

}
