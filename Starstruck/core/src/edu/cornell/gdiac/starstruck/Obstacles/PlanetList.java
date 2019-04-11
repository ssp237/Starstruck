package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Galaxy;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.util.ArrayList;

/**
 * An instance represents a list of the planets in one level.
 * Implements iterable.
 * Stores texture files, so that different galaxy themes can be selected.
 */
public class PlanetList {

    /** Textures to be used when drawing planets*/
    private TextureRegion planet1_texture;
    private TextureRegion planet2_texture;
    private TextureRegion planet3_texture;
    private TextureRegion planet4_texture;
    private TextureRegion planet5_texture;
    private TextureRegion planet6_texture;

    /** Texture for gravity ring */
    private TextureRegion gring_texture;

    /** The planets in this PlanetList*/
    private ArrayList<Planet> planets;

    /** Scale to convert physics to pixels */
    private Vector2 scale;

    /**
     *  Constructs a new PlanetList by loading the appropriate sprites
     * @param galaxy The galaxy for the planets in this level.
     * @param scale The drawing scale
     */
    public PlanetList(Galaxy galaxy, Vector2 scale) {
        setPlanetFiles(galaxy);

        planet1_texture = JsonAssetManager.getInstance().getEntry("whirlpool planet 1", TextureRegion.class);
        planet2_texture = JsonAssetManager.getInstance().getEntry("whirlpool planet 2", TextureRegion.class);
        planet3_texture = JsonAssetManager.getInstance().getEntry("whirlpool planet 3", TextureRegion.class);
        planet4_texture = JsonAssetManager.getInstance().getEntry("whirlpool planet 4", TextureRegion.class);
        planet5_texture = JsonAssetManager.getInstance().getEntry("whirlpool planet 5", TextureRegion.class);
        planet6_texture = JsonAssetManager.getInstance().getEntry("whirlpool planet 6", TextureRegion.class);
        gring_texture = JsonAssetManager.getInstance().getEntry("gravity ring", TextureRegion.class);

        planets = new ArrayList<Planet>();

        this.scale = scale;
    }

    /**
     * Sets the planet sprites based on the current galaxy.
     * @param galaxy The galaxy who's planet sprites should be used.
     */
    private void setPlanetFiles(Galaxy galaxy) {
        switch(galaxy){
            case WHIRLPOOL: setWhirlpoolSprites(); break;
            case DEFAULT :
            default : setDefaultSprites();

        }
    }

    /**
     * Sets the planet files to the default sprite options.
     */
    private void setDefaultSprites(){

    }

    /**
     * Sets the planet files to the whirlpool sprite options.
     */
    private void setWhirlpoolSprites(){

    }

    /**
     * Returns the texture for the 0-indexed i'th planet sprite in the current galaxy.
     * @param i The planet sprite to return in the current galaxy (indexed by 0)
     * @return The 0-indexed i'th planet sprite in the current galaxy.
     */
    private TextureRegion getPlanetTexture(int i) {
        switch(i) {
            case 0:
                return planet1_texture;
            case 1:
                return planet2_texture;
            case 2:
                return planet3_texture;
            case 3:
                return planet4_texture;
            case 4:
                return planet5_texture;
            case 5:
                return planet6_texture;
            default:
                return planet1_texture;
        }
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
    public void addPlanet(float x, float y, int i, World world, VectorWorld vectorWorld) {
        Planet p = new Planet(x, y, i, world, scale);
        vectorWorld.addPlanet(p);
        planets.add(p);
    }

    /**
     * Add the planet specified by JsonValue json to the PlanetList using World world and VectorWorld vectorWorld.
     * @param json JsonValue containing data for the planet to be added.
     * @param world World this planet exists in.
     * @param vectorWorld VectorWorld controlling gravity for this planet.
     */
    public void addPlanet(JsonValue json, World world, VectorWorld vectorWorld) {
        float x = json.getFloat("x");
        float y = json.getFloat("y");
        int i = json.getInt("i");
        addPlanet(x,y,i,world,vectorWorld);
    }

    /**
     * Add the specified planet to the planet list
     * @param x X coordinate of planet's origin
     * @param y Y coordinate of planet's origin
     * @param radius Planet's radius
     * @param mass Planet's mass
     * @param grange The range from which gravity is effective for this planet
     * @param sprite Sprite to be used when drawing this planet
     * @param world World that owns this planet
     * @param vectorWorld VectorWorld that controls this planet's gravity
     */
    public void addPlanet(float x, float y, float radius, float mass, float grange,
                          int sprite, World world, VectorWorld vectorWorld) {
        TextureRegion texture = getPlanetTexture(sprite);
        Planet p = new Planet(x, y, radius, mass, grange, texture, world, scale, gring_texture);
        vectorWorld.addPlanet(p);
        planets.add(p);
    }

    /**
     * Add the specified planets to the list.
     * @param planetSpecs A 2D array of specifications for the planets to be constructed. Each row specifies
     *                    a planet, and must contain in order: x coord, y coord, radius, mass, draw scale,
     *                    gravitation range, planet sprite to use (1-4).
     *                    Will probably need to edit eventually.
     * @param vectorWorld The vectorWorld controlling the gravity for this level
     * @param world The world idk why we need this but sure
     */
    public void addPlanets(float[][] planetSpecs, World world, VectorWorld vectorWorld) {
        for (int i = 0; i < planetSpecs.length; i++) {

            addPlanet(planetSpecs[i][0], planetSpecs[i][1], planetSpecs[i][2], planetSpecs[i][3],
                    planetSpecs[i][4], (int) planetSpecs[i][5], world, vectorWorld);

            //Vector2 pos = new Vector2(obj.getBody().getPosition().x, obj.getBody().getPosition().y - obj.getRadius());
            //vectorWorld.addPlanet(obj, PLANETS[i][3], obj.getCenter()); //Radius parameter is temporary fix for why center is off
        }
    }

    /**
     * Helper to find distance
     *
     * @param v1 v1
     * @param v2 v2
     * @return distance between v1 and v2
     */
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
            if (dist > minDist) {
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