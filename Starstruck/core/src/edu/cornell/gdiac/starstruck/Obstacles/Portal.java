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
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.util.FilmStrip;

public class Portal extends BoxObstacle {

    /** TODO */
    private FilmStrip texture;
    /** Which portal this is -- 1 or 2*/
    private int portNum;
    /** The name of this portal pair */
    private String portName;

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
    public Portal(float width, float height) {
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
    public Portal(float x, float y, float width, float height) {
        super(x,y, width, height);
    }

    public Portal(float x, float y, float width, float height, int n) {
        this(x, y, width, height);
        portNum = n;
    }

    /**
     * Create a new anchor at (x,y) with the given texture and draw scale.
     * @param x X coord of the new anchor
     * @param y Y coord of the new anchor
     * @param texture Texture for the new anchor
     * @param scale Draw scale for the new anchor
     */
    public Portal(float x, float y, TextureRegion texture, Vector2 scale) {
        this(x,y,texture.getRegionWidth()/scale.x,texture.getRegionHeight()/scale.y);
        setDrawScale(scale);
        setTexture(texture);
    }

    public String toString() {
        String out = "Portal with {";

        out += "pos: " + getPosition();
        out += "}";

        return out;
    }

    public void setPortName(String name) { portName = name; }

    public String getPortName() { return portName; }

//    /**
//     * TODO
//     * @param texture
//     */
//    public void setTexture(FilmStrip texture) {
//
//    }

    /**
     * TODO
     * @return
     */
//    public FilmStrip getTexture() {
//        return texture;
//    }

    public ObstacleType getType() { return ObstacleType.PORTAL;}
}
