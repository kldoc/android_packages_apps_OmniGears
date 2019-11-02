/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package org.omnirom.omnigears.ambiantpulse;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import org.omnirom.omnilib.preference.SystemSettingSwitchPreference;
import org.omnirom.omnilib.preference.ColorSelectPreference;

import java.util.List;
import java.util.ArrayList;

public class AmbiantPulseSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "AmbiantPulseSettings";

    private static final String PULSE_AMBIANT_LIGHT_PREF = "pulse_ambient_light_enabled";
    private static final String AMBIANT_PULSE_COLOR_PREF = "pulse_color";

    private SystemSettingSwitchPreference mEnabledPref;

    private PreferenceGroup mColorPrefs;
    private ColorSelectPreference mFastColorPref;
    private ColorSelectPreference mAmbiantPulseLightColorPref;
    private static final int MENU_RESET = Menu.FIRST;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreenitems);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();

        mEnabledPref = (SystemSettingSwitchPreference)prefSet.findPreference(PULSE_AMBIANT_LIGHT_PREF);
        mEnabledPref.setChecked(Settings.System.getInt(resolver,
                        Settings.System.OMNI_PULSE_AMBIANT_LIGHT_ENABLED, 1) != 0);
        mEnabledPref.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);

        // Ambiant Pulse Light color preference
        mAmbiantPulseLightColorPref = (ColorSelectPreference) prefSet.findPreference(AMBIANT_PULSE_COLOR_PREF);
        mAmbiantPulseLightColorPref.setOnPreferenceChangeListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDefault();
    }

    private void refreshDefault() {
        ContentResolver resolver = getContentResolver();
        Resources res = getResources();

        if (mAmbiantPulseLightColorPref != null) {
            int ambiantcolor = Settings.System.getInt(resolver, Settings.System.OMNI_PULSE_AMBIANT_LIGHT_COLOR,
                    res.getInteger(com.android.internal.R.integer.config_AmbiantPulseLightColor));
            mAmbiantPulseLightColorPref.setColor(ambiantcolor);
        }

    }

    /**
     * Updates the default or application specific notification settings.
     *
     * @param key of the specific setting to update
     * @param color
     */
    protected void updateValues(String key, Integer color) {
        ContentResolver resolver = getContentResolver();

        if (key.equals(AMBIANT_PULSE_COLOR_PREF)) {
            Settings.System.putInt(resolver, Settings.System.OMNI_PULSE_AMBIANT_LIGHT_COLOR, color);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup_restore)
                .setAlphabeticShortcut('r')
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefaults();
                return true;
        }
        return false;
    }

    protected void resetColors() {
        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = getResources();

        Settings.System.putInt(resolver, Settings.System.OMNI_PULSE_AMBIANT_LIGHT_COLOR,
                res.getInteger(com.android.internal.R.integer.config_AmbiantPulseLightColor));
        refreshDefault();
    }

    protected void resetToDefaults() {
        if (mEnabledPref != null) mEnabledPref.setChecked(true);
        resetColors();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mEnabledPref) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.OMNI_PULSE_AMBIANT_LIGHT_ENABLED, value ? 1:0);
        } else {
            ColorSelectPreference lightPref = (ColorSelectPreference) preference;
            updateValues(lightPref.getKey(), lightPref.getColor());
        }
        return true;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.lockscreenitems;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    final Resources res = context.getResources();
                    result.add(AMBIANT_PULSE_COLOR_PREF);
                    return result;
                }
            };
}
