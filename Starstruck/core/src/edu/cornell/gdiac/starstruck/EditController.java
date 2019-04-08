package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.JsonAssetManager;

public class EditController extends WorldController implements ContactListener {

    private class SaveListener implements Input.TextInputListener {

        public String file;

        public SaveListener(){
            file = null;
        }

        public void input (String text) {
            file = text;
        }


        public void canceled () {
            file = null;
        }
    }

    /** Speed of camera pan */
    private static final float PAN_CONST = 2;

    /** Current obstacle */
    private Obstacle current;
    /** VectorWorld */
    private VectorWorld vectorWorld;
    /** Reference to the game level */
    protected LevelModel level;
    /** Camera offset */
    private float camOffsetX;
    private float camOffsetY;
    /** Listener for save data */
    private SaveListener save;

    /** References to players and rope */
    private AstronautModel player1;
    private AstronautModel player2;
    private Rope rope;

    /** Initial position of player 1*/
    private static Vector2 P1_POS = new Vector2(2.5f, 5.0f);
    /** Initial position of player 2*/
    private static Vector2 P2_POS = new Vector2(3.5f, 6.5f);
    /** The width of the rope bridge */
    private static final float  BRIDGE_WIDTH = 6.0f;

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
        save = new SaveListener();
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
        save = new SaveListener();

    }

    private void createPlayers() {
        float dwidth;
        float dheight;
        TextureRegion texture;

        camOffsetX = 0;

        texture = JsonAssetManager.getInstance().getEntry("astronaut 1", TextureRegion.class);
        dwidth = texture.getRegionWidth()/scale.x;
        dheight = texture.getRegionHeight()/scale.y;
        player1 = new AstronautModel(P1_POS.x, P1_POS.y, dwidth, dheight, true, true);
        player1.setDrawScale(scale);
        player1.setTexture(texture);
        player1.setGlow(JsonAssetManager.getInstance().getEntry("glow", TextureRegion.class));
        player1.setName("avatar 1");

        texture = JsonAssetManager.getInstance().getEntry("astronaut 2", TextureRegion.class);
        dwidth = texture.getRegionWidth()/scale.x;
        dheight = texture.getRegionHeight()/scale.y;
        player2 = new AstronautModel(P2_POS.x, P2_POS.y, dwidth, dheight, false, false);
        player2.setDrawScale(scale);
        player2.setTexture(texture);
        player2.setGlow(JsonAssetManager.getInstance().getEntry("glow", TextureRegion.class));
        player2.setName("avatar 2");

        level.add(player1); level.add(player2);

        texture = JsonAssetManager.getInstance().getEntry("rope", TextureRegion.class);
        dwidth = texture.getRegionWidth()/scale.x;
        dheight = texture.getRegionHeight()/scale.y;
        rope = new Rope(player1.getX() + 0.5f, player1.getY() + 0.5f, BRIDGE_WIDTH, dwidth, dheight, player1, player2);
        rope.setTexture(texture);
        rope.setDrawScale(scale);
        rope.setName("rope");
        level.add(rope);

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
            current = new Planet(pos.x + camOffsetX/scale.x, pos.y, p.getInd() + 1, world, scale);
            level.add(current);
        } else if (input.didDown()) {
            Planet p = (Planet) current;
            level.remove(p);
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x + camOffsetX/scale.x, pos.y, p.getInd() - 1, world, scale);
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
     * Helper function to update camera panning with arrow keys when no planet is selected
     */
    private void updateCamera() {
        OrthographicCamera camera = (OrthographicCamera) canvas.getCamera();
        InputController input = InputController.getInstance();
        if (input.didLeft()) {
            camera.position.x = camera.position.x - PAN_CONST;
            camOffsetX = camOffsetX - PAN_CONST;
        }
        if (input.heldUp()) {
            camera.position.y = camera.position.y + PAN_CONST;
            camOffsetY = camOffsetY + PAN_CONST;
        }
        if (input.didRight()) {
            camera.position.x = camera.position.x + PAN_CONST;
            camOffsetX = camOffsetX + PAN_CONST;
        }
        if (input.heldDown()) {
            camera.position.y = camera.position.y - PAN_CONST;
            camOffsetY = camOffsetY - PAN_CONST;
        }
        camera.update();
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

        if (current == null)
            updateCamera();
        if (save.file != null) {
            //System.out.println(level.toJSON());
            save.file = null;
        }

        if (current != null) {
            if (input.didBackspace() && current.getType() != ObstacleType.PLAYER) {
                level.remove(current);
                current = null;
            } else {
                current.setPosition((input.xPos() + camOffsetX) / scale.x, -((input.yPos() + camOffsetY)/ scale.y) + bounds.height);
                switch (current.getType()) {
                    case PLANET:
                        updatePlanet();
                }
            }
        } else {
            if (input.shiftHeld() && input.didS()){
                Gdx.input.getTextInput(save, "Save as...", "level.json", "");
            } else if (input.didP()) {
                Vector2 pos = input.getCrossHair();
                current = new Planet(pos.x + camOffsetX/scale.x, pos.y + camOffsetY, 1, world, scale);
                level.add(current);
            } else if (input.didA()) {
                Vector2 pos = input.getCrossHair();
                current = new Anchor(pos.x + camOffsetX/scale.x, pos.y + camOffsetY, JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale);
                level.add(current);
            } else if (input.didS()) {
                Vector2 pos = input.getCrossHair();
                current = new Star(pos.x + camOffsetX/scale.x, pos.y + camOffsetY, JsonAssetManager.getInstance().getEntry("star", TextureRegion.class), scale);
                level.add(current);
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
