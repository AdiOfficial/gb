package com.zwsi.gb.feature


import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.zwsi.gb.feature.GBViewModel.Companion.gi


var lastClickTime = 0L
val clickDelay = 300L

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // setTheme(R.style.AppTheme) // TODO Switch back from the Launcher Theme, but this won't compile to APK
        // setTheme(R.style.AppTheme) works in Android Studio -> Emulator, but not when building APKs. Error is
        // E:\AndroidStudioProjects\gb\feature\src\main\java\com\zwsi\gb\feature\MainActivity.kt: (25, 20): Unresolved reference: style

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the Version View
        val version = findViewById<TextView>(R.id.version)
        version.setText(BuildConfig.VERSIONNAME) // for now: 0.0.0.~ #commits...

        // Set up the MessageBox View to listen to news
        val messageBox: TextView = findViewById<TextView>(R.id.messageBox)!!
        messageBox.setText("Welcome to Andromeda Rising!\nA Game of galactic domination.\n\n")

        // FIXME. Need to disable all (most) buttons until we do have a Universe!!!!

        val playButton: Button = findViewById(R.id.PlayButton)
        playButton.setEnabled(false)
        playButton.setOnClickListener(View.OnClickListener {
            mapView(it)
        })

        val turnObserver = Observer<Int> { newTurn ->
            // This is a bit overkill, as it enables on every new turn
            playButton.setEnabled(true)
            messageBox.append("\nTurn: ${newTurn.toString()}\n")
            for (article in gi.news!!) {
                messageBox.append(article)
            }
            messageBox.invalidate()
        }  // TODO why is newTurn nullable?
        GBViewModel.currentTurn.observe(this, turnObserver)

        val doButton: Button = findViewById(R.id.DoButton)
        doButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.doUniverse(it)
        })

        val contButton: Button = findViewById(R.id.ContinuousButton)
        contButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.toggleContinuous(it)
        })

        val newButton: Button = findViewById(R.id.NewButton)
        newButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.makeUniverse(it)
            messageBox.append("\nCreated New Universe.\n")
        })

        val loadButton: Button = findViewById(R.id.LoadButton)
        loadButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.loadUniverse(it,0)
            messageBox.append("\nContinuing Current Game.\n")
        })

        val loadButton1: Button = findViewById(R.id.LoadButton1)
        loadButton1.setOnClickListener(View.OnClickListener {
            GlobalStuff.loadUniverse(it,1)
            messageBox.append("\nLoaded Mission 1.\n")
        })

        val loadButton2: Button = findViewById(R.id.LoadButton2)
        loadButton2.setOnClickListener(View.OnClickListener {
            GlobalStuff.loadUniverse(it,2)
            messageBox.append("\nLoaded Mission 2.\n")
        })

        val loadButton3: Button = findViewById(R.id.LoadButton3)
        loadButton3.setOnClickListener(View.OnClickListener {
            GlobalStuff.loadUniverse(it,3)
            messageBox.append("\nLoaded Mission 3.\n")
        })

        // Kick that off last, we want the app up and running asap
        //GlobalStuff.makeUniverse(applicationContext)

    }

    /** Called when the user taps the Map button */
    fun mapView( @Suppress("UNUSED_PARAMETER")view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < clickDelay) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    // TODO DELETE unless we need it again
    fun makeStuff(view: View) {
        if (SystemClock.elapsedRealtime() - lastClickTime < clickDelay) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        val message = "God Mode: Not doing anything right now"
        Toast.makeText(view.context, message, Toast.LENGTH_SHORT).show()
//        Thread(Runnable {
//
//        }).start()

    }


    // TODO DELETE The stuff below is all deprecated

//    /** Called when the user taps the Stars button */
//    fun starmap1( @Suppress("UNUSED_PARAMETER")view: View) {
//        if (SystemClock.elapsedRealtime() - lastClickTime < clickDelay) {
//            return;
//        }
//        lastClickTime = SystemClock.elapsedRealtime();
//        val intent = Intent(this, StarsActivity::class.java)
//        startActivity(intent)
//    }
//
//
//    /** Called when the user taps the Stars button */
//    fun stars( @Suppress("UNUSED_PARAMETER")view: View) {
//        if (SystemClock.elapsedRealtime() - lastClickTime < clickDelay) {
//            return;
//        }
//        lastClickTime = SystemClock.elapsedRealtime();
//        val intent = Intent(this, StarsSlideActivity::class.java)
//        startActivity(intent)
//    }
//
//    /** Called when the user taps the Planets button */
//    fun planets1( @Suppress("UNUSED_PARAMETER")view: View) {
//        if (SystemClock.elapsedRealtime() - lastClickTime < clickDelay) {
//            return;
//        }
//        lastClickTime = SystemClock.elapsedRealtime();
//        val intent = Intent(this, PlanetsScrollActivity::class.java)
//        startActivity(intent)
//    }
//
//    /** Called when the user taps the Planets button */
//    fun planets2( @Suppress("UNUSED_PARAMETER")view: View) {
//        if (SystemClock.elapsedRealtime() - lastClickTime < clickDelay) {
//            return;
//        }
//        lastClickTime = SystemClock.elapsedRealtime();
//        val intent = Intent(this, PlanetsSlideActivity::class.java)
//        startActivity(intent)
//    }
//
//    /** Called when the user taps the Races button */
//    fun races( @Suppress("UNUSED_PARAMETER")view: View) {
//        if (SystemClock.elapsedRealtime() - lastClickTime < clickDelay) {
//            return;
//        }
//        lastClickTime = SystemClock.elapsedRealtime();
//        val intent = Intent(this, RacesSlideActivity::class.java)
//
//        intent.putExtra("title", "All Races")
//        intent.putExtra("UID", 0)
//
//        startActivity(intent)
//    }
//
//    /** Called when the user taps the Ships button */
//    fun ships( @Suppress("UNUSED_PARAMETER")view: View) {
//        if (SystemClock.elapsedRealtime() - lastClickTime < clickDelay) {
//            return;
//        }
//        lastClickTime = SystemClock.elapsedRealtime();
//        val intent = Intent(this, ShipsSlideActivity::class.java)
//        startActivity(intent)
//    }

}
