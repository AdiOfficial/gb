package com.zwsi.gb.app

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import com.zwsi.gb.app.GBViewModel.Companion.showClickTargets
import com.zwsi.gb.app.GBViewModel.Companion.showContButton
import com.zwsi.gb.app.GBViewModel.Companion.showRaceStats
import com.zwsi.gb.app.GBViewModel.Companion.showStats
import com.zwsi.gb.app.GBViewModel.Companion.superSensors
import com.zwsi.gb.app.GBViewModel.Companion.vm
import com.zwsi.gblib.GBController

class AROptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        // Version TextView
        val version = findViewById<TextView>(R.id.version)
        version.setText(BuildConfig.VERSIONNAME)

        // Done Button
        val doneButton: Button = findViewById(R.id.DoneButton)
        doneButton.setOnClickListener(View.OnClickListener {
            finish()
        })

        val sharedPref = this.getSharedPreferences("options", Context.MODE_PRIVATE)

        val superSensorsButton: Switch = findViewById(R.id.SuperSensors)
        superSensorsButton.isChecked = superSensors
        superSensorsButton.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("superSensors", isChecked)
                apply()
            }
            GBViewModel.updatePrefs()
        }

        val showStatsButton: Switch = findViewById(R.id.ShowStats)
        showStatsButton.isChecked = showStats
        showStatsButton.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("showStats", isChecked)
                apply()
            }
            GBViewModel.updatePrefs()
        }

        val showRaceStatsButton: Switch = findViewById(R.id.ShowRaceStats)
        showRaceStatsButton.isChecked = showRaceStats
        showRaceStatsButton.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("showRaceStats", isChecked)
                apply()
            }
            GBViewModel.updatePrefs()
        }

        val showClickTargetsButton: Switch = findViewById(R.id.ShowClickTargets)
        showClickTargetsButton.isChecked = showClickTargets
        showClickTargetsButton.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("showClickTargets", isChecked)
                apply()
            }
            GBViewModel.updatePrefs()
        }

        val showContButtonButton: Switch = findViewById(R.id.ShowContButton)
        showContButtonButton.isChecked = showContButton
        showContButtonButton.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("showContButton", isChecked)
                apply()
            }
            GBViewModel.updatePrefs()
            GBViewModel.actionsTaken.value = System.currentTimeMillis().toInt()

        }

        val DemoModeButton: Switch = findViewById(R.id.DemoModeButton)
        DemoModeButton.isChecked
        DemoModeButton.setOnCheckedChangeListener { _, isChecked ->
            vm.demoMode = isChecked
            GBController.setDemoMode(isChecked)
            GBViewModel.actionsTaken.value = System.currentTimeMillis().toInt()
        }
    }

}
