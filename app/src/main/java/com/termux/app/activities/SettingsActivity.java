package com.termux.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;

import com.termux.app.compose.settings.SettingsUi;
import com.termux.shared.activities.ReportActivity;
import com.termux.shared.file.FileUtils;
import com.termux.shared.models.ReportInfo;
import com.termux.app.models.UserAction;
import com.termux.shared.interact.ShareUtils;
import com.termux.shared.android.PackageUtils;
import com.termux.shared.android.AndroidUtils;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.TermuxUtils;
import com.termux.shared.activity.media.AppCompatActivityUtils;
import com.termux.shared.theme.NightMode;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatActivityUtils.setNightMode(this, NightMode.getAppNightMode().getName(), true);

        ComposeView composeView = new ComposeView(this);
        SettingsUi.setSettingsContent(composeView, getTitle().toString(), this::finish);
        setContentView(composeView);
    }

    public static void showAbout(@NonNull Context context) {
        new Thread() {
            @Override
            public void run() {
                String title = "About";

                StringBuilder aboutString = new StringBuilder();
                aboutString.append(TermuxUtils.getAppInfoMarkdownString(context, TermuxUtils.AppInfoMode.TERMUX_AND_PLUGIN_PACKAGES));
                aboutString.append("\n\n").append(AndroidUtils.getDeviceInfoMarkdownString(context, true));
                aboutString.append("\n\n").append(TermuxUtils.getImportantLinksMarkdownString(context));

                String userActionName = UserAction.ABOUT.getName();

                ReportInfo reportInfo = new ReportInfo(userActionName,
                    TermuxConstants.TERMUX_APP.TERMUX_SETTINGS_ACTIVITY_NAME, title);
                reportInfo.setReportString(aboutString.toString());
                reportInfo.setReportSaveFileLabelAndPath(userActionName,
                    Environment.getExternalStorageDirectory() + "/" +
                        FileUtils.sanitizeFileName(TermuxConstants.TERMUX_APP_NAME + "-" + userActionName + ".log", true, true));

                ReportActivity.startReportActivity(context, reportInfo);
            }
        }.start();
    }

    /**
     * If APK is a Google Playstore release, then do not show the donation link
     * since Termux isn't exempted from the playstore policy donation links restriction
     * Check Fund solicitations: https://pay.google.com/intl/en_in/about/policy/
     */
    public static boolean isDonateVisible(@NonNull Context context) {
        String signingCertificateSHA256Digest = PackageUtils.getSigningCertificateSHA256DigestForPackage(context);
        if (signingCertificateSHA256Digest == null) return false;

        String apkRelease = TermuxUtils.getAPKRelease(signingCertificateSHA256Digest);
        return apkRelease != null &&
            !apkRelease.equals(TermuxConstants.APK_RELEASE_GOOGLE_PLAYSTORE_SIGNING_CERTIFICATE_SHA256_DIGEST);
    }

    public static void openDonate(@NonNull Context context) {
        ShareUtils.openUrl(context, TermuxConstants.TERMUX_DONATE_URL);
    }

}
