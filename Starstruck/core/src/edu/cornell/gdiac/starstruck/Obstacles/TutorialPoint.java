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
import edu.cornell.gdiac.starstruck.ControllerType;
import edu.cornell.gdiac.starstruck.InputController;
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
    private TextureRegion task;
    /** Name of this tutorial */
    private String name;
    /** Whether pinkPoint was completed */
    private boolean pinkHit;
    /** Whether bluePoint was completed */
    private boolean blueHit;
    /** Whether this task is complete */
    private boolean complete;
    /** The general name of the task */
    private String taskName;
    /** twoplayer */
    private static boolean twoplayer;

    /**
     * Create new tutorial points with the given texture and draw scale.
     * @param x1, x2 X coord of the new star
     * @param y1, x2 Y coord of the new star
     * @param scale Draw scale for the new star
     */
    public TutorialPoint(float x1, float y1, float x2, float y2, Vector2 scale) {
        //pinkPoint = new Star(x1, y1, texture.getRegionWidth(), texture.getRegionHeight());
        pinkPoint = new Star(x1, y1, JsonAssetManager.getInstance().getEntry("pink dot animated", FilmStrip.class), scale);
        pinkPoint.setColor("pink");
//        pinkPoint.setTexture(texture);
//        pinkPoint.setDrawScale(scale);
        pinkPoint.setType(ObstacleType.TUTORIAL);
        pinkHit = false;
        //bluePoint = new Star(x2, y2, texture.getRegionWidth(), texture.getRegionHeight());
        bluePoint = new Star(x2, y2, JsonAssetManager.getInstance().getEntry("blue dot animated", FilmStrip.class), scale);
        bluePoint.setColor("blue");
        //bluePoint.setTexture(texture);
        //bluePoint.setDrawScale(scale);
        bluePoint.setType(ObstacleType.TUTORIAL);
        blueHit = false;
        complete = false;
    }

    public TutorialPoint(float x1, float y1, float x2, float y2, TextureRegion taskText, Vector2 scale) {
        this(x1, y1, x2, y2, scale);
        task = taskText;
    }

    public TutorialPoint(float x1, float y1, float x2, float y2, TextureRegion taskText, Vector2 scale, String name) {
        this(x1, y1, x2, y2, taskText, scale);
        this.name = name;
        pinkPoint.setTutName(name);
        bluePoint.setTutName(name);
    }

    public TutorialPoint(float x1, float y1, float x2, float y2, TextureRegion taskText, Vector2 scale, String name, String taskname) {
        this(x1, y1, x2, y2, taskText, scale, name);
        taskName = taskname;
    }

    public Star getPinkPoint() { return pinkPoint; }

    public Star getBluePoint() { return bluePoint; }

    public String getName() { return name; }

    public TextureRegion getTask() { return task; }

    public boolean pinkHit() { return pinkHit; }

    public void setPinkHit(boolean value) {
        pinkHit = value;
        pinkPoint.setHit(true);
    }

    public boolean blueHit() { return blueHit; }

    public void setBlueHit(boolean value) {
        blueHit = value;
        bluePoint.setHit(true);
    }

    public boolean complete() { return complete; }

    public void setComplete(boolean value) { complete = value; }

    public void setTwoplayer(boolean value) { twoplayer = value; }

    /**
     * Return a new star with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static TutorialPoint fromJSON(JsonValue json, Vector2 scale) {
        InputController input = InputController.getInstance();
        String taskname = json.get("taskText").asString();
        String key = taskname + " keyone";
        if ((input.getControlType() == ControllerType.CTRLONE || input.getControlType() == ControllerType.CTRLTWO) && !twoplayer) {
            key = taskname + " ctrlone";
        }
        if ((input.getControlType() == ControllerType.CTRLONE || input.getControlType() == ControllerType.CTRLTWO) && twoplayer) {
            key = taskname + " ctrlone";
            if (taskname.equals("switch"))
                key = "move ctrlone";
        }
        if (InputController.getInstance().getControlType() == ControllerType.KEY && twoplayer) {
            key = taskname + " keytwo";
        }
        if (taskname.equals("transparent")) {
            key = taskname;
        }
        TextureRegion taskText = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        String name = json.get("name").asString();
        TutorialPoint out = new TutorialPoint(json.get("x1").asFloat(), json.get("y1").asFloat(), json.get("x2").asFloat(), json.get("y2").asFloat(), taskText, scale, name, taskname);
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
        json.addChild("taskText", new JsonValue(taskName));

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
