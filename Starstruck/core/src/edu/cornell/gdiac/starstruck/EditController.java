package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.JsonAssetManager;

public class EditController extends WorldController implements ContactListener {

    /** Current obstacle */
    private Obstacle current;
    /** VectorWorld */
    private VectorWorld vectorWorld;
    /** Reference to the game level */
    protected LevelModel level;

    /** Initial position of player 1*/
    private static Vector2 P1_POS = new Vector2(2.5f, 5.0f);
    /** Initial position of player 2*/
    private static Vector2 P2_POS = new Vector2(3.5f, 6.5f);

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

        level.setBackground(JsonAssetManager.getInstance().getEntry("background", Texture.class));
        level.getWorld().setContactListener(this);
        world = level.getWorld();

        createPlayers();

        current = null;

        setComplete(false);
        setFailure(false);

    }

    private void createPlayers() {
        float dwidth;
        float dheight;
        TextureRegion texture;

        texture = JsonAssetManager.getInstance().getEntry("astronaut 1", TextureRegion.class);
        dwidth = texture.getRegionWidth()/scale.x;
        dheight = texture.getRegionHeight()/scale.y;
        AstronautModel player1 = new AstronautModel(P1_POS.x, P1_POS.y, dwidth, dheight, true, true);
        player1.setDrawScale(scale);
        player1.setTexture(texture);
        player1.setGlow(JsonAssetManager.getInstance().getEntry("glow", TextureRegion.class));
        player1.setBodyType(BodyDef.BodyType.StaticBody);
        player1.setName("avatar 1");

        texture = JsonAssetManager.getInstance().getEntry("astronaut 2", TextureRegion.class);
        dwidth = texture.getRegionWidth()/scale.x;
        dheight = texture.getRegionHeight()/scale.y;
        AstronautModel player2 = new AstronautModel(P2_POS.x, P2_POS.y, dwidth, dheight, false, false);
        player2.setDrawScale(scale);
        player2.setTexture(texture);
        player2.setGlow(JsonAssetManager.getInstance().getEntry("glow", TextureRegion.class));
        player2.setBodyType(BodyDef.BodyType.StaticBody);
        player2.setName("avatar 2");

        level.add(player1); level.add(player2);
    }

    /**
     * Helper to update current obstacle if it is a planet.
     */
    private void updatePlanet() {
        InputController input = InputController.getInstance();
        if (input.didPrimary()){
            Planet p = (Planet) current;
            level.remove(p);
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x, pos.y, p.getInd() + 1, world, scale);
            level.add(current);
        } else if (input.didDown()) {
            Planet p = (Planet) current;
            level.remove(p);
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x, pos.y, p.getInd() - 1, world, scale);
            level.add(current);
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

    /**
     * Helper function to update camera due to mouse dragging when no obstacle is selected.
     */
    private void updateCamera() {

    }

    public void update(float dt) {
        //System.out.println(current);
        int i = 0;
//        for (Obstacle obj : level.getAllObjects()) {
//            i += obj.getType() == ObstacleType.ANCHOR ? 1 : 0;
//        }
//        System.out.println(i);
        //System.out.println(level.getAllObjects());
        //System.out.println(level.getPlayer1().getVX() + "   " + level.getPlayer1().getVY());
        InputController input = InputController.getInstance();

        if (current != null) {
            if (input.didBackspace() && current.getType() != ObstacleType.PLAYER) {
                level.remove(current);
                current = null;
            } else {
                current.setPosition(input.xPos() / scale.x, -(input.yPos() / scale.y) + bounds.height);
                switch (current.getType()) {
                    case PLANET:
                        updatePlanet();
                }
            }
        } else {
            if (input.didP()) {
                Vector2 pos = input.getCrossHair();
                current = new Planet(pos.x, pos.y, 1, world, scale);
                level.add(current);
            } else if (input.didA()) {
                Vector2 pos = input.getCrossHair();
                current = new Anchor(pos.x, pos.y, JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale);
                level.add(current);
                current.setBodyType(BodyDef.BodyType.StaticBody);
            } else if (input.didS()) {
                Vector2 pos = input.getCrossHair();
                current = new Star(pos.x, pos.y, JsonAssetManager.getInstance().getEntry("star", TextureRegion.class), scale);
                level.add(current);
                current.setBodyType(BodyDef.BodyType.StaticBody);
            }
            if (input.mouseDragged()) {
                updateCamera();
            }
        }

        if (input.didTertiary()) {
            updateClick();
        }

    }

    /**
     * Override superclass's setDebug to also edit the level's current state
     * @param d The new value for debug
     */
    public void setDebug(boolean d) {
        super.setDebug(d);
        level.setDebug(d);
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
