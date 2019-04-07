package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Obstacles.Anchor;
import edu.cornell.gdiac.starstruck.Obstacles.Obstacle;
import edu.cornell.gdiac.starstruck.Obstacles.Planet;
import edu.cornell.gdiac.starstruck.Obstacles.Star;
import edu.cornell.gdiac.util.JsonAssetManager;

public class EditController extends WorldController implements ContactListener {

    /** Current obstacle */
    private Obstacle current;
    /** VectorWorld */
    private VectorWorld vectorWorld;
    /** Reference to the game level */
    protected LevelModel level;

    public EditController() {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
        level = new LevelModel();
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
//        sensorFixtures = new ObjectSet<Fixture>();
        current = null;
        vectorWorld = new VectorWorld();
    }

    public void reset() {
        level.dispose();

        level.setBackround(JsonAssetManager.getInstance().getEntry("background", Texture.class));
        level.getWorld().setContactListener(this);
        world = level.getWorld();

        setComplete(false);
        setFailure(false);

    }

    /**
     * Helper to update current obstacle if it is a planet.
     */
    private void updatePlanet() {
        InputController input = InputController.getInstance();
        if (input.didPrimary()){
            Planet p = (Planet) current;
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x, pos.y, p.getInd() + 1, world, scale);
        } else if (input.didDown()) {
            Planet p = (Planet) current;
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x, pos.y, p.getInd() - 1, world, scale);
        }
    }

    /**
     * Helper function to process clicking
     */
    private void updateClick() {
        InputController input = InputController.getInstance();
        if (current != null) {
            current = null;
        }
        else {
            for (Obstacle obj : level.getAllObjects()) {
                if (obj.containsPoint(input.getCrossHair())) {
                    current = obj;
                }
            }
        }
    }

    public void update(float dt) {
        System.out.println(current);
        //System.out.println(level.getAllObjects());
        InputController input = InputController.getInstance();
        if (input.didP()) {
            Vector2 pos = input.getCrossHair();
            current = new Planet(pos.x, pos.y, 1, world, scale);
            level.add(current);
        } else if (input.didA()) {
            Vector2 pos = input.getCrossHair();
            current = new Anchor(pos.x, pos.y, JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale);
            level.add(current);
        } else if (input.didS()) {
            Vector2 pos = input.getCrossHair();
            current = new Star(pos.x, pos.y, JsonAssetManager.getInstance().getEntry("star", TextureRegion.class), scale);
            level.add(current);
        }
        if (current != null) {
            if (input.didBackspace()) {
                level.remove(current);
            } else {
                current.setPosition(input.xPos() / scale.x, -(input.yPos() / scale.y) + bounds.height);
                switch (current.getType()) {
                    case PLANET:
                        updatePlanet();
                }
            }
        } else {

        }

        if (input.didTertiary()) {
            updateClick();
        }

    }

    public void draw(float dt) {
        canvas.clear();

        level.draw(canvas);

//        canvas.begin();
//        if (current != null) {
//            current.draw(canvas);
//        }
//
//        canvas.end();
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {}

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {}

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}
}
