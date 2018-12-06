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
import sun.font.GlyphLayout
import java.util.*

class GBShip(val idxtype: Int, val race: GBRace, var loc: GBLocation) {

    val id: Int
    val uid: Int
    val rid: Int
    val name: String
    val type: String
    val speed: Int
    var health: Int

    var dest: GBLocation? = null
    private val trail = Collections.synchronizedList(mutableListOf<GBxy>())

    @Synchronized
    fun getTrailList() : List<GBxy> {
     addToTrail() // TODO: This shouldn't be necessary here on every get.. Need to fix at trail maintenance elsewhere
     return trail.toList()
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
                universe.universeShips.add(this)
            }
            else -> {
                gbAssert("Bad Parameters for ship placement $loc", { false })
            }

        }

        type = GBData.getShipType(idxtype)
        name = race.name.first().toString() + type.first().toString() + race.raceShips.indexOf(this)
        speed = GBData.getShipSpeed(idxtype)
        health = 100
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
                universe.universeShips.remove(this)
            }
            else -> {
                gbAssert("Bad Parameters for ship removement $loc", { false })
            }
        }
        when (loc.level) {
            LANDED -> {
                if (idxtype == POD) {
                    // TODO We should really handle this somewhere else. If pod were a subtype of ship, it could overwrite
                    // This is a pod, they populate, then destroy.
                    // We set health to 0, and clean up elsewhere
                    this.health = 0
                    universe.landPopulation(this.loc.getPlanet()!!, race.uid, 1)
                    //
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
                universe.universeShips.add(this)
            }
            else -> {
                gbAssert("Bad Parameters for ship placement $loc", { false })
            }
        }
        this.loc = loc

    }

    fun doShip() {
        //removeDeadShips()
        moveShip()
    }

    @Synchronized
    fun trimTrail() {
        while (trail.size > 10) {
            trail.removeAt(0)
        }
    }

    @Synchronized
    fun addToTrail() {
        trail.add(loc.getLoc())
    }

    fun removeDeadShips() {
        for (sh in universe.allShips) {
            if (sh.health == 0) {
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
                        universe.universeShips.remove(this)
                    }
                    else -> {
                        gbAssert("Bad Parameters for ship removement $loc", { false })
                    }
                }
                sh.race.raceShips.remove(this)
                universe.deadShips.add(this)
                //universe.allShips.remove(this)
            }
        }
    }

    fun moveShip() {

        if (this.health == 0) {
            GBLog.d("Trying to move dead ship $name") // TODO Performance. Assert this never happens
            return
        }

        trimTrail()

        if (dest == null) {
            return
        }

        addToTrail()

        val dest = this.dest!!
        val dxy = dest.getLoc()       // use getLoc to get universal (x,y)
        val sxy = loc.getLoc()        // center of planet for landed and orbit

        GBLog.d("Moving $name from ${this.loc.getLocDesc()} to ${dest.getLocDesc()}.")

        if (loc.level == LANDED) { // We are landed

            GBLog.d(name + " is landed")

            if ((dest.level != loc.level) || (dest.refUID != loc.refUID)) { // landed and we need to get to orbit

                var next = GBLocation(loc.getPlanet()!!, 1f, 1f)
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

            GBLog.d("$name is in orbit at destination. Landing.")

            // in orbit at destination so we need to land
            changeShipLocation(dest)
            universe.news.add("$name ${loc.getLocDesc()}.\n")
            this.dest = null

            return

        } else {
            // we are not LANDED, so either in ORBIT, in SYTEM, or in DEEPSPACE

            var distance = sxy.distance(dxy)

            if (distance < speed) { // we will arrive at a planet (i.e. in Orbit) this turn. Can only fly to planets (right now)

                var next = GBLocation(dest.getPlanet()!!, 1f, 1f)
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


                if (distanceToStar < GBData.getSystemRadius()) { // we arrived at destination System

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

                if (distanceToStar > GBData.getSystemRadius()) {  // we left the system

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

