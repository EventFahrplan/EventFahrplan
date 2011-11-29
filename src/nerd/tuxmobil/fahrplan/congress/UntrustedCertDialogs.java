package nerd.tuxmobil.fahrplan.congress;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;


interface cert_accepted {
	void cert_accepted();
}

public class UntrustedCertDialogs {

	private static void showErrorDialog(final int msgResId, final Activity ctx, final Object... args) {
		new AlertDialog.Builder(ctx).setTitle(
				ctx.getString(R.string.dlg_invalid_certificate_could_not_apply))
				.setMessage(ctx.getString(msgResId, args))
				.setPositiveButton(ctx.getString(R.string.OK),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
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
			hash.append(String.format("%02x", (0xFF & digest[i])));
			if (i < (digest.length-1)) hash.append(" ");
		}
		return hash.toString();
	}

	public static void acceptKeyDialog(final int msgResId, final Activity ctx, final cert_accepted accept_callback, final Object... args) {
		final X509Certificate[] chain = TrustManagerFactory.getLastCertChain();
		String exMessage = "Unknown Error";

		Exception ex = ((Exception) CustomHttpClient.getSSLException());
		if (ex != null) {
			if (ex.getCause() != null) {
				if (ex.getCause().getCause() != null) {
					exMessage = ex.getCause().getCause().getMessage();

				} else {
					exMessage = ex.getCause().getMessage();
				}
			} else {
				exMessage = ex.getMessage();
			}
		}

		StringBuffer chainInfo = new StringBuffer(100);
		for (int i = 0; i < chain.length; i++) {
			// display certificate chain information
			chainInfo.append("Certificate chain[" + i + "]:\n");
			chainInfo.append("Subject: " + chain[i].getSubjectDN().toString()).append("\n");
			chainInfo.append("Issuer: " + chain[i].getIssuerDN().toString()).append("\n");
			chainInfo.append("Issued On: " + String.format("%02d.%02d.%04d", 
					chain[i].getNotBefore().getDate(),
					chain[i].getNotBefore().getMonth()+1,
					chain[i].getNotBefore().getYear()+1900)).append("\n");
			chainInfo.append("Expires On: " + String.format("%02d.%02d.%04d", 
					chain[i].getNotAfter().getDate(),
					chain[i].getNotAfter().getMonth()+1,
					chain[i].getNotAfter().getYear()+1900)).append("\n");
			chainInfo.append("SHA1 Fingerprint: " + getFingerPrint(chain[i])).append("\n");
		}

		new AlertDialog.Builder(ctx).setTitle(
				ctx.getString(R.string.dlg_invalid_certificate_title)).setMessage(
				ctx.getString(msgResId, exMessage) + " " + chainInfo.toString())
				.setCancelable(true).setPositiveButton(
						ctx.getString(R.string.dlg_invalid_certificate_accept),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									TrustManagerFactory
											.addCertificateChain(chain);
									if (accept_callback != null) {
										accept_callback.cert_accepted();
									}
								} catch (CertificateException e) {
									showErrorDialog(
											R.string.dlg_certificate_message_fmt, ctx,
											e.getMessage() == null ? "" : e
													.getMessage());
								}
							}
						}).setNegativeButton(
						ctx.getString(R.string.dlg_invalid_certificate_reject),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}

}
