package com.zwsi.gb.feature

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zwsi.gblib.GBController.Companion.universe
import com.zwsi.gblib.GBData.Companion.FACTORY

class ShipFragment : Fragment() {

    companion object {

        fun newInstance(message: String): ShipFragment {

            val f = ShipFragment()

            val bdl = Bundle(1)

            bdl.putString("UID", message)

            f.setArguments(bdl)

            return f

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View? = inflater.inflate(R.layout.fragment_ship, container, false);

        // What is this fragment about, and make sure the fragment remembers
        val shipID = arguments!!.getString("UID")!!.toInt()
        val sh = GBViewModel.viewShips.get(shipID)

        if (sh != null) {


            view!!.tag = sh

            val imageView = view.findViewById<ImageView>(R.id.ShipView)

            if (sh.idxtype == FACTORY) {
                imageView.setImageResource(R.drawable.factory)

                view.findViewById<Button>(R.id.makePod).setVisibility(View.VISIBLE)
                view.findViewById<Button>(R.id.makeCruiser).setVisibility(View.VISIBLE)

            } else if (sh.idxtype == com.zwsi.gblib.GBData.POD) {
                if (sh.race.uid == 2) {
                    imageView.setImageResource(R.drawable.beetlepod)

                } else {
                    imageView.setImageResource(R.drawable.podt)
                }
            } else if (sh.idxtype == 2) {
                imageView.setImageResource(R.drawable.cruisert)
            } else
                imageView.setImageResource(R.drawable.yellow)


            var stats = view.findViewById<TextView>(R.id.ShipStats)
            var paint = stats.paint
            paint.textSize = 40f

            stats.append("\n")
            stats.append("Name: " + sh.name + "\n")
            stats.append("Type: " + sh.type + "\n")
            stats.append("Speed: " + sh.speed + "\n")
            stats.append("Health: " + sh.health+ "\n")
            stats.append("Race: " + sh.race.name + "\n")
            stats.append("Location: " + sh.loc.getLocDesc() + "\n")
            if (sh.dest != null) stats.append("Destination: "+ sh.dest?.getLocDesc())

            stats = view.findViewById<TextView>(R.id.ShipBackground)
            paint = stats.paint
            paint.textSize = 40f

            stats.setText("Lorem ipsum")

            stats.append("\n")

            stats.append("id:" + sh.id + " | ")
            stats.append("refUID:" + sh.uid + " | ")
            stats.append("idxt:" + sh.idxtype + " | ")
            stats.append("loca:" + sh.loc.level + "." + sh.loc.refUID)


            if (sh.speed == 0) {
                view.findViewById<Spinner>(R.id.spinner).setVisibility(View.GONE)
                view.findViewById<Button>(R.id.flyTo).setVisibility(View.GONE)
            }

            // TODO: better selection of possible targets once we have visibility. Right now it's all insystem
            // and first planet of each system outside.
            var destinationPlanets = arrayListOf<String>()
            if (sh.getStar() != null) {
                for (p in sh.getStar()!!.starPlanets) {
                    destinationPlanets.add(p.name)
                }
            }
            for (s in universe.allStars) {
                destinationPlanets.add(s.starPlanets[0].name)
            }

            // Create an ArrayAdapter
            val adapter =
                ArrayAdapter<String>(this.activity!!, android.R.layout.simple_spinner_item, destinationPlanets)

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // Apply the adapter to the spinner
            var spinner = view.findViewById<Spinner>(R.id.spinner)
            spinner.adapter = adapter

            val flyButton = view.findViewById<Button>(R.id.flyTo)
            flyButton.setTag(R.id.TAG_FLYTO_SPINNER,spinner)
            flyButton.setTag(R.id.TAG_FLYTO_SHIP, sh)

            view.findViewById<Button>(R.id.goButton).tag=sh
            view.findViewById<Button>(R.id.makePod).tag=sh
            view.findViewById<Button>(R.id.makeCruiser).tag=sh

        }
        return view
    }
}
