package org.oo_oo.timetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.Preference

class SettingsActivity : AppCompatActivity() {

    private lateinit var dbHelper: WorkTimeDatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = WorkTimeDatabaseHelper(this)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, SettingsFragment.newInstance(dbHelper))
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var dbHelper: WorkTimeDatabaseHelper
        companion object {
            fun newInstance(dbHelper: WorkTimeDatabaseHelper): SettingsFragment {
                val fragment = SettingsFragment()
                fragment.dbHelper = dbHelper
                return fragment
            }
        }
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val clearDatabasePreference: Preference? = findPreference("clear_database")
            clearDatabasePreference?.setOnPreferenceClickListener {
                dbHelper.clearDatabase()

                true
            }
            val clearOT: Preference? = findPreference("zero_overtime")
            clearOT?.setOnPreferenceClickListener {

//                it.sharedPreferences?.edit()?.putLong("ot", 0)!!.commit()
                it.sharedPreferences?.edit()?.putString("ot", "00:00:00")!!.commit()
            }
        }
    }
}
