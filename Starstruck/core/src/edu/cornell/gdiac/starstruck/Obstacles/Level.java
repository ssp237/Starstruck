package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Galaxy;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.util.JsonAssetManager;

/** Representation of a planet in the game. Has its own mass, size, and range of effect for gravity.
 *  Also stores the galaxy the planet is from, to be used when determining sprite, and
 *
 */
public class Level extends WheelObstacle {

    /** The radius of a planet in [slightly arbitrary] units. */
    protected float radius;
    /** The range from which gravity is effective for this planet (physics units)*/
    public float grange;
    /** Texture for gravity ring */
    protected TextureRegion ringTexture;
    /** Boolean to check if level has been completed */
    protected boolean completed;
    /** Boolean to check if level has been unlocked */
    protected boolean unlocked;

    private float grscale;

    /** Counts the number of levels created to assign names */
    private static int counter = 0;

    /**
     * Constructor. Creates a planet centered at (x,y) with the specified radius, mass and galaxy.
     * @param x The x coordinate of the planet's center
     * @param y The y coordinate of the planet's center
     * @param radius The planet's radius
     */
    public Level(float x, float y, float radius, TextureRegion texture,
                 World world, Vector2 scale, TextureRegion ringTexture) {
        super(x, y, radius);
        this.texture = texture;
        this.ringTexture = ringTexture;
        this.radius = radius;

        setBodyType(BodyDef.BodyType.StaticBody);

        activatePhysics(world);
        setDrawScale(scale);

        //Set drawing scale
        float newScale = (2 * radius * drawScale.x)/texture.getRegionWidth();
        this.scaleDraw = newScale;

        grscale = (float) texture.getRegionWidth() / (float) ringTexture.getRegionWidth();

        setName("level" + counter);
        counter++;
    }

    /**
     * Write this level to a JsonValue. When parsed, this JsonValue should return the same level.
     * @return A JsonValue representing this Planet.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write position
        Vector2 pos = getPosition();
        json.addChild("x", new JsonValue(pos.x));
        json.addChild("y", new JsonValue(pos.y));

        //Add radius
        json.addChild("r", new JsonValue(radius));

        //Add booleans
        json.addChild("unlocked", new JsonValue(unlocked));
        json.addChild("completed", new JsonValue(completed));

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));
        json.addChild("ring texture", new JsonValue(JsonAssetManager.getInstance().getKey(getRingTexture())));

        return json;
    }


    /**
     * Return a new level with parameters specified by the JSON
     * @param json A JSON containing data for one level
     * @param scale The scale to convert physics units to drawing units
     * @return A level created according to the specifications in the JSON
     */
    public static Level fromJSON(JsonValue json, Vector2 scale, World world) {
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        String keyRing = json.get("ring texture").asString();
        TextureRegion ringTexture = JsonAssetManager.getInstance().getEntry(keyRing, TextureRegion.class);

        Level out =  new Level(json.get("x").asFloat(), json.get("y").asFloat(), json.get("r").asFloat(),
            texture, world, scale, ringTexture);

        boolean state = json.get("completed").asBoolean();
        out.setCompleted(state);

        state = json.get("unlocked").asBoolean();
        out.setUnlocked(state);

        return out;
    }

    /**
     * Return the radius of this level
     * @return The radius of this level.
     */
    public float getRadius() { return radius;}

    /**
     * Return the texture of this level
     * @return The texture of this level.
     */
    public TextureRegion getRingTexture() { return ringTexture;}

    /**
     * Set the unlocked state of this level
     */
    public void setUnlocked(boolean state) { unlocked = state;}
    /**
     * Set the completed state of this level
     */
    public void setCompleted(boolean state) { completed = state;}

    /**
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {

        //Draw planet
        canvas.draw(getTexture(), Color.WHITE, origin.x, origin.y,getX() * drawScale.x - texture.getRegionWidth()/(2/scaleDraw),
                getY() * drawScale.x - texture.getRegionHeight()/(2/scaleDraw), getAngle(), scaleDraw,scaleDraw);

        float rScale = grscale * scaleDraw * ((getRadius() + grange) / getRadius());

        Color color = new Color(1,1,1,0.75f);

        //Draw ring
//        canvas.draw(ringTexture, color, origin.x, origin.y,getX() * drawScale.x - ringTexture.getRegionWidth()/(2/rScale),
//                getY() * drawScale.x - ringTexture.getRegionHeight()/(2/rScale), getAngle(), rScale, rScale);

    }

    public String toString() {
        String out = "Level with {";

        out += "pos: " + getPosition() + ", ";
        out += "radius: " + getRadius() + ", ";
        out += "}";

        return out;
    }

    public ObstacleType getType() { return ObstacleType.PLANET;}

}
