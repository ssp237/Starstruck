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
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class TutorialPoint {

    /** To be removed */
    protected boolean remove = false;
    /** point for pink astronaut */
    private Star pinkPoint;
    /** Point for blue astronaut */
    private Star bluePoint;
    /** Instruction associated with these tasks */
    private FilmStrip task;
    /** Name of this tutorial */
    private String name;

    /**
     * Create new tutorial points with the given texture and draw scale.
     * @param x1, x2 X coord of the new star
     * @param y1, x2 Y coord of the new star
     * @param texture Texture for the new star
     * @param scale Draw scale for the new star
     */
    public TutorialPoint(float x1, float y1, float x2, float y2, TextureRegion texture, Vector2 scale) {
        //pinkPoint = new Star(x1, y1, texture.getRegionWidth(), texture.getRegionHeight());
        pinkPoint = new Star(x1, y1, texture, scale);
        pinkPoint.setColor("pink");
//        pinkPoint.setTexture(texture);
//        pinkPoint.setDrawScale(scale);
        pinkPoint.setType(ObstacleType.TUTORIAL);
        //bluePoint = new Star(x2, y2, texture.getRegionWidth(), texture.getRegionHeight());
        bluePoint = new Star(x2, y2, texture, scale);
        bluePoint.setColor("blue");
        //bluePoint.setTexture(texture);
        //bluePoint.setDrawScale(scale);
        bluePoint.setType(ObstacleType.TUTORIAL);
    }

    public TutorialPoint(float x1, float y1, float x2, float y2, TextureRegion texture, FilmStrip taskText, Vector2 scale) {
        this(x1, y1, x2, y2, texture, scale);
        task = taskText;
    }

    public TutorialPoint(float x1, float y1, float x2, float y2, TextureRegion texture, FilmStrip taskText, Vector2 scale, String name) {
        this(x1, y1, x2, y2, texture, taskText, scale);
        this.name = name;
        pinkPoint.setTutName(name);
        bluePoint.setTutName(name);
    }

    public Star getPinkPoint() { return pinkPoint; }

    public Star getBluePoint() { return bluePoint; }

    public String getName() { return name; }

    /**
     * Return a new star with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static TutorialPoint fromJSON(JsonValue json, Vector2 scale) {
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        key = json.get("taskText").asString();
        FilmStrip taskText = JsonAssetManager.getInstance().getEntry(key, FilmStrip.class);
        String name = json.get("name").asString();
        TutorialPoint out = new TutorialPoint(json.get("x1").asFloat(), json.get("y1").asFloat(), json.get("x2").asFloat(), json.get("y2").asFloat(), texture, taskText, scale, name);
        return out;
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write position and size
        Vector2 p1 = pinkPoint.getPosition();
        Vector2 p2 = bluePoint.getPosition();

        json.addChild("x1", new JsonValue(p1.x));
        json.addChild("y1", new JsonValue(p1.y));
        json.addChild("x2", new JsonValue(p2.x));
        json.addChild("y2", new JsonValue(p2.y));

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(pinkPoint.getTexture())));
        json.addChild("taskText", new JsonValue(JsonAssetManager.getInstance().getKey(task)));

        //Add name
        json.addChild("name", new JsonValue(name));

        //System.out.println(json);

        return json;
    }

    public String toString() {
        String out = "Tutorial points with {";

        out += "pink: " + pinkPoint.getPosition();
        out += "}, {";
        out += "blue: " + bluePoint.getPosition();

        return out;
    }

//    public void setTexture(TextureRegion texture) {
//        setTexture(texture);
//    }
//
//    public TextureRegion getTexture() {
//        return getTexture();
//    }

//    public boolean getRemove() { return remove; }
//
//    public void setRemove(boolean value) { remove = value; }
}
