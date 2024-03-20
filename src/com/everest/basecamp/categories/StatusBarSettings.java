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

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.everest.support.preferences.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.everest.support.preferences.SystemSettingMasterSwitchPreference;

@SearchIndexable
public class StatusBarSettings extends SettingsPreferenceFragment 
            implements Preference.OnPreferenceChangeListener {

    private static final String KEY_NETWORK_TRAFFIC = "network_traffic_state";
    private static final String KEY_STATUSBAR_TOP_PADDING = "statusbar_top_padding";
    private static final String KEY_STATUSBAR_LEFT_PADDING = "statusbar_left_padding";
    private static final String KEY_STATUSBAR_RIGHT_PADDING = "statusbar_right_padding";
    private static final String DEFAULT = "_default";

    private boolean enabled;
    private SystemSettingMasterSwitchPreference mNetworkTraffic;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.everest_statusbar);
        PreferenceScreen prefSet = getPreferenceScreen();
        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

        mNetworkTraffic = (SystemSettingMasterSwitchPreference)
                findPreference(KEY_NETWORK_TRAFFIC);
        enabled = Settings.System.getIntForUser(resolver,
                KEY_NETWORK_TRAFFIC, 0, UserHandle.USER_CURRENT) == 1;
        mNetworkTraffic.setChecked(enabled);
        mNetworkTraffic.setOnPreferenceChangeListener(this);

        final int defaultLeftPadding = Settings.System.getIntForUser(getContentResolver(),
                    KEY_STATUSBAR_LEFT_PADDING + DEFAULT, 0, UserHandle.USER_CURRENT);
        CustomSeekBarPreference seekBar = findPreference(KEY_STATUSBAR_LEFT_PADDING);
        seekBar.setDefaultValue(defaultLeftPadding, true);

        final int defaultRightPadding = Settings.System.getIntForUser(getContentResolver(),
                    KEY_STATUSBAR_RIGHT_PADDING + DEFAULT, 0, UserHandle.USER_CURRENT);
        seekBar = findPreference(KEY_STATUSBAR_RIGHT_PADDING);
        seekBar.setDefaultValue(defaultRightPadding, true);

        final int defaultTopPadding = Settings.System.getIntForUser(getContentResolver(),
                    KEY_STATUSBAR_TOP_PADDING + DEFAULT, 0, UserHandle.USER_CURRENT);
        seekBar = findPreference(KEY_STATUSBAR_TOP_PADDING);
        seekBar.setDefaultValue(defaultTopPadding, true);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNetworkTraffic) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(resolver, KEY_NETWORK_TRAFFIC,
                    value ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
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
                    sir.xmlResId = R.xml.everest_statusbar;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
