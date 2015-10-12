package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

public abstract class AlertDialogHelper {

    public static void showErrorDialog(
            @NonNull final Context context,
            @StringRes final int title,
            @StringRes final int message,
            @Nullable final Object... messageArguments) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(context.getString(message, messageArguments))
                .setPositiveButton(R.string.OK, null)
                .show();
    }

}
