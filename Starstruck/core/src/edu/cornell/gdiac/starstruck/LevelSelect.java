/*
 * GameController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.starstruck.Models.Enemy;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.util.SoundController;

import java.util.ArrayList;

//import edu.cornell.gdiac.physics.*;
//import edu.cornell.gdiac.starstruck.Models.PlayerModel;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class LevelSelect extends WorldController implements ContactListener {

    /** The reader to process JSON files */
    private JsonReader jsonReader;
    /** The JSON asset directory */
    private JsonValue  assetDirectory;
    /** The JSON defining the level model */
    private JsonValue  levelFormat;
    /** Reference to the game level */
    protected LevelModel level;

    /** The sound file for a jump */
    private static final String JUMP_FILE = "jump";
    /** The sound file for a landing */
    private static final String LAND_FILE = "land";
    /** The sound file for a collision */
    private static final String COLLISION_FILE = "anchor";
    /** The sound file for a character switch */
    private static final String SWITCH_FILE = "switch";
    /** The sound file to anchor */
    private static final String ANCHOR_FILE = "anchor";
    /** Space sounds */
    private static final String SPACE_SOUNDS = "space sounds";

    /** Texture asset for the enemy */
    private FilmStrip enemyTexture;
    /** Texture asset for the enemy */
    private FilmStrip pinkwormTexture;
    /** Texture asset for the enemy */
    private FilmStrip greenwormTexture;


    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** Cache variable to store current planet being drawn*/
    private WheelObstacle planetCache;


    /** Location and animation information for enemy */
    private Enemy enemy;

    private Enemy pinkworm;

    private Enemy greenworm;

    /**
     * Load the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {
        if (platformAssetState != AssetState.LOADING) {
            return;
        }

        JsonAssetManager.getInstance().allocateDirectory();


        enemyTexture = JsonAssetManager.getInstance().getEntry("orange bug", FilmStrip.class);
        pinkwormTexture = JsonAssetManager.getInstance().getEntry("pink worm", FilmStrip.class);
        greenwormTexture = JsonAssetManager.getInstance().getEntry("green worm", FilmStrip.class);

        // TODO sound
        SoundController sounds = SoundController.getInstance();
        sounds.allocate("jump");
        sounds.allocate("land");
        sounds.allocate("anchor");
        sounds.allocate("switch");
        sounds.allocate("space sounds");

        super.loadContent(manager);
        platformAssetState = AssetState.COMPLETE;

        planets = new PlanetList(scale);
    }

    // Physics constants for initialization
    /** The new heavier gravity for this world (so it is not so floaty) */
    private static final float  DEFAULT_GRAVITY = 0f;//-14.7f;
    /** The density for most physics objects */
    private static final float  BASIC_DENSITY = 0.0f;
    /** The density for a bullet */
    private static final float  HEAVY_DENSITY = 10.0f;
    /** Friction of most platforms */
    private static final float  BASIC_FRICTION = 0.4f;
    /** The restitution for all physics objects */
    private static final float  BASIC_RESTITUTION = 0.1f;
    /** The width of the rope bridge */
    private static final float  BRIDGE_WIDTH = 6.0f;
    /** Offset for bullet when firing */
    private static final float  BULLET_OFFSET = 0.2f;
    /** The speed of the bullet after firing */
    private static final float  BULLET_SPEED = 20.0f;
    /** The volume for sound effects */
    private static final float EFFECT_VOLUME = 0.8f;
    /** The volume for music */
    private static final float MUSIC_VOLUME = 0.3f;
    /** The distance from an anchor at which an astronaut will be able to anchor */
    private static float ANCHOR_DIST = 1f;
    /** Speed of bug */
    private static final float BUG_SPEED = 0.01f;
    /** Speed of astronaut on planet */
    private static final float PLANET_SPEED = 0.03f;
    /** 0 vector 2 */
    private static final Vector2 reset = new Vector2(0, 0);
    /** Turns off enemy collisions for testing */
    public static final boolean testE = true;
    /** Allows manual control of astronaut in space for testing */
    public static final boolean testC = true;

    // Physics objects for the game
    /** Reference to the character avatar */
    private AstronautModel avatar;
    /** Planets */
    private PlanetList planets;
    /** Planets */
    private Galaxy galaxy = Galaxy.WHIRLPOOL;

    /** Reference to the goalDoor (for collision detection) */
