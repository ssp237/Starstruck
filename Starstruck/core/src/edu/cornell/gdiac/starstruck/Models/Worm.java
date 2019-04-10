package edu.cornell.gdiac.starstruck.Models;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.util.FilmStrip;

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
    public Worm(float x, float y, float width, float height, FilmStrip texture, float velocityX) {
        super(x,y,width,height);
        this.setVX(velocityX);
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

        if (this.getPosition().x < 0) {
            this.setPosition(right_bound, y_pos);
        }

    }


    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? -1.0f : 1.0f;
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
    }

    public ObstacleType getType() { return ObstacleType.WORM;}
}


