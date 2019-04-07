package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Obstacles.Obstacle;
import edu.cornell.gdiac.starstruck.Obstacles.Planet;

public class EditController extends WorldController implements ContactListener {

    /** Current obstacle */
    private Obstacle current;
    /** VectorWorld */
    private VectorWorld vectorWorld;

    public EditController() {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
//        jsonReader = new JsonReader();
//        level = new LevelModel();
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
//        sensorFixtures = new ObjectSet<Fixture>();
        current = null;
        vectorWorld = new VectorWorld();
    }

    public void reset() {
        world = new World(new Vector2(0,0), false);
        world.setContactListener(this);

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

    public void update(float dt) {
        InputController input = InputController.getInstance();
        if (input.didP()) {
            current = new Planet(input.xPos()/scale.x, -(input.yPos()/scale.y) + bounds.height, 1, world, scale);
        }

        if (current != null) {
            current.setPosition(input.xPos()/scale.x, -(input.yPos()/scale.y) + bounds.height);
            switch (current.getType()) {
                case PLANET: updatePlanet();
            }
        } else {

        }

    }

    public void draw(float dt) {
        canvas.clear();
        canvas.begin();

        if (current != null) {
            current.draw(canvas);
        }

        canvas.end();
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
