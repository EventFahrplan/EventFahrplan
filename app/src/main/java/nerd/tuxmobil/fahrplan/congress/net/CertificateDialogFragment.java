package nerd.tuxmobil.fahrplan.congress.net;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.utils.AlertDialogHelper;

public class CertificateDialogFragment extends DialogFragment {

    public interface OnCertAccepted {

        void onCertAccepted();
    }

    public static final String FRAGMENT_TAG =
            BuildConfig.APPLICATION_ID + "CERTIFICATE_DIALOG_FRAGMENT_TAG";

    private static final String BUNDLE_KEY_EXCEPTION_MESSAGE =
            BuildConfig.APPLICATION_ID + ".BUNDLE_KEY_EXCEPTION_MESSAGE";

    private OnCertAccepted listener;

    private X509Certificate[] chain;

    public static CertificateDialogFragment newInstance(String exceptionMessage) {
        CertificateDialogFragment dialog = new CertificateDialogFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_EXCEPTION_MESSAGE, exceptionMessage);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnCertAccepted) context;
    }

    private static String getFingerPrint(X509Certificate cert) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return "SHA-1 error";
        }
        byte[] der;
        try {
            der = cert.getEncoded();
        } catch (CertificateEncodingException e) {
            return "Reading CERT error";
        }
        md.update(der);
        byte[] digest = md.digest();
        StringBuilder hash = new StringBuilder();

        for (int i = 0; i < digest.length; i++) {
            hash.append(String.format("%02x", 0xFF & digest[i]));
            if (i < digest.length - 1) {
                hash.append(" ");
            }
        }
        return hash.toString();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String exceptionMessage = "Unknown Error";
        if (args != null) {
            String message = args.getString(BUNDLE_KEY_EXCEPTION_MESSAGE);
            if (!TextUtils.isEmpty(message)) {
                exceptionMessage = message;
            }
        }

        chain = TrustManagerFactory.getLastCertChain();

        StringBuffer chainInfo = new StringBuffer(100);
        int chain_len = chain == null ? 0 : chain.length;
        for (int i = 0; i < chain_len; i++) {
            // display certificate chain information
            chainInfo.append("Certificate chain[" + i + "]:\n");
            chainInfo.append("Subject: " + chain[i].getSubjectDN().toString()).append("\n");
            chainInfo.append("Issuer: " + chain[i].getIssuerDN().toString()).append("\n");
            chainInfo.append("Issued On: " + String.format("%02d.%02d.%04d",
                    chain[i].getNotBefore().getDate(),
                    chain[i].getNotBefore().getMonth() + 1,
                    chain[i].getNotBefore().getYear() + 1900)).append("\n");
            chainInfo.append("Expires On: " + String.format("%02d.%02d.%04d",
                    chain[i].getNotAfter().getDate(),
                    chain[i].getNotAfter().getMonth() + 1,
                    chain[i].getNotAfter().getYear() + 1900)).append("\n");
            chainInfo.append("SHA1 Fingerprint: " + getFingerPrint(chain[i])).append("\n");
            if (i + 1 < chain_len) {
                chainInfo.append("\n");
            }
        }

        Activity activity = requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.dlg_invalid_certificate_title))
                .setCancelable(true)
                .setPositiveButton(getString(android.R.string.yes), (dialog, which) -> onConfirm())
                .setNegativeButton(getString(android.R.string.no), null);

        LayoutInflater inflater = activity.getLayoutInflater();
        View msgView = inflater.inflate(R.layout.cert_dialog, null);
        TextView messageView = msgView.findViewById(R.id.cert);
        String message = getString(R.string.dlg_certificate_message_fmt, exceptionMessage);
        message += "\n\n" + chainInfo.toString();
        messageView.setText(message);
        builder.setView(msgView);
        return builder.create();
    }

    private void onConfirm() {
        try {
            if (chain != null) {
                TrustManagerFactory.addCertificateChain(chain);
            }
            if (listener != null) {
                listener.onCertAccepted();
            }
        } catch (CertificateException e) {
            String messageArguments = e.getMessage() == null ? "" : e.getMessage();
            AlertDialogHelper.showErrorDialog(
                    requireContext(),
                    R.string.dlg_invalid_certificate_could_not_apply,
                    R.string.dlg_certificate_message_fmt,
                    messageArguments);
        }
    }

}