//    private BoxObstacle goalDoor;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * Return a reference to the primary avatar
     * @return Return a reference to the primary avatar.
     */
    public AstronautModel getAvatar() { return avatar; }

    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public LevelSelect() {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
//        jsonReader = new JsonReader();
//        level = new LevelModel();
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        level.dispose();

//        levelFormat = jsonReader.parse(Gdx.files.internal("levels/test.json"));
//        level.getWorld().setContactListener(this);

        setComplete(false);
        setFailure(false);
        assignLevelFields();
        populateLevel(); //Just to add enemies
    }

    /**
     * Assign the fields of the game controller state to reference the fields of the level
     */
    private void assignLevelFields() {
        avatar = level.getPlayer1();
        objects = level.objects; planets = level.getPlanets();
        world = level.getWorld(); vectorWorld = level.getVectorWorld();
    }

    /**
     * Override superclass's setDebug to also edit the level's current state
     * @param d The new value for debug
     */
    public void setDebug(boolean d) {
        super.setDebug(d);
        level.setDebug(d);
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth;
        float dheight;
    }

    /**
     * Was anchor pressed
     *
     * @return true if space was pressed
     */
    private boolean anchord() {
        return InputController.getInstance().didDown();
    }

    /**
     * Was switch pressed
     *
     * @return true if switch was pressed
     */
    private boolean shifted() {
        return InputController.getInstance().didSpace();
    }

    /**
     * print method
     *
     * @param s what to print
     */
    protected void print(Object s) { System.out.println(s); }

    /**
     * Helper to find distance
     *
     * @param v1 v1
     * @param v2 v2
     * @return distance between v1 and v2
     */
    private float dist(Vector2 v1, Vector2 v2) {
        return (float) Math.sqrt((v1.x - v2.x)*(v1.x-v2.x) + (v1.y - v2.y) * (v1.y - v2.y));
    }

    /**
     * Helper method to move the camera with the astronauts
     */
    private void updateCam() {
        float xCam = avatar.getPosition().x * avatar.drawScale.x;
        if (xCam < canvas.getWidth()/2) xCam = canvas.getWidth()/2;
        canvas.getCamera().position.set(new Vector3(xCam, canvas.getCamera().position.y, 0));
        canvas.getCamera().update();
//        System.out.println(canvas.getCamera().position);
//        System.out.println(canvas.getHeight());
    }

    /**
     * Helper for update for control on planet
     *
     * @param avatar the active avatar
     * @param contactDir Up direction of avatar
     * @param curPlanet planet the avatar is currently on
     * @param auto Whether this astronaut is being controlled or acting on its own
     */
    private void updateMovement(AstronautModel avatar, Vector2 contactDir, Planet curPlanet, boolean auto) {
        //contactDir = contactPoint.cpy().sub(curPlanet.getPosition());
        contactDir.rotateRad(-(float) Math.PI / 2);
        float move = InputController.getInstance().getHorizontal();
        if (InputController.getInstance().didRight() || InputController.getInstance().didLeft()) {
            avatar.setPlanetMove(contactDir.scl(move));
            avatar.moving = true;
        }

        if (InputController.getInstance().didPrimary() && !auto && !testC) {
            //print(contactPoint);
            avatar.setJumping(true);
            SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
            contactDir.set(avatar.getPosition().cpy().sub(curPlanet.getPosition()));
            avatar.setPlanetJump(contactDir);
            avatar.setOnPlanet(false);
            avatar.moving = false;
        }
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

        if (!isFailure() && (avatar.getY() < - 2 || avatar.getY() > bounds.height + 2
                || avatar.getX() < -2)) {
            // || avatar.getX() > bounds.getWidth() + 1)) {
            setFailure(true);
            return false;
        }

        return true;
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        updateCam();

        if (isFailure()) return;


        avatar.applyForce();

        avatar.lastPoint.set(avatar.getPosition());

        //TODO Removed sound stuffs

//         If we use sound, we must remember this.
        SoundController.getInstance().update();
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
    public void beginContact(Contact contact) {
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

            // Check for win condition
            //TODO Removed win
//            if ((bd1 == avatar   && bd2 == goalDoor) ||
//                    (bd1 == goalDoor && bd2 == avatar)) {
//                setComplete(true);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        //avatar.setOnPlanet(false);

        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }

    }

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {
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

            //Turns off enemy avatar collisions entirely for testing
            if (testE) {
                if ((bd1.getName().contains("avatar") || bd2.getName().contains("avatar"))
                && ((bd1.getName().contains("bug") || bd2.getName().contains("bug"))
                || (bd1.getName().contains("worm") || bd2.getName().contains("worm"))))
                    contact.setEnabled(false);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Draw the physics objects to the canvas and the background
     *
     * The method draws all objects in the order that they weret added.
     *
     * @param delta The delay in seconds since the last update
     */
    public void draw(float delta) {
        canvas.clear();

        level.draw(canvas);

        if (isFailure()) {
            displayFont.setColor(Color.RED);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("u ded :(", displayFont, 0.0f);
            canvas.end();
        }
    }
}