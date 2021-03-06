package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Galaxy;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Models.Bug;
import edu.cornell.gdiac.util.JsonAssetManager;

/** Representation of a planet in the game. Has its own mass, size, and range of effect for gravity.
 *  Also stores the galaxy the planet is from, to be used when determining sprite, and
 *
 */
public class Planet extends WheelObstacle {

    /** Number of possible planets */
    public static final int NUM_PLANETS = 4;
    /** The mass of a planet in [slightly arbitrary] units. */
    protected float mass;
    /** The range from which gravity is effective for this planet (physics units)*/
    public float grange;
    /** Texture for gravity ring */
    protected TextureRegion ringTexture;
    /** Identifier for index of planet.*/
    private int ind;
    /** True if this planet has a bug.*/
    private boolean bug_bool;
    private Bug buggy;


    /** Pre-set masses */
    private static float mass1 = 700;
    private static float mass2 = 800;
    private static float mass3 = 900;
    private static float mass4 = 1000;

    /** Current galaxy for drawing */
    private static Galaxy galaxy = Galaxy.WHIRLPOOL;

    private float grscale;

    /** Counts the number of planets created to assign names */
    private static int counter = 0;

    /**
     * Constructor. Creates a planet centered at (x,y) with the specified radius, mass and galaxy.
     * @param x The x coordinate of the planet's center
     * @param y The y coordinate of the planet's center
     * @param radius The planet's radius
     * @param mass The planet's mass
     */
    public Planet(float x, float y, float radius, float mass, float grange,
                  TextureRegion texture, World world, Vector2 scale, TextureRegion ringTexture, Bug bug) {
        super(x, y, radius);
        this.mass = mass;
        this.texture = texture;
        this.grange = grange;
        this.ringTexture = ringTexture;

        if (bug == null) {
            this.bug_bool = false;
        }
        else {
            this.bug_bool = true;
        }

        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(500f);

        activatePhysics(world);
        setDrawScale(scale);

        //Set drawing scale
        float newScale = (2 * radius * drawScale.x)/texture.getRegionWidth();
        this.scaleDraw = newScale;

        grscale = (float) texture.getRegionWidth() / (float) ringTexture.getRegionWidth();

        setName("planet" + counter);
        counter++;

        ind = 0;

        // Not needed?
        //setFriction(BASIC_FRICTION);
        //setRestitution(BASIC_RESTITUTION);
    }

    /**
     *  Create a copy of the i'th planet centered at (x,y) existing in World world with draw scale scale.
     * @param x X coord of center of planet.
     * @param y Y coord of center of planet.
     * @param i Index of planet to be created.
     * @param world World the planet exists in.
     * @param scale Draw scale for this planet.
     */
    public Planet(float x, float y, int i, World world, Vector2 scale, Bug bug) {
        super(x,y, 0);

        if (i > 0) {
            i = ((i - 1) % NUM_PLANETS) + 1;
        } else {
            i = ((i - 1) % NUM_PLANETS) + 1 + NUM_PLANETS;
        }

        if (bug == null) {
            this.bug_bool = false;
        }
        else {
            this.bug_bool = true;
            buggy = bug;
        }

        String gal = galaxy.getChars();

        //System.out.println(gal + " p" + i);

        texture = JsonAssetManager.getInstance().getEntry(( gal + " p" + i), TextureRegion.class);
        ringTexture = JsonAssetManager.getInstance().getEntry((gal + " g" + i), TextureRegion.class);

        //System.out.println(texture);

        float radius = texture.getRegionWidth() / (scale.x * 2);
        setRadius(radius);
        grange = (ringTexture.getRegionWidth()/(scale.x * 2)) - radius;

        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(500f);

        activatePhysics(world);
        setDrawScale(scale);

        mass = massOfInd(i);

        //Set drawing scale
        float newScale = (2 * radius * drawScale.x)/texture.getRegionWidth();
        this.scaleDraw = newScale;

        grscale = (float) texture.getRegionWidth() / (float) ringTexture.getRegionWidth();

        setName("planet" + counter);
        counter++;

        ind = i;
    }

    /**
     * Set da bug
     * @param bug da bug
     */
    public void setBug (Bug bug) {
        buggy = bug;
        bug_bool = true;
    }

