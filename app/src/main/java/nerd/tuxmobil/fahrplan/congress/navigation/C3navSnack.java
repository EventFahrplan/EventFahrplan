package nerd.tuxmobil.fahrplan.congress.navigation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.widget.TextView;

import org.ligi.snackengage.SnackContext;
import org.ligi.snackengage.snacks.OpenURLSnack;

import nerd.tuxmobil.fahrplan.congress.R;

public class C3navSnack extends OpenURLSnack {

    private final Context context;

    public C3navSnack(Context context) {
        super(context.getString(R.string.snack_engage_c3nav_url),
                context.getString(R.string.snack_engage_c3nav_unique_id));
        this.context = context;
        overrideTitleText(context.getString(R.string.snack_engage_c3nav_title));
        overrideActionText(context.getString(R.string.snack_engage_c3nav_action));
    }

    @NonNull
    @Override
    protected Snackbar createSnackBar(SnackContext snackContext) {
        Snackbar snackBar = super.createSnackBar(snackContext);
        TextView textView = snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.c3nav, 0, 0, 0);
        int pixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.snack_engage_c3nav_pixel_offset);
        textView.setCompoundDrawablePadding(pixelOffset);
        return snackBar;
    }

}
