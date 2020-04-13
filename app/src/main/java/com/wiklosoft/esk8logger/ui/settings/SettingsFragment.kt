package com.wiklosoft.esk8logger.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.wiklosoft.esk8logger.R


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val TAG = "SettingsFragment"

    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()

        settingsViewModel.storageInfo.observe(viewLifecycleOwner, Observer {
            findPreference<EditTextPreference>("storage")?.text = it
        })

        settingsViewModel.wifiSsid.observe(viewLifecycleOwner, Observer {
            findPreference<EditTextPreference>("wifi_ssid")?.text = it
        })

        settingsViewModel.wifiState.observe(viewLifecycleOwner, Observer {
            findPreference<SwitchPreferenceCompat>("wifi_enabled")?.isChecked = it
        })

        settingsViewModel.wifiPass.observe(viewLifecycleOwner, Observer {
            findPreference<EditTextPreference>("wifi_pass")?.text = it
        })

        settingsViewModel.startRideManually.observe(viewLifecycleOwner, Observer {
            findPreference<SwitchPreferenceCompat>("manual_ride_start")?.isChecked = it
        })

        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        Log.d(TAG, "onPreferenceChange " + key);

        when (key) {
            "wifi_ssid" -> sharedPreferences.getString(
                key,
                "esk8pal"
            )?.let { settingsViewModel.setWifiSsid(it) }
            "wifi_pass" -> sharedPreferences.getString(
                key,
                "esk8palpass"
            )?.let { settingsViewModel.setWifiPass(it) }
            "wifi_enabled" -> sharedPreferences.getBoolean(
                key,
                false
            )?.let { settingsViewModel.setWifiState(if (it) 1 else 0) }
            "manual_ride_start" -> sharedPreferences.getBoolean(
                key,
                false
            )?.let { settingsViewModel.setManualRideStart(if (it) 1 else 0) }
        }
    }
}