    /**
     * Get da bug (null if no bug)
     * @return da bug
     */
    public Bug getBug(){
        return buggy;
    }

    /**
     * Remove da bug
     */
    public void removeBug() {
        buggy = null;
        bug_bool = false;
    }

    /**
     * Return the index of this planet.
     *
     * @return The index of this planet.
     */
    public int getInd() {return ind;}

    private float massOfInd(int i) {
        switch (i) {
            case 1: return mass1;
            case 2: return mass2;
            case 3: return mass3;
            case 4: return mass4;
            default: return mass1;
        }
    }

    /**
     * Set the preset values for planet construction according to the JSON we
     * read in.
     * @param json The json containing data to set preset values.
     */
    public static void setPresets(JsonValue json) {
        float[] vals = json.asFloatArray();
        mass1 = vals[0];
        mass2 = vals[1];
        mass3 = vals[2];
        mass4 = vals[3];
    }

    /**
     * Set the galaxy to galaxy.
     * @param galaxy The galaxy to set as the current galaxy.
     */
    public static void setGalaxy(Galaxy galaxy){
        Planet.galaxy = galaxy;
    }

    /**
     *  Set the current galaxy to the galaxy represented by string S.
     * @param s A string representing the galaxy t set as the new current galaxy.
     */
    public static void setGalaxy(String s) {
        setGalaxy(Galaxy.fromString(s));
    }

    /**
     * Return a JsonValue representing the preset mass values for planets such that this JsonValue could
     * be passed into setPresets() to return the same preset mass values.
     *
     * @return A JsonValue representing the preset mass values for planets.
     */
    public static JsonValue presetJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.array);

        json.addChild("p1 mass", new JsonValue(mass1));
        json.addChild("p2 mass", new JsonValue(mass2));
        json.addChild("p3 mass", new JsonValue(mass3));
        json.addChild("p4 mass", new JsonValue(mass4));

        //System.out.println(json);

        return json;
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same planet.
     * @return A JsonValue representing this Planet.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write position
        Vector2 pos = getPosition();
        json.addChild("x", new JsonValue(pos.x));
        json.addChild("y", new JsonValue(pos.y));

        //Add index
        json.addChild("i", new JsonValue(ind));

        //System.out.println(json);

        if (bug_bool) {
            json.addChild("bug", buggy.toJson());
        }

        return json;
    }

    /**
     * Return the mass of this planet
     * @return The mass of this planet.
     */
    public float getMass() { return mass;}

    /**
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {

        float rScale = grscale * scaleDraw * ((getRadius() + grange) / getRadius());

        Color color = new Color(1,1,1,0.75f);

        //Draw ring
        canvas.draw(ringTexture, color, origin.x, origin.y,getX() * drawScale.x - ringTexture.getRegionWidth()/(2/rScale),
                getY() * drawScale.x - ringTexture.getRegionHeight()/(2/rScale), getAngle(), rScale, rScale);

        //System.out.println(JsonAssetManager.getInstance().getKey(getTexture()));
        //Draw planet
        canvas.draw(getTexture(), Color.WHITE, origin.x, origin.y,getX() * drawScale.x - texture.getRegionWidth()/(2/scaleDraw),
                getY() * drawScale.x - texture.getRegionHeight()/(2/scaleDraw), getAngle(), scaleDraw,scaleDraw);

    }

    public String toString() {
        String out = "Planet with {";

        out += "pos: " + getPosition() + ", ";
        out += "radius: " + getRadius() + ", ";
        out += "mass: " + mass;
        out += "}";

        return out;
    }

    public ObstacleType getType() { return ObstacleType.PLANET;}


    public static float getRadiusPrePlanet(int i, Vector2 scale) {
        if (i > 0) {
            i = ((i - 1) % NUM_PLANETS) + 1;
        } else {
            i = ((i - 1) % NUM_PLANETS) + 1 + NUM_PLANETS;
        }

        TextureRegion texture = JsonAssetManager.getInstance().getEntry(("wp p" + i), TextureRegion.class);

        float radius = texture.getRegionWidth() / (scale.x * 2);
        return radius;
    }

}
