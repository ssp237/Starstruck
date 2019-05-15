package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.Anchor;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.starstruck.Obstacles.WheelObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.util.ArrayList;

public class AztecWheel extends WheelObstacle {

    private static int counter = 0;

    /** Angular velocity */
    private final float v = 0.5f;
    /** Initial angle */
    private float omegaNot;
    /** Radius for anchor positions */
    private float radius2;
    /** List of our anchors */
    private ArrayList<Anchor> anchors;

    public AztecWheel(float x, float y, Vector2 scale) {
        super(x,y,0);

        texture = JsonAssetManager.getInstance().getEntry("so boss", TextureRegion.class);
        setTexture(texture);
        setDrawScale(scale);


        float radius = texture.getRegionWidth() / (scale.x * 2);
        setRadius(radius);

        setDrawScale(scale);


        //Set drawing scale
        float newScale = (2 * radius * drawScale.x)/texture.getRegionWidth();
        this.scaleDraw = newScale;


        setName("aztecWheel" + counter);
        counter++;
        radius2 = radius * 1.1f;

        anchors = new ArrayList<Anchor>();

        for (Vector2 v : getAnchorPositions()) {
            anchors.add(new Anchor(v.x, v.y, JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale));
        }

        omegaNot = getAngle();
    }

    private ArrayList<Vector2> getAnchorPositions() {

        ArrayList<Vector2> out = new ArrayList<Vector2>();

        Vector2 pos = getPosition();
        float twoRootTwo = (float) (Math.sqrt(2)/2);

        out.add(new Vector2(pos.x, pos.y + radius2));
        out.add(new Vector2(pos.x + twoRootTwo * radius2, pos.y + twoRootTwo * radius2));
        out.add(new Vector2(pos.x + radius2, pos.y));
        out.add(new Vector2(pos.x + twoRootTwo * radius2, pos.y - twoRootTwo * radius2));
        out.add(new Vector2(pos.x, pos.y - radius2));
        out.add(new Vector2(pos.x - twoRootTwo * radius2, pos.y - twoRootTwo * radius2));
        out.add(new Vector2(pos.x - radius2, pos.y));
        out.add(new Vector2(pos.x - twoRootTwo * radius2, pos.y + twoRootTwo * radius2));


        return out;
    }

    public void setPosition(Vector2 position) {
        super.setPosition(position);

        setAngle(omegaNot);

        ArrayList<Vector2> anchorPositions = getAnchorPositions();
        for (int i = 0; i < 8; i ++) {
            anchors.get(i).setPosition(anchorPositions.get(i));
        }
    }

    public void setPosition(float x, float y) {
        setPosition(new Vector2(x, y));
    }

    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) return false;
        for (Anchor a : anchors) {
            if (!a.activatePhysics(world)) return false ;
        }
        return true;
    }

    public void deactivatePhysics(World world) {
        super.deactivatePhysics(world);
        for (Anchor a : anchors) {
            a.deactivatePhysics(world);
        }
    }

    public void update(float dt) {
        super.update(dt);
        float newAngle = (getAngle() + v*dt) % (float) Math.toRadians(360);
        setAngle(newAngle);
        newAngle -= omegaNot;
        newAngle += (float) Math.toRadians(360);
        float vx, vy;
        Vector2 pos = getPosition();
        for (Anchor a : anchors) {
            vx = -radius2 * (float) Math.cos(newAngle) * v;
            vy = -radius2 * (float) Math.sin(newAngle) * v;
            a.setVX(vx);
            a.setVY(vy);
            newAngle -= (float) Math.toRadians(45);
//            Vector2 pos = getPosition().cpy().sub(a.getPosition()).scl(v*10000);
//            a.setVX(pos.y);
//            a.setVY(-pos.x);
        }
    }


    /**
     * Return a new AztecWheel with parameters specified by the JSON
     * @param json A JSON containing data for one AztecWheel
     * @param scale The scale to convert physics units to drawing units
     * @return A AztecWheel created according to the specifications in the JSON
     */
    public static AztecWheel fromJSON(JsonValue json, Vector2 scale) {
        AztecWheel out =  new AztecWheel(json.get("x").asFloat(), json.get("y").asFloat(), scale);
        return out;
    }

    /**
     * Write this AztecWheel to a JsonValue. When parsed, this JsonValue should return the same AztecWheel.
     * @return A JsonValue representing this AztecWheel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        //Write position
        Vector2 pos = getPosition();

        json.addChild("x", new JsonValue(pos.x));
        json.addChild("y", new JsonValue(pos.y));

        //System.out.println(json);

        return json;
    }

    public String toString(){
        String out = "AztecWheel at (";
        out += getX() + "," + getY() + ")";



        //"Worm with { velocity " + getVX() + " and position " + getPosition() +"}";
        return out;
    }


    public ObstacleType getType() {return ObstacleType.AZTEC_WHEEL;}

    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        for (Anchor a : anchors) {
            a.draw(canvas);
        }
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        for (Anchor a : anchors) {
            a.drawDebug(canvas);
        }
    }




}
