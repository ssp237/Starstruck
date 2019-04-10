package edu.cornell.gdiac.starstruck.Models;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.starstruck.Obstacles.Star;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class Worm extends Enemy{


    /** Overrides texture with a filmstrip */
    private FilmStrip texture;
    /** Current animation frame */
    private int animeframe;
    /** Counter for animation delay */
    private int delay;
    /** y position of the enemy */
    private float y_pos;
    /** right bound of the screen */
    private float right_bound;
    /** Counter for names */
    private static int worm_count = 1;
    /** Delay for wormie to come back on screen */
    private int delay_pos = 1000;


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
    public Worm(float x, float y, float width, float height, float velocityX, float right_b) {
        super(x,y,width,height);
        this.setVX(velocityX);
        right_bound = right_b;
        setName("worm" + worm_count);
        worm_count++;
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
    public Worm(float x, float y, FilmStrip texture, Vector2 scale, float velocityX, float right_b) {
        this(x,y,texture.getRegionWidth()/scale.x,texture.getRegionHeight()/scale.y, velocityX, right_b);
        setTexture(texture);
        setDrawScale(scale);
        y_pos = y;
        //right_bound = right_b;

    }

    /**
     * Return a new star with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static Worm fromJSON(JsonValue json, Vector2 scale, float right_b) {
        String key = json.get("texture").asString();
        FilmStrip texture = JsonAssetManager.getInstance().getEntry(key, FilmStrip.class);
        Worm out =  new Worm(json.get("x").asFloat(), json.get("y").asFloat(), texture, scale, json.get("velocity").asFloat(), right_b);
        return out;
    }

    /**
     * Sets the texture to the given filmstrip with size size and delay animDelay between frames.
     * @param texture The filmstrip to set
     */
    public void setTexture(FilmStrip texture) {
        this.texture = texture;
        origin = new Vector2(texture.getRegionWidth()/2.0f, texture.getRegionHeight()/2.0f);
    }

    public void update(float dt) {
        texture.tick(); //Animation
        super.update(dt);
        System.out.println(getVX());

        if (this.getPosition().x < 0) {
            delay_pos--;
            if (delay_pos == 0) {
                delay_pos = 1000;
                this.setPosition(right_bound, y_pos);
            }
        }

    }


    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? -1.0f : 1.0f;
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
    }

    public ObstacleType getType() { return ObstacleType.WORM;}



}


