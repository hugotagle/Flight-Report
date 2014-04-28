package com.dataedge.android.pc.activity;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

import com.dataedge.android.pc.R;
import com.dataedge.android.pc.R.layout;
import com.dataedge.android.pc.model.ReportModel;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    List<ReportModel> reports = null;
    int excessRptNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.myprofile_pref);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        updatePrefSummary(findPreference(key));

     }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory pCat = (PreferenceCategory) p;
            for (int i = 0; i < pCat.getPreferenceCount(); i++) {
                initSummary(pCat.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }

    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            if (listPref.getKey().equals("MyPosition")) {
                if (listPref.getEntry() == null) {
                    p.setSummary("Choose Position");
                } else {
                    p.setSummary(listPref.getEntry());
                }
            }
            if (listPref.getKey().equals("MaxRptNum")) {
                if (listPref.getEntry() == null) {
                    p.setSummary("Choose maximum number of submitted reports kept in device");
                } else {
                    p.setSummary(listPref.getEntry());
                }
            }
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (editTextPref.getKey().equals("MyEmail")) {
                if (editTextPref.getText() == null || editTextPref.getText().equals("")) {
                    p.setSummary("Enter email address");
                } else {
                    p.setSummary(editTextPref.getText());
                }
            }
            if (editTextPref.getKey().equals("MyEmpID")) {
                if (editTextPref.getText() == null || editTextPref.getText().equals("")) {
                    p.setSummary("Enter employee ID");
                } else {
                    p.setSummary(editTextPref.getText());
                }
            }
            if (editTextPref.getKey().equals("MyName")) {
                if (editTextPref.getText() == null || editTextPref.getText().equals("")) {
                    p.setSummary("Enter name");
                } else {
                    p.setSummary(editTextPref.getText());
                }
            }
            if (editTextPref.getKey().equals("MyPhone")) {
                if (editTextPref.getText() == null || editTextPref.getText().equals("")) {
                    p.setSummary("Enter phone number");
                } else {
                    p.setSummary(editTextPref.getText());
                }
            }
            if (editTextPref.getKey().equals("localhost")) {
                if (editTextPref.getText() == null || editTextPref.getText().equals("")) {
                    p.setSummary("Enter IP for localhost");
                } else {
                    p.setSummary(editTextPref.getText());
                }
            }
        }

    }


    @Override
    public void onBackPressed() {
        // restart Main activity

        // start Filed Reports
        Intent myIntent = new Intent(getApplicationContext(), MainMenuActivity.class);

        startActivityForResult(myIntent, 0);
    }
}
