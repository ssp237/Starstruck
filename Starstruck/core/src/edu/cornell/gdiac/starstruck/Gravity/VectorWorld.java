package edu.cornell.gdiac.starstruck.Gravity;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.starstruck.Obstacles.*;

import java.util.ArrayList;

/**
 * Class to represent the items in a world that exert a force of gravity on the player (i.e. planets).
 * Includes methods to add and remove planets, and to find the force experienced at a point.
 */
public class VectorWorld {

    /** Number of points in the vector field (x direction)*/
    private final int NUM_VX = 50;
    /** Number of points in the vector field (y direction)*/
    private final int NUM_VY = 50;
    /** Gravitational constant (will probably change)*/
    private final float G = 2.66E-2f;

    private ArrayList<Planet> planets;

    /**
     *  Initializes a VectorWorld to contain no planets.
     */
    public VectorWorld(){
        planets = new ArrayList<Planet>();
    }

    /**
     * Add the specified planet to the VectorWorld (i.e. it will be used when calculating gravity).
     * @param p The planet to add.
     */
    public void addPlanet(Planet p) {
        planets.add(p);
    }

    /**
     * Remove the specified planet from the VectorWorld (i.e. it will no longer be used when calculating gravity).
     * Return true if the planet was successfully removed, false if it was not (or was not in the VectorWorld).
     * @param p The planet to be removed.
     * @return True if the planet was removed, false if it was not (e.g. it was not in the VectorWorld).
     */
    public boolean removePlanet(Planet p) {
        if (planets.contains(p)) {
            planets.remove(p);
            return true;
        }
        return false;
    }

    /**
     *  Return the force of gravity experienced at position pos exerted by planet p..
     * @param pos The position of the body experiencing gravity.
     * @param p the planet exerting gravity on the body.
     * @return The
     */
    private Vector2 forceToPlanet(Vector2 pos, Planet p) {

        float dist = dist(pos, p.getPosition());
        float mag = (G * p.getMass()) / (dist * dist);

        float dx = pos.x - p.getPosition().x; float dy = pos.y - p.getPosition().y;

        float fx = (dx/dist) * mag; float fy = (dy/dist) * mag;


        return new Vector2(fx, fy);
    }

    /**
     * Returns the effective force experienced at the position with coordinates pos.
     * @param pos A Vector2 representing a position.
     * @return A Vector2 representing the effective force experienced at position pos.
     */
    public Vector2 getForce(Vector2 pos){
        Vector2 totalForce = new Vector2(0,0);
        //System.out.println(planets);
        for (Planet p : planets) {
            if (dist(pos, p.getPosition()) < p.grange + p.getRadius()) {
                totalForce.add(forceToPlanet(pos, p));
            }
        }
        return totalForce;
    }

    private float dist(Vector2 v1, Vector2 v2) {
        return (float) Math.sqrt((v1.x - v2.x)*(v1.x-v2.x) + (v1.y - v2.y) * (v1.y - v2.y));
    }

    /**
     * Add all planets given to the VectorWorld
     * @param orbs The PlanetList of planets to add to the VectorWorld.
     */
    public void addAll(PlanetList orbs) {
        planets.addAll(orbs.getPlanets());
    }




}
