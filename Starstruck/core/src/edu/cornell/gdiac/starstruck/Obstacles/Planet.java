package edu.cornell.gdiac.starstruck.Obstacles;

import edu.cornell.gdiac.starstruck.Galaxy;

/** Representation of a planet in the game. Has its own mass, size, and range of effect for gravity.
 *  Also stores the galaxy the planet is from, to be used when determining sprite, and
 *
 */
public class Planet extends WheelObstacle {

    /** The possible sprites for a planet*/


    /** The mass of a planet in [slightly arbitrary] units. */
    protected float mass;
    /** The galaxy this planet is in (kind of useless for now, eventually to be used for auto-setting
     * sprites) */
    protected Galaxy galaxy;


    /**
     * Constructor. Creates a planet centered at (x,y) with the specified radius, mass and galaxy.
     * @param x The x coordinate of the planet's center
     * @param y The y coordinate of the planet's center
     * @param radius The planet's radius
     * @param mass The planet's mass
     * @param galaxy The galaxy the planet is from.
     */
    public Planet(float x, float y, float radius, float mass, Galaxy galaxy){
        super(x, y, radius);
        this.mass = mass;
        this.galaxy = galaxy;
    }


}
