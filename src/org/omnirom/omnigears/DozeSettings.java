/*
 *  Copyright (C) 2015-2018 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package org.omnirom.omnigears;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Color;
import android.content.Intent;
import android.content.res.Resources;
import android.os.UserHandle;
import androidx.preference.Preference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.Utils;

import org.omnirom.omnilib.preference.ColorSelectPreference;
import org.omnirom.omnilib.preference.SeekBarPreference;
import org.omnirom.omnilib.preference.SystemSettingSwitchPreference;

import java.util.List;
import java.util.ArrayList;

public class DozeSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "DozeSettings";
    private static final String KEY_PULSE_BRIGHTNESS = "ambient_pulse_brightness";
    private static final String KEY_DOZE_BRIGHTNESS = "ambient_doze_brightness";
    private static final String PULSE_COLOR_PREF = "pulse_ambient_light_color";

    private SeekBarPreference mPulseBrightness;
    private SeekBarPreference mDozeBrightness;

    private ColorSelectPreference PulseLightColorPref;
    private int mColor;
    private static final int MENU_RESET = Menu.FIRST;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.doze_settings);

        int defaultDoze = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessDoze);
        int defaultPulse = getResources().getInteger(
                com.android.internal.R.integer.config_screenBrightnessPulse);
        if (defaultPulse == -1) {
            defaultPulse = defaultDoze;
        }

        mPulseBrightness = (SeekBarPreference) findPreference(KEY_PULSE_BRIGHTNESS);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_PULSE_BRIGHTNESS, defaultPulse);
        mPulseBrightness.setValue(value);
        mPulseBrightness.setOnPreferenceChangeListener(this);

        mDozeBrightness = (SeekBarPreference) findPreference(KEY_DOZE_BRIGHTNESS);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_DOZE_BRIGHTNESS, defaultDoze);
        mDozeBrightness.setValue(value);
        mDozeBrightness.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);

        // Ambiant Pulse Light color preference
        PulseLightColorPref = (ColorSelectPreference) findPreference(PULSE_COLOR_PREF);
        int defaultColor = Color.parseColor("#3980FF");
        boolean useAccent = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.OMNI_AMBIENT_NOTIFICATION_LIGHT_ACCENT,
                0, UserHandle.USER_CURRENT) != 0;
        if (useAccent) {
            mColor = useAccent ?
                         Utils.getColorAccentDefaultColor(getContext()) : defaultColor;
            Settings.System.putIntForUser(getContentResolver(),
                         Settings.System.OMNI_PULSE_AMBIENT_LIGHT_COLOR, mColor,
                         UserHandle.USER_CURRENT);
            PulseLightColorPref.setEnabled(useAccent);
        } else {
            mColor = Settings.System.getInt(getContentResolver(),
                         Settings.System.OMNI_PULSE_AMBIENT_LIGHT_COLOR, defaultColor);
            PulseLightColorPref.setEnabled(useAccent);
        }
        PulseLightColorPref.setColor(mColor);
        PulseLightColorPref.setOnPreferenceChangeListener(this);
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

    protected void resetToDefaults() {
        int defaultColor = Color.parseColor("#3980FF");
        Settings.System.putInt(getContentResolver(), Settings.System.OMNI_PULSE_AMBIENT_LIGHT_COLOR,
                defaultColor);
        PulseLightColorPref.setColor(defaultColor);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPulseBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_PULSE_BRIGHTNESS, value);
            return true;
        } else if (preference == mDozeBrightness) {
            int value = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_DOZE_BRIGHTNESS, value);
            return true;
        } else if (preference == PulseLightColorPref){
            ColorSelectPreference lightPref = (ColorSelectPreference) preference;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.OMNI_PULSE_AMBIENT_LIGHT_COLOR, lightPref.getColor());
            return true;
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
                    sir.xmlResId = R.xml.doze_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}

