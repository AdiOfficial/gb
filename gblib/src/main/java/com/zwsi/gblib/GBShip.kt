// Copyright 2018 Louis Perrochon. All rights reserved
//
// // Inspiration
// https://github.com/kaladron/galactic-bloodshed/blob/master/data/exam.dat
// https://github.com/kaladron/galactic-bloodshed/blob/master/data/ship.dat

package com.zwsi.gblib

import com.zwsi.gblib.GBController.Companion.universe
import com.zwsi.gblib.GBData.Companion.POD
import com.zwsi.gblib.GBLocation.Companion.DEEPSPACE
import com.zwsi.gblib.GBLocation.Companion.LANDED
import com.zwsi.gblib.GBLocation.Companion.ORBIT
import com.zwsi.gblib.GBLocation.Companion.SYSTEM
import com.zwsi.gblib.GBLog.gbAssert
import java.util.*
import kotlin.math.atan2

class GBShip(val idxtype: Int, val race: GBRace, var loc: GBLocation) {

    val id: Int     // Unique global ID of this ship
    val uid: Int    // id in universe wide list
    val rid: Int    // id in list of ships of race/owner
    val name: String // name, first letters of race and type, then id
    val type: String // type in printable form
    val speed: Int   // speed of ship // TODO Feature: insystem and hyperspeed.
    var health: Int  // health of ship. Goes down when shot at. Not going up (as of now)

    internal val PLANET_ORBIT_SIZE = 1f  // TODO Where should this live.
    // If it's too big, orbits of planets overlap, which is problematic. But we want it bigger for better space use
    // in MapView.

    var dest: GBLocation? = null
    private val trails: MutableList<GBxy> = Collections.synchronizedList(arrayListOf<GBxy>())
    internal var lastTrailsUpdate = -1
    internal var trailsList = trails.toList()

    fun getTrailList(): List<GBxy> {

        if (universe.turn > lastTrailsUpdate) {
            trailsList = trails.toList()
        }
        return trailsList
    }

    init {
        id = GBData.getNextGlobalId()

        universe.allShips.add(this)
        uid = universe.allShips.indexOf(this)

        race.raceShips.add(this)
        rid = race.raceShips.indexOf(this)

        when (loc.level) {
            LANDED -> {
                loc.getPlanet()!!.landedShips.add(this)
            }
            ORBIT -> {
                loc.getPlanet()!!.orbitShips.add(this)
            }
            SYSTEM -> {
                loc.getStar()!!.starShips.add(this)
            }
            DEEPSPACE -> {
                universe.deepSpaceShips.add(this)
            }
            else -> {
                gbAssert("Bad Parameters for ship placement $loc", { false })
            }
        }

        type = GBData.getShipType(idxtype)
        name = race.name.first().toString() + type.first().toString() + race.raceShips.indexOf(this)
        speed = GBData.getShipSpeed(idxtype)
        health = GBData.getShipHealth(idxtype)
    }


    fun changeShipLocation(loc: GBLocation) {

        GBLog.d("Changing " + name + " from " + this.loc.getLocDesc() + " to " + loc.getLocDesc())

        // TODO Performance: no need to always do these whens, e.g. if things are the same, no need to remove and add
        when (this.loc.level) {
            LANDED -> {
                this.loc.getPlanet()!!.landedShips.remove(this)
            }
            ORBIT -> {
                this.loc.getPlanet()!!.orbitShips.remove(this)
            }
            SYSTEM -> {
                this.loc.getStar()!!.starShips.remove(this)
            }
            DEEPSPACE -> {
                universe.deepSpaceShips.remove(this)
            }
            else -> {
                gbAssert("Bad Parameters for ship removement $loc", { false })
            }
        }
        when (loc.level) {
            LANDED -> {
                if (idxtype == POD) {
                    // TODO We should really handle this somewhere else. If pod were a subtype of ship, it could overwrite
                    // This is a pod, they populate, then destroy.  We set health to 0, and clean up elsewhere
                    loc.getPlanet()!!.landedShips.add(this)
                    this.health = 0
                    universe.landPopulation(this.loc.getPlanet()!!, race.uid, 1)
                } else {
                    loc.getPlanet()!!.landedShips.add(this)
                }
            }
            ORBIT -> {
                loc.getPlanet()!!.orbitShips.add(this)
            }
            SYSTEM -> {
                loc.getStar()!!.starShips.add(this)
            }
            DEEPSPACE -> {
                universe.deepSpaceShips.add(this)
            }
            else -> {
                gbAssert("Bad Parameters for ship placement $loc", { false })
            }
        }
        this.loc = loc

    }

    fun doShip() {

        if (health > 0) { // for now don't update dead ships...
            moveShip()
            moveOrbitShip()
            trails.add(loc.getLoc())
            while (trails.size > 5) {
                trails.removeAt(0)
            }
        }
    }

