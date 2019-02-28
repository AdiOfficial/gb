package com.zwsi.gb.feature

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.zwsi.gb.feature.GBViewModel.Companion.vm

class StarsSlideActivity : AppCompatActivity() {

    private lateinit var viewpager: ViewPager
    private var startItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stars_slide)

        val title = if (intent.hasExtra("title")) intent.getStringExtra("title") else "Stars"
        val titleTextView = findViewById<TextView>(R.id.text_stars_title)
        titleTextView.setText(title)

        initViews()
        setupViewPager()

        viewpager.setCurrentItem(startItem)

    }

    private fun initViews() {
        viewpager = findViewById(R.id.viewpager)
    }

    private fun setupViewPager() {

        val adapter = MyFragmentPagerAdapter(getSupportFragmentManager())
        val startUID = getIntent().getIntExtra("UID", -1)

        // Figure out which items to display
        val displayList: ArrayList<Int>
        if (intent.hasExtra("stars")) {
            displayList = intent.getIntegerArrayListExtra("stars")
        } else {
            displayList = ArrayList<Int>()
            for ((_, star) in vm.stars) {
                displayList.add(star.uid)
            }
        }

        // Adding a fregment for each item we want to display
        for (uid in displayList) {
            val fragment: StarFragment = StarFragment.newInstance(uid.toString())
            adapter.addFragment(fragment, uid.toString())
            if (uid == startUID)
                startItem = adapter.count-1
        }

        viewpager.adapter = adapter

        viewpager.setClipToPadding(false)
        viewpager.setPadding(50,0,50,0)

    }

//    fun goToLocation( @Suppress("UNUSED_PARAMETER")view:View) {
//        //TODO DEPRECATED. Would have to create a map view and then zoom in
//    }

//    fun goToShips(view:View) {
//        GlobalStuff.goToShips(view)
//    }


}