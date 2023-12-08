package nerd.tuxmobil.fahrplan.congress.engagements

import android.content.Context
import android.os.Build
import org.ligi.snackengage.SnackContext
import org.ligi.snackengage.conditions.SnackCondition
import org.ligi.snackengage.snacks.Snack

class IsInstalledViaGooglePlay : SnackCondition {

    private companion object {
        const val GOOGLE_PLAY_INSTALLER_PACKAGE_NAME = "com.android.vending"
    }

    override fun isAppropriate(context: SnackContext, snack: Snack): Boolean {
        val installerPackageName = getInstallerPackageName(context.androidContext)
        return installerPackageName != null && installerPackageName == GOOGLE_PLAY_INSTALLER_PACKAGE_NAME
    }

    private fun getInstallerPackageName(context: Context): String? {
        val packageName = context.packageName
        val installerPackageName = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            context.packageManager.getInstallerPackageName(packageName)
        } else {
            context.packageManager.getInstallSourceInfo(packageName).installingPackageName
        }
        return installerPackageName
    }

}
