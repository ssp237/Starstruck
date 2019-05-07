package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.util.FilmStrip;

public class ColoredBug extends Bug {

    /** Color of this bug */
    private ModelColor color;

    /** Texture to use if sleeping */
    private FilmStrip sleepTexture;
    private Vector2 sleepOrigin;

    /**Are we sleeping?*/
    private boolean sleeping;

    public int range = 4;

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
    public ColoredBug(float x, float y, FilmStrip texture, FilmStrip sleepTexture, Vector2 scale, ModelColor color) {
        super(x,y,texture, scale);
        this.color = color;
        setSleepingTexture(sleepTexture);
        sleeping = true;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public void setSleeping(boolean value) {
        if (value) {
            sleepTexture.setFrame(texture.getFrame());
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

    public ModelColor getColor() {
        return color;
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
