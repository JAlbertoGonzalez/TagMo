package com.hiddenramblings.tagmo;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import androidx.appcompat.app.AppCompatDelegate;

import com.hiddenramblings.tagmo.eightbit.content.ScaledContext;
import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.hiddenramblings.tagmo.eightbit.io.Debug;
import com.hiddenramblings.tagmo.settings.Preferences;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.util.Objects;

import me.weishu.reflection.Reflection;

public class TagMo extends Application {

    private static SoftReference<Context> mContext;
    private static SoftReference<Preferences> mPrefs;
    public static final int uiDelay = 50;

    public static Preferences getPrefs() {
        return mPrefs.get();
    }

    public static Context getContext() {
        return mContext.get();
    }

    private final boolean isWatchingANR = !BuildConfig.DEBUG && !BuildConfig.GOOGLE_PLAY;
    private boolean isUncaughtANR(Throwable error) {
        return null != error.getCause() && (error.getCause().getCause() instanceof ANRError);
    }

    public void setThemePreference() {
        switch (mPrefs.get().applicationTheme()) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (Debug.isNewer(Build.VERSION_CODES.P))
            HiddenApiBypass.addHiddenApiExemptions("LBluetooth");
        else if (Debug.isNewer(Build.VERSION_CODES.LOLLIPOP))
            Reflection.unseal(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (BuildConfig.WEAR_OS) {
            mContext = new SoftReference<>(new ScaledContext(this).watch(2));
            mContext.get().setTheme(R.style.AppTheme);
        } else {
            mContext = new SoftReference<>(this);
        }

        mPrefs = new SoftReference<>(new Preferences(this));

        if (isWatchingANR) {
            new ANRWatchDog(10000).setANRListener(error -> {
                StringWriter exception = new StringWriter();
                error.printStackTrace(new PrintWriter(exception));
                Debug.processException(this, exception.toString());
            }).start();
        }

        Thread.setDefaultUncaughtExceptionHandler((t, error) -> {
            if (isWatchingANR && isUncaughtANR(error)) return;
            StringWriter exception = new StringWriter();
            error.printStackTrace(new PrintWriter(exception));
            Debug.processException(this, exception.toString());
            System.exit(0);
        });

        setThemePreference();
    }

    private static final String commitHash = "#" + BuildConfig.COMMIT;
    private static final String versionLabel = "TagMo "
            + BuildConfig.VERSION_NAME + " (" + (BuildConfig.GOOGLE_PLAY
            ? "Google Play" : "GitHub") + " " + (BuildConfig.WEAR_OS
            ? "Wear OS" : Objects.equals(BuildConfig.BUILD_TYPE, "release")
            ? "Release" : "Debug") + ") " + commitHash;
    private static final String commitLink =
            "<a href=https://github.com/HiddenRamblings/TagMo/commit/"
            + BuildConfig.COMMIT + ">" + commitHash + "</a>";
    public static Spanned getVersionLabel(boolean plain) {
        return Html.fromHtml(plain ? versionLabel : versionLabel.replace(commitHash, commitLink));
    }
}