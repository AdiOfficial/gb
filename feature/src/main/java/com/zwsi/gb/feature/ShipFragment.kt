package com.zwsi.gb.feature

import android.arch.lifecycle.Observer
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.zwsi.gb.feature.GBViewModel.Companion.uidActivePlayer
import com.zwsi.gb.feature.GBViewModel.Companion.vm
import com.zwsi.gblib.GBData.Companion.BATTLESTAR
import com.zwsi.gblib.GBData.Companion.CRUISER
import com.zwsi.gblib.GBData.Companion.FACTORY
import com.zwsi.gblib.GBData.Companion.POD
import com.zwsi.gblib.GBData.Companion.RESEARCH
import com.zwsi.gblib.GBData.Companion.SHUTTLE
import com.zwsi.gblib.GBData.Companion.STATION
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation




class ShipFragment : Fragment() {

    companion object {

        fun newInstance(@Suppress("UNUSED_PARAMETER") message: String): ShipFragment {
            return ShipFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.fragment_ship, container, false)!!

        setDetails(view)

        val sh = vm.ship(tag!!.toInt())

        val background = view.background as GradientDrawable
        background.mutate()
        background.setStroke(2, Color.parseColor(vm.race(sh.uidRace).color))

        val turnObserver = Observer<Int> { _ ->
            setDetails(view)
            view.invalidate()
        }  // TODO why is newTurn nullable?
        GBViewModel.currentTurn.observe(this, turnObserver)

        val actionObserver = Observer<Int> { _ ->
            setDetails(view)
            view.invalidate()
        }
        GBViewModel.actionsTaken.observe(this, actionObserver)

        val makePodButton: Button = view.findViewById(R.id.makePod)
        makePodButton.tag = sh.uid
        makePodButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.makeShip(it, POD)
        })

        // FIXME Clean up this mess
        var makeCruiserButton: Button = view.findViewById(R.id.makeCruiser)
        makeCruiserButton.tag = sh.uid
        makeCruiserButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.makeShip(it, CRUISER)
        })

        makeCruiserButton = view.findViewById(R.id.makeShuttle)
        makeCruiserButton.tag = sh.uid
        makeCruiserButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.makeShip(it, SHUTTLE)
        })
        makeCruiserButton = view.findViewById(R.id.makeBattlestar)
        makeCruiserButton.tag = sh.uid
        makeCruiserButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.makeShip(it, BATTLESTAR)
        })
        makeCruiserButton = view.findViewById(R.id.makeStation)
        makeCruiserButton.tag = sh.uid
        makeCruiserButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.makeShip(it, STATION)
        })
        makeCruiserButton = view.findViewById(R.id.makeResearch)
        makeCruiserButton.tag = sh.uid
        makeCruiserButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.makeShip(it, RESEARCH)
        })

        val zoomButton: Button = view.findViewById(R.id.panzoomToShip)
        zoomButton.tag = sh.uid
        zoomButton.setOnClickListener(View.OnClickListener {
            GlobalStuff.panzoomToShip(it)
        })

        val destinationButton: Button = view.findViewById(R.id.destination)
        destinationButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this.context, DestinationActivity::class.java)
            intent.putExtra("uidShip", sh.uid)
            startActivity(intent)
        })

        return view
    }

    private fun setDetails(view: View) {

        val sh = vm.ships[tag!!.toInt()] // Don't use ship(), as we need to handle null here.

        val stats = view.findViewById<TextView>(R.id.ShipStats)
        val paint = stats.paint
        paint.textSize = 40f

        if (sh == null) {
            stats.setText("Boom! This ship no longer exists.")
        } else {

            stats.setText("Name: " + sh.name + "\n")
            stats.append("Type: " + sh.type + "\n")
            stats.append("Race: " + sh.race.name + "\n")
            stats.append("Speed: " + sh.speed + "\n")
            if (sh.guns > 0) {
                stats.append("Weapons: " + sh.guns + " ")
                stats.append("Damage: " + sh.damage + " ")
                stats.append("Range: " + sh.range + "\n")
            }
            stats.append("Health: " + sh.health + "\n")
            stats.append("Location: " + sh.loc.getLocDesc() + "\n")

            if (sh.uidRace == uidActivePlayer && sh.dest != null) stats.append("Destination: ${sh.dest?.getLocDesc()}\n")

            val shipRaceView = view.findViewById<ImageView>(R.id.ShipRaceView)
            shipRaceView.setImageResource(sh.race.getDrawableResource())

            val destinationButton = view.findViewById<Button>(R.id.destination)!!
            if (sh.uidRace != uidActivePlayer || sh.speed == 0) {
                destinationButton.visibility = View.GONE
            } else if (vm.secondPlayer && vm.playerTurns[uidActivePlayer] < GBViewModel.MIN_ACTIONS) {
                destinationButton.isEnabled = false
                destinationButton.alpha = 0.5f
            } else {
                destinationButton.isEnabled = true
                destinationButton.alpha = 1f // See Hack above.
            }

            val shipView = view.findViewById<ImageView>(R.id.ShipView)
            shipView.setImageBitmap(sh.getBitmap())
            if (sh.idxtype == STATION) {
                // TODO Animate Station in ShipDetail. Redraw the image view on every vsync postInvalidateOnAnimation()

                val anim = RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                anim.interpolator = LinearInterpolator()
                anim.repeatCount = Animation.INFINITE
                anim.duration = 10000

                // Start animating the image
                shipView.startAnimation(anim)
            }

            if (sh.idxtype == FACTORY) {
                if (sh.uidRace == uidActivePlayer) {
                    view.findViewById<Button>(R.id.makePod).setVisibility(View.VISIBLE)
                    view.findViewById<Button>(R.id.makeCruiser).setVisibility(View.VISIBLE)
                    view.findViewById<Button>(R.id.makeShuttle).setVisibility(View.VISIBLE)
                    view.findViewById<Button>(R.id.makeBattlestar).setVisibility(View.VISIBLE)
                    view.findViewById<Button>(R.id.makeStation).setVisibility(View.VISIBLE)
                    view.findViewById<Button>(R.id.makeResearch).setVisibility(View.VISIBLE)
                    if (vm.secondPlayer && vm.playerTurns[uidActivePlayer] < GBViewModel.MIN_ACTIONS) {
                        view.findViewById<Button>(R.id.makePod).isEnabled = false
                        view.findViewById<Button>(R.id.makeCruiser).isEnabled = false
                        view.findViewById<Button>(R.id.makeShuttle).isEnabled = false
                        view.findViewById<Button>(R.id.makeBattlestar).isEnabled = false
                        view.findViewById<Button>(R.id.makeStation).isEnabled = false
                        view.findViewById<Button>(R.id.makeResearch).isEnabled = false
                    } else {
                        view.findViewById<Button>(R.id.makePod).isEnabled = true
                        view.findViewById<Button>(R.id.makeCruiser).isEnabled = true
                        view.findViewById<Button>(R.id.makeShuttle).isEnabled = true
                        view.findViewById<Button>(R.id.makeBattlestar).isEnabled = true
                        view.findViewById<Button>(R.id.makeStation).isEnabled = true
                        view.findViewById<Button>(R.id.makeResearch).isEnabled = true
                    }
                }
            }
        }
    }
}

