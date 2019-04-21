/*
 * Star.java
 *
 * This class provides a spinning rectangle on a fixed pin.  We did not really need
 * a separate class for this, as it has no update.  However, ComplexObstacles always
 * make joint management easier.
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don HolHden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.math.collision.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.util.JsonAssetManager;

public class Star extends BoxObstacle {
//    /** The debug name for the entire obstacle */
//    private static final String SPINNER_NAME = "star_spinner";
//    /** The debug name for the spinning barrier */
//    private static final String BARRIER_NAME = "star_barrier";
//    /** The debug name for the central pin */
//    private static final String SPIN_PIN_NAME = "star_pin";
//    /** The density for most physics objects */
//    private static final float LIGHT_DENSITY = 0.0f;
//    /** The density for a bullet */
//    private static final float HEAVY_DENSITY = 10.0f;
//    /** The radius of the central pin */
//    private static final float SPIN_PIN_RADIUS = 0.1f;
//
//    /** The primary spinner obstacle */
//    private BoxObstacle barrier;
//    private WheelObstacle pivot;

    /** To be removed */
    protected boolean remove = false;
    /** Location of this star */
    private String location;

    /** Ray cache for calculating intersection */
    private Ray rayCache;

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
    public Star(float width, float height) {
        this(0,0,width,height);
    }

    /**
     * Creates a new spinner at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Star(float x, float y, float width, float height) {
        super(x,y,width,height);
        setName("star");
//        setName(SPINNER_NAME);
//
//        // Create the barrier
//        barrier = new BoxObstacle(x,y,width,height);
//        barrier.setName(BARRIER_NAME);
//        barrier.setDensity(HEAVY_DENSITY);
//        bodies.add(barrier);
//
//        //#region INSERT CODE HERE
//        // Create a pin to anchor the barrier
//        pivot = new WheelObstacle(x,y,SPIN_PIN_RADIUS);
//        pivot.setName(SPIN_PIN_NAME);
//        pivot.setDensity(LIGHT_DENSITY);
//        pivot.setBodyType(BodyDef.BodyType.StaticBody);
//        bodies.add(pivot);

        // Radius: SPIN_PIN_RADIUS
        // Density: LIGHT_DENSITY

        //#endregion
    }

    /**
     * Create a new star at (x,y) with the given texture and draw scale.
     * @param x X coord of the new star
     * @param y Y coord of the new star
     * @param texture Texture for the new star
     * @param scale Draw scale for the new star
     */
    public Star(float x, float y, TextureRegion texture, Vector2 scale) {
        this(x,y,texture.getRegionWidth()/scale.x,texture.getRegionHeight()/scale.y);
        setDrawScale(scale);
        setTexture(texture);
    }

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param location Whether this star is in space or on planet
     */
    public Star(float x, float y, float width, float height, String location) {
        this(x, y, width, height);
        this.location = location;
    }

    public String getLoc() { return location; }

    /**
     * Return a new star with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static Star fromJSON(JsonValue json, Vector2 scale) {
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        String location = json.get("location").asString();
        Star out =  new Star(json.get("x").asFloat(), json.get("y").asFloat(),
                texture.getRegionWidth()/scale.x, texture.getRegionHeight()/scale.y, location);
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

        //Add location
        json.addChild("location", new JsonValue(location));

        //System.out.println(json);

        return json;
    }


    /**
     * Creates the joints for this object.
     *
     * We implement our custom logic here.
     *
     * world Box2D world to store joints
     *
     * @return true if object allocation succeeded
     */
//    protected boolean createJoints(World world) {
//        assert bodies.size() > 0;
//
//        //#region INSERT CODE HERE
//        // Attach the barrier to the pin here
//
//        RevoluteJointDef jointDef = new RevoluteJointDef();
//
//        jointDef.bodyA = barrier.getBody();
//        jointDef.bodyB = pivot.getBody();
//        jointDef.collideConnected = false;
//        Joint joint = world.createJoint(jointDef);
//        joints.add(joint);
//
//        //#endregion
//
//        return true;
//    }

    public String toString() {
        String out = "Star with {";

        out += "pos: " + getPosition();
        out += "}";

        return out;
    }

//    public void setTexture(TextureRegion texture) {
//        setTexture(texture);
//    }
//
//    public TextureRegion getTexture() {
//        return getTexture();
//    }

    public boolean getRemove() { return remove; }

    public void setRemove(boolean value) { remove = value; }

    /**
     * Copyright 2000 softSurfer, 2012 Dan Sunday
     *
     * Determines whether p is the left, right, or on the vector from v0 to v1
     * >0 for on the left,  =0 for on the line, <0 for on the right
     *
     * @param v0 The start of the vector
     * @param v1 The end of the vector
     * @param p The point
     * @return A number less than, greater than, or equal to 0
     */
    private float isLeft(Vector2 v0, Vector2 v1, Vector2 p) {
        return (v1.x-v0.x)*(p.y-v0.y) - (p.x-v0.x)*(v1.y-v0.y);
    }

    /**
     * Collects this star
     *
     * @param vertices A list of positions of vertices of the polygon -- aka rope joints
     * @return True if this star is collected and needs to be destroyed, false otherwise
     */
    public boolean collect(Vector2[] vertices) {
        Vector2 pos = this.getPosition();
        int wn = 0;
        for (int i = 0; i < vertices.length - 1; i++) {
            Vector2 vertex0 = vertices[i];
            Vector2 vertex1 = vertices[i + 1];

            //If edge goes upwards and star is to the left, increase wn
            if (pos.y >= vertex0.y && pos.y < vertex1.y) {
                if (isLeft(vertex0, vertex1, pos) > 0) {
                    wn++;
                }
            }
            //If edge goes downwards and star is to the right, decrease wn
            else if (pos.y < vertex0.y && pos.y >= vertex1.y) {
                if (isLeft(vertex0, vertex1, pos) < 0) {
                    wn--;
                }
            }
        }

        if (wn != 0) { //Inside the polygon
            return true;
        }
        return false;
    }

    public ObstacleType getType() { return ObstacleType.STAR;}

    public boolean containsPoint(Vector2 point) {
        return containsPoint(point);
        //return barrier.containsPoint(point) || pivot.containsPoint(point);
    }
}
