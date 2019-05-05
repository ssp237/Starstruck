package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Models.Bug;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;


import java.util.ArrayList;

/**
 * An instance represents a list of the planets in one level.
 * Implements iterable.
 * Stores texture files, so that different galaxy themes can be selected.
 */
public class PlanetList {

    /** The planets in this PlanetList*/
    private ArrayList<Planet> planets;

    /** Scale to convert physics to pixels */
    private Vector2 scale;

    /**
     *  Constructs a new PlanetList by loading the appropriate sprites
     * @param scale The drawing scale
     */
    public PlanetList(Vector2 scale) {

        planets = new ArrayList<Planet>();

        this.scale = scale;
    }

    /**
     * Return a reference to the array of planets.
     * @return a reference to the array of planets.
     */
    public ArrayList<Planet> getPlanets(){
        return planets;
    }

    /**
     * Add the specified planet to the planet list
     * @param p The planet to be added
     */
    public void addPlanet(Planet p, VectorWorld vectorWorld) {
        vectorWorld.addPlanet(p);
        planets.add(p);
    }

    /**
     * Add planet i centered at (x,y) to the planet list using World world and VectorWorld vectorWorld.
     * @param x X coord of center of planet.
     * @param y Y coord of center of planet.
     * @param i Index of planet to create.
     * @param world World this planet exists in.
     * @param vectorWorld VectorWorld controlling gravity for this planet.
     */
    public void addPlanet(float x, float y, int i, World world, VectorWorld vectorWorld, Bug bug) {
        Planet p = new Planet(x, y, i, world, scale, bug);
        vectorWorld.addPlanet(p);
        planets.add(p);
    }

    /**
     * Add the planet specified by JsonValue json to the PlanetList using World world and VectorWorld vectorWorld.
     * @param json JsonValue containing data for the planet to be added.
     * @param world World this planet exists in.
     * @param vectorWorld VectorWorld controlling gravity for this planet.
     */
    public void addPlanet(JsonValue json, World world, VectorWorld vectorWorld, Bug buggy) {
        float x = json.getFloat("x");
        float y = json.getFloat("y");
        int i = json.getInt("i");

        Planet p = new Planet(x,y,i,world, scale, buggy);
        addPlanet(p,vectorWorld);

        if (buggy != null) {
            buggy.setPlanet(p);
        }
    }

//    /**
//     * Helper to find distance
//     *
//     * @param v1 v1
//     * @param v2 v2
//     * @return distance between v1 and v2
//     */
//    public void addPlanet(float x, float y, float radius, float mass, float grange,
//                          int sprite, World world, VectorWorld vectorWorld) {
//        TextureRegion texture = getPlanetTexture(sprite);
//        Planet p = new Planet(x, y, radius, mass, grange, texture, world, scale, gring_texture, null);
//        vectorWorld.addPlanet(p);
//        planets.add(p);
//    }
    private float dist(Vector2 v1, Vector2 v2) {
        return (float) Math.sqrt((v1.x - v2.x)*(v1.x-v2.x) + (v1.y - v2.y) * (v1.y - v2.y));
    }

    /**
     * Finds and returns the direction of the planet with the nearest surface
     * Assumes that planetList is not empty
     *
     * @param pos The point at which to find the nearest planet
     */
    public Vector2 toNearest(Vector2 pos) {
        ArrayList<Planet> planets = getPlanets();
        if (planets.size() == 0) System.out.println("PLANET LIST EMPTY");
        Planet minPlanet = planets.get(0);
        float minDist;
        Vector2 dir = pos.cpy().sub(minPlanet.getPosition());
        dir.setLength(minPlanet.getRadius());
        Vector2 surface = minPlanet.getPosition().cpy().add(dir);
        float dist = dist(pos, surface);
        minDist = dist;
        for (Planet p : planets) {
            dir = pos.cpy().sub(p.getPosition());
            dir.setLength(p.getRadius());
            surface = p.getPosition().cpy().add(dir);
            dist = dist(pos, surface);
            if (dist < minDist) {
                minDist = dist;
                minPlanet = p;
            }
        }

        return minPlanet.getPosition().cpy().sub(pos).nor();
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same planetList.
     * @return A JsonValue representing this planetList.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.array);

        for (Planet p : planets) {
            json.addChild(p.toJson());
        }

        return json;
    }

    /**
     * Remove all planets
     */
    public void clear() {
        planets = new ArrayList<Planet>();
    }

    /**
     * Remove planet p from the PlanetList
     *
     * @param p The planet to be removed
     */
    public void remove(Planet p) {
        planets.remove(p);
    }

    public String toString() {
        return planets.toString();
    }

    public int size() { return planets.size(); }
}