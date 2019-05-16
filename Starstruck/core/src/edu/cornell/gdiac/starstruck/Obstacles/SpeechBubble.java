package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.ControllerType;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.InputController;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class SpeechBubble{
    /** To be removed */
    protected boolean remove = false;
    /** speech bubble for pink astronaut */
    private Star speechBubble;


    /** Name of this tutorial */
    private String name;

    private TextureRegion texture;

//    private float x;
//
//    private float y;
//
//    public float getXPos() {return x; }
//
//    private Vector2 scale;



    /**
     * Create new tutorial points with the given texture and draw scale.
     * @param x X coord of the new star
     * @param y Y coord of the new star
     * @param scale Draw scale for the new star
     */
    public SpeechBubble(float x, float y, Vector2 scale, TextureRegion texture) {
        //pinkPoint = new Star(x1, y1, texture.getRegionWidth(), texture.getRegionHeight());
        speechBubble = new Star(x, y, texture, scale);
//        this.x = x;
//        this.y = y;
//        this.scale = scale;
        speechBubble.setType(ObstacleType.SPEECH_BUBBLE);
        this.texture = texture;
    }

    public Star getBubble() {
        return speechBubble;
    }

//    public SpeechBubble(float x1, float y1, float x2, float y2, TextureRegion taskText, Vector2 scale) {
//        this(x1, y1, x2, y2, scale);
//        task = taskText;
//    }
//
//    public SpeechBubble(float x1, float y1, float x2, float y2, TextureRegion taskText, Vector2 scale, String name) {
//        this(x1, y1, x2, y2, taskText, scale);
//        this.name = name;
//        pinkPoint.setTutName(name);
//        bluePoint.setTutName(name);
//    }

//    public SpeechBubble(float x1, float y1, float x2, float y2, TextureRegion taskText, Vector2 scale, String name, String taskname) {
//        this(x1, y1, x2, y2, taskText, scale, name);
//        taskName = taskname;
//    }

//    public Star getPinkPoint() { return pinkPoint; }
//
//    public Star getBluePoint() { return bluePoint; }

    public String getName() { return name; }



//    public void draw(GameCanvas canvas) {
//        float effect = 1.0f;
//        Vector2 origin = new Vector2(texture.getRegionWidth()/2.0f, texture.getRegionHeight()/2.0f);
//        //System.out.println(drawScale);
//        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getXPos()*scale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
//    }

//    public TextureRegion getTask() { return task; }
//
//    public boolean pinkHit() { return pinkHit; }
//
//    public void setPinkHit(boolean value) {
//        pinkHit = value;
//        pinkPoint.setHit(true);
//    }
//
//    public boolean blueHit() { return blueHit; }
//
//    public void setBlueHit(boolean value) {
//        blueHit = value;
//        bluePoint.setHit(true);
//    }
//
//    public boolean complete() { return complete; }
//
//    public void setComplete(boolean value) { complete = value; }
//
//    public void setTwoplayer(boolean value) { twoplayer = value; }

//    /**
//     * Return a new star with parameters specified by the JSON
//     * @param json A JSON containing data for one star
//     * @param scale The scale to convert physics units to drawing units
//     * @return A star created according to the specifications in the JSON
//     */
//    public static TutorialPoint fromJSON(JsonValue json, Vector2 scale) {
//        String taskname = json.get("taskText").asString();
//        String key = taskname + " keyone";
//        if (InputController.getInstance().getControlType() == ControllerType.CTRLONE) {
//            key = taskname + " ctrlone";
//        }
//        if (InputController.getInstance().getControlType() == ControllerType.CTRLTWO) {
//            key = taskname = " ctrltwo";
//        }
//        if (InputController.getInstance().getControlType() == ControllerType.KEY && twoplayer) {
//            key = taskname + " keytwo";
//        }
//        if (taskname.equals("transparent")) {
//            key = taskname;
//        }
//        TextureRegion taskText = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
//        String name = json.get("name").asString();
//        TutorialPoint out = new TutorialPoint(json.get("x1").asFloat(), json.get("y1").asFloat(), json.get("x2").asFloat(), json.get("y2").asFloat(), taskText, scale, name, taskname);
//        return out;
//    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
//    public JsonValue toJson() {
//        JsonValue json = new JsonValue(JsonValue.ValueType.object);
//
//        //Write position and size
//        Vector2 p1 = pinkPoint.getPosition();
//        Vector2 p2 = bluePoint.getPosition();
//
//        json.addChild("x1", new JsonValue(p1.x));
//        json.addChild("y1", new JsonValue(p1.y));
//        json.addChild("x2", new JsonValue(p2.x));
//        json.addChild("y2", new JsonValue(p2.y));
//
//        //Add textures
//        json.addChild("taskText", new JsonValue(taskName));
//
//        //Add name
//        json.addChild("name", new JsonValue(name));
//
//        //System.out.println(json);
//
//        return json;
//    }
//
//    public String toString() {
//        String out = "Tutorial points with {";
//
//        out += "pink: " + pinkPoint.getPosition();
//        out += "}, {";
//        out += "blue: " + bluePoint.getPosition();
//
//        return out;
//    }
//
////    public void setTexture(TextureRegion texture) {
////        setTexture(texture);
////    }
////
////    public TextureRegion getTexture() {
////        return getTexture();
////    }
//
////    public boolean getRemove() { return remove; }
////
////    public void setRemove(boolean value) { remove = value; }

}
