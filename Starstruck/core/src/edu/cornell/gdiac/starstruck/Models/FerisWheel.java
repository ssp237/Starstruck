package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.Color;
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

public class FerisWheel extends WheelObstacle {

    private static int counter = 0;

    /** Angular velocity */
    private float v = 0.5f;
    /** Initial angle */
    private float omegaNot;
    /** Radius for anchor positions */
    private float radius2;
    /** List of our anchors */
    private ArrayList<Anchor> anchors;
    /**Basket texture*/
    private TextureRegion basketTexture;

    public FerisWheel(float x, float y, Vector2 scale) {
        super(x,y,0);

        texture = JsonAssetManager.getInstance().getEntry("ci boss", TextureRegion.class);
        setTexture(texture);
        setDrawScale(scale);

        basketTexture = JsonAssetManager.getInstance().getEntry("ci boss-ket", TextureRegion.class);


        float radius = texture.getRegionWidth() / (scale.x * 2);
        setRadius(radius);

        setDrawScale(scale);


        //Set drawing scale
        float newScale = (2 * radius * drawScale.x)/texture.getRegionWidth();
        this.scaleDraw = newScale;


        setName("ferisWheel" + counter);
        counter++;
        radius2 = radius * 1.1f;

        anchors = new ArrayList<Anchor>();

        for (Vector2 v : getAnchorPositions()) {
            anchors.add(new Anchor(v.x, v.y, JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale));
        }

        omegaNot = getAngle() + (float) Math.toRadians(10);
    }

    public FerisWheel(float x, float y, float v, Vector2 scale) {
        this(x,y,scale);
        this.v = v;
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

        setAngle(omegaNot - + (float) Math.toRadians(10));

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
            a.update(dt);
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
    public static FerisWheel fromJSON(JsonValue json, Vector2 scale) {
        FerisWheel out =  new FerisWheel(json.get("x").asFloat(), json.get("y").asFloat(),json.get("v").asFloat(), scale);
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
        json.addChild("v", new JsonValue(v));

        //System.out.println(json);

        return json;
    }

    public String toString(){
        String out = "FerisWheel at (";
        out += getX() + "," + getY() + ")";



        //"Worm with { velocity " + getVX() + " and position " + getPosition() +"}";
        return out;
    }


    public ObstacleType getType() {return ObstacleType.FERIS_WHEEL;}

    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        float x = (float) basketTexture.getRegionWidth()/2;
        float y = basketTexture.getRegionHeight();
        for (Anchor a : anchors) {
            a.draw(canvas);
            Vector2 pos = a.getPosition();
            canvas.draw(basketTexture, Color.WHITE, x,y,pos.x * drawScale.x,
                    pos.y * drawScale.x, 0, scaleDraw, scaleDraw);
        }
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        for (Anchor a : anchors) {
            a.drawDebug(canvas);
        }
    }




}
