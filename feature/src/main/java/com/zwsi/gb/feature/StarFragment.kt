package com.zwsi.gb.feature

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.zwsi.gb.feature.GBViewModel.Companion.vm

class StarFragment : Fragment() {

    companion object {

        fun newInstance(@Suppress("UNUSED_PARAMETER")message: String): StarFragment {
            return StarFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.fragment_star, container, false)!!

        setDetails(view)

        val turnObserver = Observer<Int> { _->
            setDetails(view)
            view.invalidate()
        }  // TODO why is newTurn nullable?
        GBViewModel.currentTurn.observe(this, turnObserver)

        val starButton: Button = view.findViewById(R.id.panzoomToStar)
        starButton.tag = tag!!.toInt()
        starButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.panzoomToStar(it)
        })


        return view
    }

    private fun setDetails(view: View) {

        // Stars don't go away, so the below !! should be safe
        val star = vm.star(tag!!.toInt())

        val stats = view.findViewById<TextView>(R.id.StarStats)
        val paint = stats.paint
        paint.textSize = 40f

        stats.setText("${star.name} at (" + (star.loc.getLoc().x.toInt()) + ", " + star.loc.getLoc().y.toInt() + ")\n")

        val planets = star.starUidPlanets.map { vm.planet(it) }
        if (planets.isNotEmpty()) {
            stats.append("Planets (${planets.size}): ")
            for (p in planets) {
                stats.append("  " + p.name + " ")
            }
            stats.append("\n")
        }

        val ships = star.starUidShips.map { vm.ship(it) }
        if (ships.isNotEmpty()) {
            stats.append("Ships (${ships.size.toString()}): ")
            for (s in ships) {
                stats.append(s.name + " ")
            }
        }

    }
}
