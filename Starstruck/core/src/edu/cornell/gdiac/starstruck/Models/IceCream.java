package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class IceCream extends Enemy {
    /** Overrides texture with a filmstrip */
    private FilmStrip texture;
    /** Current animation frame */
    private int animeframe;
    /** Counter for animation delay */
    private int delay;
    /** y position of the enemy */
    private float y_pos;
    /** right bound of the screen */
    private float right_bound = 57.60002f;
    /** Counter for names */
    private static int icecream_count = 1;
    /** Delay for wormie to come back on screen */
    private static int MAX_DELAY = 10;
    private int delay_pos = MAX_DELAY;

    private float x_original;

    /**Top of the screen */
    private float upBound = 0;


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
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public IceCream(float x, float y, float width, float height, float velocityY) {
        super(x,y,width,height);
        this.setVY(velocityY);
        //right_bound = right_b;
        setName("IceCream" + icecream_count);
        icecream_count++;
        x_original = x;
    }

    /**
     * Set the upper bound of the screen for this ice cream
     * @param bound The upper bound of the screen.
     */
    public void setUpBound(float bound){
        upBound = bound;
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

     */
    public IceCream(float x, float y, FilmStrip texture, Vector2 scale, float velocityX) {
        this(x,y,texture.getRegionWidth()/scale.x,texture.getRegionHeight()/scale.y, velocityX);
        setTexture(texture);
        setDrawScale(scale);
        y_pos = y;
        x_original = x;
        //right_bound = right_b;

    }

    /**
     * Return a new star with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static IceCream fromJSON(JsonValue json, Vector2 scale) {
        String key = json.get("texture").asString();
        FilmStrip texture = JsonAssetManager.getInstance().getEntry(key, FilmStrip.class);
        IceCream out =  new IceCream(json.get("x").asFloat(), json.get("y").asFloat(), texture, scale, json.get("velocity").asFloat());
        return out;
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));

        //Write position
        Vector2 pos = getPosition();

        json.addChild("x", new JsonValue(pos.x));
        json.addChild("y", new JsonValue(pos.y));

        //Write velocity
        json.addChild("velocity", new JsonValue(getVY() == 0 ? 1 : getVY()));

        //System.out.println(json);

        return json;
    }

    /**
     * Sets the texture to the given filmstrip with size size and delay animDelay between frames.
     * @param texture The filmstrip to set
     */
    public void setTexture(FilmStrip texture) {
        int i = texture.getSize();
        //System.out.println((int) Math.random() * i);
        texture.setFrame((int) (Math.random() * i));
        this.texture = texture;
        origin = new Vector2(texture.getRegionWidth()/2.0f, texture.getRegionHeight()/2.0f);
    }

    public void update(float dt) {
        texture.tick(); //Animation
        super.update(dt);
        //System.out.println(getVX());

        //System.out.println(upBound);
        if (getPosition().y > upBound + getHeight()/2) {
            delay_pos--;
            //System.out.println(delay_pos);
            if (delay_pos == 0) {
                delay_pos = MAX_DELAY;
                setPosition(x_original, -getHeight()/2);
            }
        }
//        if (this.getPosition().x < x_original - 5) {
//            //System.out.println(getVX());
//            setVX(-getVX());
//        } else if (this.getPosition().x > x_original + 5) {
//            //System.out.println(getVX());
//            setVX(-getVX());
//        }
        //System.out.println(this);
        //System.out.println(this);

    }

    public FilmStrip getTexture() {
        return texture;
    }

    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? -1.0f : 1.0f;
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
    }

    public ObstacleType getType() { return ObstacleType.ICE_CREAM;}


    /** Sets the place where the worm re-enters to the rightmost bound of the level*/
    public void setRight_bound(float r_bound) {right_bound = r_bound;}

    public String toString(){
        String out = "{texture: ";
        //System.out.println(texture);
        out += JsonAssetManager.getInstance().getKey(texture) + ", ";
        out += "rightbound: " + right_bound +"}";


        //"Worm with { velocity " + getVX() + " and position " + getPosition() +"}";
        return out;
    }


}
