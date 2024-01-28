/*
 * Copyright (C) 2023 The EverestOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.everest.basecamp.categories;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.everest.OmniJawsClient;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.everest.support.preferences.SecureSettingSwitchPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SearchIndexable
public class LockScreenSettings extends SettingsPreferenceFragment 
            implements Preference.OnPreferenceChangeListener {

     private static final String CATEGORY_AMBIENT = "ambient_display";

     private static final String LOCKSCREEN_DOUBLE_LINE_CLOCK = "lockscreen_double_line_clock_switch";

    private static final String KEY_WEATHER = "lockscreen_weather_enabled";

    private static final String MAIN_WIDGET_1_KEY = "main_custom_widgets1";
    private static final String MAIN_WIDGET_2_KEY = "main_custom_widgets2";
    private static final String EXTRA_WIDGET_1_KEY = "custom_widgets1";
    private static final String EXTRA_WIDGET_2_KEY = "custom_widgets2";
    private static final String EXTRA_WIDGET_3_KEY = "custom_widgets3";
    private static final String EXTRA_WIDGET_4_KEY = "custom_widgets4";
    private Preference mWeather;
    private OmniJawsClient mWeatherClient;

    	private SwitchPreferenceCompat mKGCustomClockColor;
    	private SecureSettingSwitchPreference mDoubleLineClock;

    private Preference mMainWidget1;
    private Preference mMainWidget2;
    private Preference mExtraWidget1;
    private Preference mExtraWidget2;
    private Preference mExtraWidget3;
    private Preference mExtraWidget4;

    private Map<Preference, String> widgetKeysMap = new HashMap<>();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.everest_lockscreen);
        PreferenceScreen prefSet = getPreferenceScreen();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final PreferenceCategory ambientCat = (PreferenceCategory) prefScreen.findPreference(CATEGORY_AMBIENT);
        if (TextUtils.isEmpty(getResources().getString(com.android.internal.R.string.config_dozeDoubleTapSensorType)) &&
                TextUtils.isEmpty(getResources().getString(com.android.internal.R.string.config_dozeTapSensorType))) {
            prefScreen.removePreference(ambientCat);
        }

        mDoubleLineClock = (SecureSettingSwitchPreference ) findPreference(LOCKSCREEN_DOUBLE_LINE_CLOCK);
        mDoubleLineClock.setChecked((Settings.Secure.getInt(getContentResolver(),
             Settings.Secure.LOCKSCREEN_USE_DOUBLE_LINE_CLOCK, 1) != 0));
        mDoubleLineClock.setOnPreferenceChangeListener(this);

        mWeather = (Preference) findPreference(KEY_WEATHER);
        mWeatherClient = new OmniJawsClient(getContext());
        updateWeatherSettings();

        mMainWidget1 = findPreference(MAIN_WIDGET_1_KEY);
        mMainWidget2 = findPreference(MAIN_WIDGET_2_KEY);
        mExtraWidget1 = findPreference(EXTRA_WIDGET_1_KEY);
        mExtraWidget2 = findPreference(EXTRA_WIDGET_2_KEY);
        mExtraWidget3 = findPreference(EXTRA_WIDGET_3_KEY);
        mExtraWidget4 = findPreference(EXTRA_WIDGET_4_KEY);

        List<Preference> widgetPreferences = Arrays.asList(mMainWidget1, mMainWidget2, mExtraWidget1, mExtraWidget2, mExtraWidget3, mExtraWidget4);
        for (Preference widgetPref : widgetPreferences) {
            widgetPref.setOnPreferenceChangeListener(this);
            widgetKeysMap.put(widgetPref, "");
        }

        String mainWidgets = Settings.System.getString(getActivity().getContentResolver(), "lockscreen_widgets");
        String extraWidgets = Settings.System.getString(getActivity().getContentResolver(), "lockscreen_widgets_extras");

        setWidgetValues(mainWidgets, mMainWidget1, mMainWidget2);
        setWidgetValues(extraWidgets, mExtraWidget1, mExtraWidget2, mExtraWidget3, mExtraWidget4);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

            if (preference == mDoubleLineClock) {
        	boolean value = (Boolean) newValue;
            Settings.Secure.putInt(resolver,
                    Settings.Secure.LOCKSCREEN_USE_DOUBLE_LINE_CLOCK, value ? 1 : 0);
            return true; 
        }
        else if (widgetKeysMap.containsKey(preference)) {
            widgetKeysMap.put(preference, String.valueOf(newValue));
            updateWidgetPreferences();
            return true;
        }
        return false;
    }  

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Secure.putIntForUser(resolver,
                Settings.Secure.DOZE_ON_CHARGE, 0, UserHandle.USER_CURRENT);
    }

    private void setWidgetValues(String widgets, Preference... preferences) {
        if (widgets == null) {
            return;
        }
        List<String> widgetList = Arrays.asList(widgets.split(","));
        for (int i = 0; i < preferences.length && i < widgetList.size(); i++) {
            widgetKeysMap.put(preferences[i], widgetList.get(i).trim());
        }
    }

    private void updateWidgetPreferences() {
        List<String> mainWidgetsList = Arrays.asList(widgetKeysMap.get(mMainWidget1), widgetKeysMap.get(mMainWidget2));
        List<String> extraWidgetsList = Arrays.asList(widgetKeysMap.get(mExtraWidget1), widgetKeysMap.get(mExtraWidget2), widgetKeysMap.get(mExtraWidget3), widgetKeysMap.get(mExtraWidget4));

        mainWidgetsList = filterEmptyStrings(mainWidgetsList);
        extraWidgetsList = filterEmptyStrings(extraWidgetsList);

        String mainWidgets = TextUtils.join(",", mainWidgetsList);
        String extraWidgets = TextUtils.join(",", extraWidgetsList);

        Settings.System.putString(getActivity().getContentResolver(), "lockscreen_widgets", mainWidgets);
        Settings.System.putString(getActivity().getContentResolver(), "lockscreen_widgets_extras", extraWidgets);
    }

    private List<String> filterEmptyStrings(List<String> inputList) {
        return inputList.stream().filter(s -> !TextUtils.isEmpty(s)).collect(Collectors.toList());
    }

    private void updateWeatherSettings() {
        if (mWeatherClient == null || mWeather == null) return;

        boolean weatherEnabled = mWeatherClient.isOmniJawsEnabled();
        mWeather.setEnabled(weatherEnabled);
        mWeather.setSummary(weatherEnabled ? R.string.lockscreen_weather_summary :
            R.string.lockscreen_weather_enabled_info);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeatherSettings();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVEREST;
    }
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.everest_lockscreen;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
