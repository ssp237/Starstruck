package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.starstruck.Obstacles.Planet;
import edu.cornell.gdiac.util.FilmStrip;

public class Bug extends Enemy {


    /** Overrides texture with a filmstrip */
    private FilmStrip texture;
    /** Current animation frame */
    private int animeframe;
    /** Counter for animation delay */
    private int delay;

    /** position */
    private float x;
    private float y;

    /**current planet of enemy */
    private Planet curPlanetEN;

    /** Speed of bug */
    private static final float BUG_SPEED = 0.01f;

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
    public Bug(float x, float y, FilmStrip texture, Vector2 scale) {
        super(x,y,texture.getRegionWidth()/scale.x,texture.getRegionHeight()/scale.y);
        this.x = x;
        this.y = y;
        setTexture(texture);
        setDrawScale(scale);
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



        if (getOnPlanet()) {
            setFixedRotation(true);
            //enemy.setRotation(1);
            Vector2 contactPointEN = new Vector2(x, y);
            Vector2 contactDirEn = contactPointEN.cpy().sub(curPlanetEN.getPosition());
            float angle = -contactDirEn.angleRad(new Vector2(0, 1));
            setAngle(angle);
            //setPosition(contactPointEN);
            contactDirEn.rotateRad(-(float) Math.PI / 2);
            setPosition(contactPointEN.add(contactDirEn.setLength(BUG_SPEED)));
            //setGravity(vectorWorld.getForce(getPosition()));
            applyForce();
        }






        super.update(dt);
    }


    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? -1.0f : 1.0f;
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
    }

    public ObstacleType getType() { return ObstacleType.BUG;}

    public void setPlanet (Planet p) { curPlanetEN = p;}

}
