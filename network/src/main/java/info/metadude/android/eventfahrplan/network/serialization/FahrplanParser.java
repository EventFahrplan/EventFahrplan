package info.metadude.android.eventfahrplan.network.serialization;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import info.metadude.android.eventfahrplan.network.models.HttpHeader;
import info.metadude.android.eventfahrplan.network.models.Meta;
import info.metadude.android.eventfahrplan.network.models.Session;

public class FahrplanParser {

    public interface OnParseCompleteListener {

        void onUpdateSessions(@NonNull List<Session> sessions);

        void onUpdateMeta(@NonNull Meta meta);

        void onParseDone(@NonNull Boolean isSuccess, @NonNull String version);
    }

    @NonNull
    private final Logging logging;

    @Nullable
    private ParserTask task;

    @NonNull
    private OnParseCompleteListener listener;

    public FahrplanParser(@NonNull Logging logging) {
        this.logging = logging;
        task = null;
    }

    public void parse(@NonNull String fahrplan, @NonNull HttpHeader httpHeader) {
        task = new ParserTask(logging, listener);
        task.execute(fahrplan, httpHeader.getETag(), httpHeader.getLastModified());
    }

    public void cancel() {
        if (task != null) {
            task.cancel(false);
        }
    }

    public void setListener(@NonNull OnParseCompleteListener listener) {
        this.listener = listener;
        if (task != null) {
            task.setListener(listener);
        }
    }
}

