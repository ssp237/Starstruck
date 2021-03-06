/*
 * Anchor.java
 *
 * This class provides a spinning rectangle on a fixed pin.  We did not really need
 * a separate class for this, as it has no update.  However, ComplexObstacles always
 * make joint management easier.
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Galaxy;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class Anchor extends WheelObstacle {
    private FilmStrip animatedText;
    private Galaxy galaxy;

//    /** The debug name for the entire obstacle */
//    private static final String SPINNER_NAME = "anchor_spinner";
//    /** The debug name for the spinning barrier */
//    private static final String BARRIER_NAME = "anchor_barrier";
//    /** The debug name for the central pin */
//    private static final String SPIN_PIN_NAME = "anchor_pin";
//    /** The density for most physics objects */
//    private static final float LIGHT_DENSITY = 0.0f;
//    /** The density for a bullet */
//    private static final float HEAVY_DENSITY = 10.0f;
//    /** The radius of the central pin */
//    private static final float SPIN_PIN_RADIUS = 0.1f;
//
//    /** The primary spinner obstacle */
//    private BoxObstacle barrier;
//
//    private WheelObstacle pivot;

    /**
     * Creates a new spinner at the origin.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
//    public Anchor(float width, float height) {
//        this(0,0,width,height);
//    }

    /**
     * Creates a new spinner at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param radius
     */
    public Anchor(float x, float y, float radius) {
        super(x,y, radius*1.5f);
        setName("anchor");
        animatedText = JsonAssetManager.getInstance().getEntry("anchor animated", FilmStrip.class);

//        setName(SPINNER_NAME);
//
//        // Create the barrier
//        barrier = new BoxObstacle(x,y,width,height);
//        barrier.setName(BARRIER_NAME);
//        barrier.setDensity(HEAVY_DENSITY);
//        bodies.add(barrier);
//
//        // Create a pin to anchor the barrier
//        pivot = new WheelObstacle(x,y,SPIN_PIN_RADIUS);
//        pivot.setName(SPIN_PIN_NAME);
//        pivot.setDensity(LIGHT_DENSITY);
//        pivot.setBodyType(BodyDef.BodyType.StaticBody);
//        bodies.add(pivot);

        // Radius: SPIN_PIN_RADIUS
        // Density: LIGHT_DENSITY
    }

    /**
     * Create a new anchor at (x,y) with the given texture and draw scale.
     * @param x X coord of the new anchor
     * @param y Y coord of the new anchor
     * @param texture Texture for the new anchor
     * @param scale Draw scale for the new anchor
     */
    public Anchor(float x, float y, TextureRegion texture, Vector2 scale) {
        this(x,y,texture.getRegionWidth()/scale.x/2);
        setDrawScale(scale);
        setTexture(texture);
    }

    public void setGalaxy(Galaxy gal) { galaxy = gal; }

    public void update(float dt) {
        animatedText.tick();
    }

    /**
     * Return a new anchor with parameters specified by the JSON
     * @param json A JSON containing data for one anchor
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static Anchor fromJSON(JsonValue json, Vector2 scale) {
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        Anchor out =  new Anchor(json.get("x").asFloat(), json.get("y").asFloat(),
                texture.getRegionWidth()/scale.x/2);
        out.setDrawScale(scale);
        out.setTexture(texture);
        return out;
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write position and size
        Vector2 pos = getPosition();

        json.addChild("x", new JsonValue(pos.x));
        json.addChild("y", new JsonValue(pos.y));

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));

        //System.out.println(json);

        return json;
    }

    public String toString() {
        String out = "Anchor with {";

        out += "pos: " + getPosition();
        out += "}";

        return out;
    }

//    public void setTexture(TextureRegion texture) {
//        barrier.setTexture(texture);
//    }
//
//    public TextureRegion getTexture() {
//        return barrier.getTexture();
//    }

    public ObstacleType getType() { return ObstacleType.ANCHOR;}

    public boolean containsPoint(Vector2 point) {
        return super.containsPoint(point);
        //return barrier.containsPoint(point) || pivot.containsPoint(point);
    }

    public void draw(GameCanvas canvas) {
        if (galaxy == Galaxy.SOMBRERO){
            canvas.draw(animatedText, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.x, getAngle(), 1, 1);
        }
        else {
            canvas.draw(animatedText, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.x, getAngle(), 1, 1);
        }
    }
}
