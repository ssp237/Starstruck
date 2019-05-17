package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Obstacles.Anchor;
import edu.cornell.gdiac.starstruck.Obstacles.Obstacle;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.starstruck.Obstacles.Planet;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class Bug extends Enemy {


    /** Overrides texture with a filmstrip */
    public FilmStrip texture;

    /** position */
    private float x;
    private float y;

    /**current planet of enemy */
    private Planet curPlanetEN;

    /** Speed of bug */
    protected float BUG_SPEED = 0.01f;//0.00001f;

    private Vector2 contactPointEN = new Vector2(x, y);

    private static int counter = 0;

    private boolean onPlanet = true;

    public VectorWorld vectorWorld;


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
    public Bug(float x, float y, FilmStrip texture, Vector2 scale, VectorWorld world) {
        super(x,y,texture.getRegionWidth()/scale.x,texture.getRegionHeight()/scale.y);
        this.x = x;
        this.y = y;
        setTexture(texture);
        setDrawScale(scale);
        this.setBodyType(BodyDef.BodyType.DynamicBody);
        counter++;
        setName("bug " + counter);
//        for (int i = 0; i < 100; i++){
//            update(0.015f);
//        }
        vectorWorld = world;
    }

    public Planet getCurPlanet() {
        return curPlanetEN;
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

            //.sub(0, (texture.getRegionHeight()/ drawScale.y)/2)
            Vector2 planetRadius = contactPointEN.cpy();


            setFixedRotation(true);
            //enemy.setRotation(1);
            contactPointEN.set(getPosition().cpy());
            Vector2 contactDirEn = contactPointEN.cpy().sub(curPlanetEN.getPosition());

            if (contactDirEn.len() - ((this.getTexture().getRegionHeight()/2)/ drawScale.y) > curPlanetEN.getRadius() ) {
                Vector2 gravity = vectorWorld.getForce(this.getPosition()).scl(-1);
                gravity.setLength(100000);
                this.getBody().applyForceToCenter(gravity, true);
            }


            float angle = -contactDirEn.angleRad(new Vector2(0, 1));
            setAngle(angle);
            //this.setPosition(contactPointEN);
            //contactDirEn.rotateRad(-(float) Math.PI / 2);
            contactDirEn.rotate90(-1);
            //this.setLinearVelocity(contactDirEn.setLength(BUG_SPEED));
            //contactDirEn.rotateRad(-(float) Math.PI / 2);
            this.setPosition(contactPointEN.add(contactDirEn.setLength(BUG_SPEED)));
            //body.applyForce(contactDirEn.setLength(BUG_SPEED), getPosition(), true);
//            body.setLinearVelocity(new Vector2 (0, 0 ));
            //body.applyLinearImpulse(contactDirEn.cpy().rotateRad(-(float) Math.PI / 2).setLength(10f), getPosition(), true);

        //setGravity(vectorWorld.getForce(getPosition()));
            //applyForce();
//            this.x = this.getPosition().x;
//            this.y = this.getPosition().y;

        super.update(dt);
    }


    public void draw(GameCanvas canvas) {
        float effect = isFacingRight() ? -1.0f : 1.0f;
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect,1.0f);
    }

    public ObstacleType getType() { return ObstacleType.BUG;}

    public void setPlanet (Planet p) { curPlanetEN = p;}

    public JsonValue toJson () {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));

        return json;
    }

    public FilmStrip getTexture() {
        return texture;
    }

    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {
        System.out.println("presolve");

        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            String bd1N = bd1.getName();
            String bd2N = bd2.getName();

            //Disable all collisions for worms
            if ((bd1.getType() == ObstacleType.BUG || bd2.getType() == ObstacleType.BUG)
                    && (bd1.getType() == ObstacleType.PLANET || bd2.getType() == ObstacleType.PLANET)) {
                onPlanet = true;
            } else {
                onPlanet = false;
            }



        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

//        Object fd1 = fix1.getUserData();
//        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

//            String bd1N = bd1.getName();
//            String bd2N = bd2.getName();

            if ((bd1.getType() == ObstacleType.BUG || bd2.getType() == ObstacleType.BUG)
                    && (bd1.getType() == ObstacleType.PLANET || bd2.getType() == ObstacleType.PLANET)) {
                contactPointEN = contact.getWorldManifold().getPoints()[0];

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String toString(){
        String out = "Bug with {texture: ";
        //System.out.println(texture);
        out += JsonAssetManager.getInstance().getKey(texture);
        out += ", planet " + curPlanetEN;

        out += "}";

        //"Worm with { velocity " + getVX() + " and position " + getPosition() +"}";
        return out;
    }

}