    fun killShip() {

        if (health == 0) {

            GBLog.d("Removing dead ship $name from " + this.loc.getLocDesc())

            // TODO factor out ship death (and call it from where pods explode, or set health to 0 there)
            when (this.loc.level) {
                LANDED -> {
                    this.loc.getPlanet()!!.landedShips.remove(this)
                }
                ORBIT -> {
                    this.loc.getPlanet()!!.orbitShips.remove(this)
                }
                SYSTEM -> {
                    this.loc.getStar()!!.starShips.remove(this)
                }
                DEEPSPACE -> {
                    universe.deepSpaceShips.remove(this)
                }
                else -> {
                    gbAssert("Bad Parameters for ship removement $loc", { false })
                }
            }
            this.race.raceShips.remove(this)
            universe.deadShips.add(this)
        }
    }

    fun moveOrbitShip() {
        if ((this.dest == null) && (this.loc.level == ORBIT)) {
            this.loc = GBLocation(this.loc.getPlanet()!!, this.loc.getOLocP().r, this.loc.getOLocP().t + 0.2f)
        }
    }


    fun moveShip() {

        if (dest == null) {
            return
        }

        val dest = this.dest!!
        val dxy = dest.getLoc()       // use getLoc to get universal (x,y)
        val sxy = loc.getLoc()        // TODO no longer true  ? : center of planet for landed and orbit

        GBLog.d("Moving $name from ${this.loc.getLocDesc()} to ${dest.getLocDesc()}.")

        if (loc.level == LANDED) { // We are landed

            GBLog.d(name + " is landed")

            if ((dest.level != loc.level) || (dest.refUID != loc.refUID)) { // landed and we need to get to orbit

                //What direction are we heading
                val t = atan2(dxy.y - sxy.y, dxy.x - sxy.x)

                val next = GBLocation(loc.getPlanet()!!, PLANET_ORBIT_SIZE, t)
                changeShipLocation(next)
                universe.news.add("Launched $name to ${loc.getLocDesc()}.\n")

                return
            } else {
                // here we will deal with surface to surface moves on the same planet
                // for now, any ship that tries is set to have arrived (so the makeStuff script can give it a new location)

                this.dest = null

            }


            return

        } else if ((loc.level == ORBIT) && (loc.refUID == dest.refUID)) {

            // We arrived at the planet of destination

            if (dest.level == ORBIT) { // We arrived in Orbit
                this.dest = null

                return

            } else {
                //dest.level == LANDED
                GBLog.d("$name is in orbit at destination. Landing.")

                // in orbit at destination so we need to land
                changeShipLocation(dest)
                universe.news.add("$name ${loc.getLocDesc()}.\n")

                return

            }
        } else {
            // we are not LANDED, so either in ORBIT, in SYTEM, or in DEEPSPACE

            var distance = sxy.distance(dxy)

            if ((distance - 1f) < speed) { // we will arrive at a planet (i.e. in Orbit) this turn. Can only fly to planets (right now)

                //What direction are we coming from
                val t = atan2(sxy.y - dxy.y, sxy.x - dxy.x)

                var next = GBLocation(dest.getPlanet()!!, PLANET_ORBIT_SIZE, t)
                changeShipLocation(next)
                universe.news.add("$name arrived in ${loc.getLocDesc()}.\n")

                if (dest.level == ORBIT) {
                    this.dest = null

                }
                return
            }

            var nxy = sxy.towards(dxy, speed.toFloat())


            GBLog.d("Flying from (${sxy.x}, ${sxy.y}) direction (${dxy.x}, ${dxy.y}) until (${nxy.x}, ${nxy.y}) at speed $speed\n")


            if (loc.level == DEEPSPACE) {

                var distanceToStar = sxy.distance(dest.getStar()!!.loc.getLoc())


                if (distanceToStar < GBData.SystemBoundary) { // we arrived at destination System

                    // TODO check if destination is the system, in which case we would just stop here.
                    // We can't fly to a system yet, so not a bug just yet.

                    var next = GBLocation(dest.getStar()!!, nxy.x, nxy.y, true)

                    changeShipLocation(next)

                    GBLog.d(" Arrived in System")

                    universe.news.add("$name arrived in ${loc.getLocDesc()}. ( ${loc.x.toInt()} , ${loc.y.toInt()} )\n")
                    return

                } else {
                    var next = GBLocation(nxy.x, nxy.y)
                    changeShipLocation(next)

                    GBLog.d(" Flying Deep Space")

                    //universe.news.add("$name moved in ${loc.getLocDesc()}. ( ${loc.x} , ${loc.y} )\n")
                    return

                }


            } else {

                var distanceToStar = sxy.distance(loc.getStar()!!.loc.getLoc())

                if (distanceToStar > GBData.SystemBoundary) {  // we left the system

                    var next = GBLocation(nxy.x, nxy.y)
                    changeShipLocation(next)

                    GBLog.d("Left System")

                    universe.news.add("$name entered ${loc.getLocDesc()}. ( ${loc.x.toInt()} , ${loc.y.toInt()} )\n")
                    return
                } else {

                    var next = GBLocation(loc.getStar()!!, nxy.x, nxy.y, true)
                    changeShipLocation(next)

                    GBLog.d("Flying insystem ")

                    //universe.news.add("$name moved in ${loc.getLocDesc()}. ( ${loc.x.toInt()} , ${loc.y.toInt()} )\n")
                    return

                }
            }
        }
    }


    fun getStar(): GBStar? {
        return loc.getStar()
    }

}

