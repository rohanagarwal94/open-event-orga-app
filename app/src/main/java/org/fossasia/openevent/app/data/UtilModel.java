package org.fossasia.openevent.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

import org.fossasia.openevent.app.data.contract.IUtilModel;
import org.fossasia.openevent.app.utils.Constants;
import org.fossasia.openevent.app.utils.NetworkUtils;

/**
 * Utility class to be used by presenters and models for
 * Android Context based actions
 *
 * Break in sub-modules if grows large
 */
public class UtilModel implements IUtilModel {

    private Context context;
    private SharedPreferences sharedPreferences;
    private String token;

    public UtilModel(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Constants.FOSS_PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public String getResourceString(@StringRes int stringId) {
        return context.getResources().getString(stringId);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    @Override
    public void saveString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    @Override
    public String getToken() {
        if(token != null)
            return token;
        return getString(Constants.SHARED_PREFS_TOKEN, null);
    }

    @Override
    public void saveToken(String token) {
        saveString(Constants.SHARED_PREFS_TOKEN, token);
    }

    @Override
    public boolean isConnected() {
        return NetworkUtils.isNetworkConnected(context);
    }
}
