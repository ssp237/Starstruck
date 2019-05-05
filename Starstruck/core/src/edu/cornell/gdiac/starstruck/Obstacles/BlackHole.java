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

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.starstruck.GameCanvas;
import com.badlogic.gdx.graphics.Color;

public class BlackHole extends BoxObstacle {

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
    public BlackHole(float width, float height) {
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
    public BlackHole(float x, float y, float width, float height) {
        super(x,y, width, height);
    }

    /**
     * Create a new anchor at (x,y) with the given texture and draw scale.
     * @param x X coord of the new anchor
     * @param y Y coord of the new anchor
     * @param texture Texture for the new anchor
     * @param scale Draw scale for the new anchor
     */
    public BlackHole(float x, float y, TextureRegion texture, Vector2 scale) {
        this(x,y,texture.getRegionWidth()/scale.x,texture.getRegionHeight()/scale.y);
        setDrawScale(scale);
        setTexture(texture);
    }

    public void moveWin(AstronautModel leadAstro, AstronautModel trailAstro) {
    }


    public String toString() {
        String out = "Goal with {";

        out += "pos: " + getPosition();
        out += "}";

        return out;
    }

    public ObstacleType getType() { return ObstacleType.GOAL;}

    /**
     * Return a new anchor with parameters specified by the JSON
     * @param json A JSON containing data for one anchor
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static BlackHole fromJson(JsonValue json, Vector2 scale) {
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        float[] pos  = json.get("pos").asFloatArray();
        float posX = pos[0], posY = pos[1];
        BlackHole out =  new BlackHole(posX, posY, texture, scale);
        return out;
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write position
        Vector2 pos = getPosition();
        JsonValue position = new JsonValue(JsonValue.ValueType.array);
        position.addChild(new JsonValue(pos.x));
        position.addChild(new JsonValue(pos.y));

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));

        //System.out.println(json);

        return json;
    }
}
