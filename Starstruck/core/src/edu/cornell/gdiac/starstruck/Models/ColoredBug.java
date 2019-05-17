package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.starstruck.Obstacles.Planet;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class ColoredBug extends Bug {

    /** Color of this bug */
    private ModelColor color;

    /** Texture to use if sleeping */
    private FilmStrip sleepTexture;
    private Vector2 sleepOrigin;

    /**Are we sleeping?*/
    private boolean sleeping;

    public int range = 1;

    private static int bugCount;

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * drawing to work properly, you MUST set the drawScale. The drawScaled
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     */
    public ColoredBug(float x, float y, FilmStrip texture, FilmStrip sleepTexture, Vector2 scale, ModelColor color, VectorWorld world) {
        super(x,y,texture, scale, world);
        this.color = color;
        setSleepingTexture(sleepTexture);
        sleeping = true;
        //System.out.println(this);
        this.BUG_SPEED = 0.05f;
        String name = "cbug" + (color == ModelColor.PINK ? "pink" : "blue") + bugCount;
        bugCount++;
        setName(name);
    }

//    public void setPlanet (Planet p) {
//        super.setPlanet(p);
//        BUG_SPEED /= (5 - p.getInd());
//    }

    /**
     * Set the speed of the bug (only for edit controller to make output useable)
     * @param speed The new speed of this bug
     */
    public void setSpeed(float speed) {
        BUG_SPEED = speed;
    }

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * drawing to work properly, you MUST set the drawScale. The drawScaled
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param scale     Drawscale for this bug
     * @param color  	Color of this bug (PINK or BLUE)
     */
    public ColoredBug(float x, float y, Vector2 scale, ModelColor color, VectorWorld world) {
        this(x,y, JsonAssetManager.getInstance().getEntry(color.getName() + " bug awake", FilmStrip.class),
                JsonAssetManager.getInstance().getEntry(color.getName() + " bug asleep", FilmStrip.class), scale, color, world);
    }

    public void setSpeedSign( int i){
        BUG_SPEED = Math.abs(BUG_SPEED);
        if (i < 0) {
           BUG_SPEED = -BUG_SPEED;
        }
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public void setSleeping(boolean value) {
        if (value) {
            sleepTexture.setFrame(texture.getFrame());
            setBodyType(BodyDef.BodyType.StaticBody);
        } else {
            setBodyType(BodyDef.BodyType.DynamicBody);
        }
        sleeping = value;
    }


    /**
     * Sets the sleeping texture to the given filmstrip with size size and delay animDelay between frames.
     * @param texture The filmstrip to set
     */
    public void setSleepingTexture(FilmStrip texture) {
        this.sleepTexture = texture;
        sleepOrigin = new Vector2(texture.getRegionWidth()/2.0f, texture.getRegionHeight()/2.0f);
    }

    public void update(float dt) {
        if (sleeping) return;
        super.update(dt);
    }

    /**
     * Get the color of this bug
     * @return The color of this bug
     */
    public ModelColor getColor() {
        return color;
    }

    public ObstacleType getType() {
        return ObstacleType.COLORED_BUG;
    }

    public JsonValue toJson () {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));
        json.addChild("color", new JsonValue(color.getName()));

        return json;
    }

    public String toString(){
        String out = "Colored Bug with {texture: ";
        //System.out.println(texture);
        out += JsonAssetManager.getInstance().getKey(texture) + ", ";
        out += "sleep texture " + JsonAssetManager.getInstance().getKey(sleepTexture) + ", ";
        out += "color: " + (color == ModelColor.PINK ? "pink," : "blue,");
        out += "planet " + getCurPlanet();
        out += "}";

        //"Worm with { velocity " + getVX() + " and position " + getPosition() +"}";
        return out;
    }

    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? -1.0f : 1.0f;
        if (sleeping){
            canvas.draw(sleepTexture, Color.WHITE,sleepOrigin.x,sleepOrigin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
        } else {
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), effect, 1.0f);
        }
    }
}
