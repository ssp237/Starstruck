package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

public class Bug extends Enemy {


    /** Overrides texture with a filmstrip */
    private FilmStrip texture;
    /** Current animation frame */
    private int animeframe;
    /** Counter for animation delay */
    private int delay;

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
    public Bug(float x, float y, float width, float height) {
        super(x,y,width,height);
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
        delay--;
        if(delay <= 0) {
            delay = texture.getDelay();

            animeframe++;
            if (animeframe >= texture.getSize()) {
                animeframe -= texture.getSize();
            }
        }

        super.update(dt);
    }


    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? -1.0f : 1.0f;
        texture.setFrame(animeframe);
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
    }

}
