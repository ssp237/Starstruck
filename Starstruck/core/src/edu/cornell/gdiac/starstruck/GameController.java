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
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import java.util.*;

//import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.starstruck.Models.Enemy;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.FilmStrip;
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
public class GameController extends WorldController implements ContactListener {

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
     * Preloads the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void preLoadContent(AssetManager manager) {
        if (platformAssetState != AssetState.EMPTY) {
            return;
        }

        platformAssetState = AssetState.LOADING;

        super.preLoadContent(manager);

        jsonReader = new JsonReader();
        assetDirectory = jsonReader.parse(Gdx.files.internal("levels/assets.json"));

        JsonAssetManager.getInstance().loadDirectory(assetDirectory);
    }

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

        planets = new PlanetList(galaxy, scale);
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
    public static final boolean testE = false;
    /** Allows manual control of astronaut in space for testing */
    public static final boolean testC = false;

    // Other game objects
    /** The position of the spinning barrier */
    private static Vector2 SPIN_POS = new Vector2(13.0f,12.5f);
    /** The initial position of the dude */
    private static Vector2 DUDE_POS = new Vector2(2.5f, 5.0f);
    /** Variable caches used for setting position on planet for avatar 1*/
    //private Vector2 contactPoint = new Vector2();

    /** Variable caches for setting position on planet for avatar 2 */
    //private Vector2 contactPoint2 = new Vector2();

    /** Variable caches used for setting position on planet for enemy*/
    private Obstacle curPlanetEN;
    private Vector2 contactPointEN = new Vector2();
    private Vector2 contactDirEn = new Vector2();

    /** Astronaut cache */
    //private AstronautModel avatarCache = new AstronautModel();

    // Physics objects for the game
    /** Reference to the character avatar */
    private AstronautModel avatar;
    /** Reference to the second character avatar*/
    private AstronautModel avatar2;
    /** List of anchors, temporary quick solution */
    private ArrayList<Anchor> anchors = new ArrayList<Anchor>();
    /** WHY GRAVITY */
    private ArrayList<Star> stars = new ArrayList<Star>();
    /** Planets */
    private PlanetList planets;
    /** Planets */
    private Galaxy galaxy = Galaxy.WHIRLPOOL;
    /** Check not barrier */
    private boolean barrier;
    /** Rope */
    private Rope rope;

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
     * Return a reference to the secondary avatar.
     * @return Return a reference to the secondary avatar.
     */
    public AstronautModel getAvatar2() { return avatar2; }

    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public GameController() {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
        jsonReader = new JsonReader();
        level = new LevelModel();
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

        levelFormat = jsonReader.parse(Gdx.files.internal("levels/test.json"));
        level.populate(levelFormat);
        level.getWorld().setContactListener(this);

        setComplete(false);
        setFailure(false);
        assignLevelFields();
        populateLevel(); //Just to add enemies
    }

    /**
     * Assign the fields of the game controller state to reference the fields of the level
     */
    private void assignLevelFields() {
        avatar = level.getPlayer1(); avatar2 = level.getPlayer2();
        rope = level.getRope();
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

        // Create enemy
        dwidth  = enemyTexture.getRegionWidth()/scale.x;
        dheight = enemyTexture.getRegionHeight()/scale.y;
        enemy = new Enemy(26 + 2, 8 + 2, dwidth, dheight);
        enemy.setDrawScale(scale);
        enemy.setTexture(enemyTexture, 3, 10);
        enemy.setName("bug");
        addObject(enemy);

        // Create pink worm enemy
        dwidth  = pinkwormTexture.getRegionWidth()/scale.x;
        dheight = pinkwormTexture.getRegionHeight()/scale.y;
        pinkworm = new Enemy(DUDE_POS.x + 10, DUDE_POS.y + 4, dwidth, dheight);
        pinkworm.setDrawScale(scale);
        pinkworm.setTexture(pinkwormTexture,14,7);
        pinkworm.setName("pinkworm");
        addObject(pinkworm);
        pinkworm.setVX(2f);

        // Create green worm enemy
        dwidth  = greenwormTexture.getRegionWidth()/scale.x;
        dheight = greenwormTexture.getRegionHeight()/scale.y;
        greenworm = new Enemy(DUDE_POS.x + 10, DUDE_POS.y + 2, dwidth, dheight);
        greenworm.setDrawScale(scale);
        greenworm.setTexture(greenwormTexture,14,6);
        greenworm.setName("greenworm");
        addObject(greenworm);
        greenworm.setVX(1.4f);

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
     * Helper method to anchor an astronaut
     *
     * @param avatar1 the avatar to be anchored
     * @param avatar2 the other avatar
     */
    private void anchorHelp(AstronautModel avatar1, AstronautModel avatar2, Anchor anchor) {  //Anchor astronaut 1 & set inactive, unanchor astronaut 2 & set active
        avatar1.setAnchored(anchor);
        avatar1.setActive(false);
        avatar1.setPosition(SPIN_POS.x, SPIN_POS.y);
        avatar1.setLinearVelocity(reset);
        avatar1.setAngularVelocity(0);
        avatar2.setUnAnchored();
        avatar2.setActive(true);
        SoundController.getInstance().play(ANCHOR_FILE,ANCHOR_FILE,false,EFFECT_VOLUME);

    }

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
     * Helper for update for anchoring
     *
     * @param avatar1 avatar 1
     * @param avatar2 avatar 2
     */
    private void updateAnchor(AstronautModel avatar1, AstronautModel avatar2) {
        //If both are unanchored and space is hit
        if (!avatar1.isAnchored() && !avatar2.isAnchored() && anchord()) {
            if (avatar1.isActive() && !avatar1.getOnPlanet()) {
                for (Anchor a : anchors) {
                    SPIN_POS.set(a.getPosition());
                    if (dist(avatar1.getPosition(), SPIN_POS) < ANCHOR_DIST) {
                        anchorHelp(avatar1, avatar2, a);
                        return;
                    }
                }
            }
            else if (avatar2.isActive() && !avatar2.getOnPlanet()) {
                for (Anchor a : anchors) {
                    SPIN_POS.set(a.getPosition());
                    if (dist(avatar2.getPosition(), SPIN_POS) < ANCHOR_DIST) {
                        anchorHelp(avatar2, avatar1, a);
                        return;
                    }
                }
            }
        }

        //If avatar1 is already anchored, check if space or shift was hit
        else if (avatar1.isAnchored()) {
            if (anchord() && !avatar2.getOnPlanet()) { //If space was hit and avatar2 is not on planet -- couldb e anchored
                for (Anchor a : anchors) {
                    SPIN_POS.set(a.getPosition());
                    if (dist(avatar2.getPosition(), SPIN_POS) < ANCHOR_DIST) {
                        anchorHelp(avatar2, avatar1, a);
                        return;
                    }
                }
            }
            else if (shifted()) { //If shift was hit unanchor avatar1 and make active
                SoundController.getInstance().play(ANCHOR_FILE,ANCHOR_FILE,false,EFFECT_VOLUME);
                avatar1.setUnAnchored();
                avatar1.setActive(true);
                return;
            }
        }

        //If avatar2 is already anchored, check if space or shift was hit
        else if (avatar2.isAnchored()) {
            if (anchord() && !avatar1.getOnPlanet()) { //If space was hit and avatar1 is not on planet -- could be anchored
                for (Anchor a : anchors) {
                    SPIN_POS.set(a.getPosition());
                    if (dist(avatar1.getPosition(), SPIN_POS) < ANCHOR_DIST) {
                        anchorHelp(avatar1, avatar2, a);
                        return;
                    }
                }
            }
            else if (shifted()) { //If shift was hit unanchor avatar2 and make active
                SoundController.getInstance().play(ANCHOR_FILE,ANCHOR_FILE,false,EFFECT_VOLUME);
                avatar2.setUnAnchored();
                avatar2.setActive(true);
                return;
            }
        }
    }

    /**
     * Helper method to move the camera with the astronauts
     */
    private void updateCam() {
        float a1x = avatar.getPosition().x * avatar.drawScale.x;
        float a2x = avatar2.getPosition().x * avatar2.drawScale.x;
        float xCam = (a1x + a2x) / 2;
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
     * Helper method to determine whether the rope is ok
     *
     * 'a' - compare rope length off of anchors
     * 'c' - compare rope length and circumference when both astronauts are on the same planet
     * 'p' - compare rope length when astronauts are on different planets
     *
     * @param avatar1 avatar1
     * @param avatar2 avatar2
     * @param rope rope
     * @param mode Which situation is rope being tested in
     * @return false if there is rope left, true if the rope is at its end
     */
    private boolean updateRope(AstronautModel avatar1, AstronautModel avatar2, Rope rope, char mode) {
        float dist = dist(avatar1.getPosition(), avatar2.getPosition());
        float length = rope.getLength();

        if (mode == 'a') {
            // If avatar1 is anchored
            if ((avatar1.isAnchored()) && !avatar2.getOnPlanet()) { //avatar1.getOnPlanet() ||
                if (dist >= length) {
//                    avatar2.setGravity(reset);
//                    avatar2.setLinearVelocity(reset);
//                    avatar1.setLinearVelocity(reset);
                    avatar2.setPosition(avatar2.lastPoint);
                    return true;
                }
            }
            // If avatar2 is anchored
            else if ((avatar2.isAnchored()) && !avatar1.getOnPlanet()) { //avatar2.getOnPlanet() ||
                if (dist >= length) {
//                    avatar1.setGravity(reset);
//                    avatar1.setLinearVelocity(reset);
//                    avatar2.setLinearVelocity(reset);
                    avatar1.setPosition(avatar1.lastPoint);
                    return true;
                }
            }
        }

        //avatar1 is active, avatar2 is not, both are on the same planet
        else if (mode == 'c') {
            float theta = 2 * (float) Math.asin((dist/2) / ((Planet) avatar1.curPlanet).getRadius());
            Float arc = (float) (2 * Math.PI * ((Planet) avatar1.curPlanet).getRadius() * (theta / (2*Math.PI)));
            if (arc >= length || arc.isNaN()) {
                return true;
            }
        }

        //If astronauts are too far return true without doing anything
        else if (mode == 'p') {
            if (dist >= length) {
                return true;
            }
        }

        return false;
    }

    /**
     * Helper for update
     *
     * @param avatar Active avatar
     * @param avatar2 Other avatar
     */
    private void updateHelp(AstronautModel avatar, AstronautModel avatar2) {
        avatar.setGravity(vectorWorld.getForce(avatar.getPosition())); //gravity will be applied no matter what
        avatar2.setGravity(vectorWorld.getForce(avatar2.getPosition()));
//        print(avatar.gravity);
//        print(avatar2.gravity);
        float angle;
        if (avatar.getOnPlanet()) { //If avatar is on the planet update control movement
            avatar.setFixedRotation(true);
            avatar.contactDir.set(avatar.getPosition().cpy().sub(avatar.curPlanet.getPosition()));
            angle = -avatar.contactDir.angleRad(new Vector2 (0, 1));
            avatar.setAngle(angle);
            updateMovement(avatar, avatar.contactDir, (Planet)avatar.curPlanet, false);

            if (avatar2.getOnPlanet()) { //Inactive astronaut
                avatar2.setFixedRotation(true);
                avatar2.contactDir.set(avatar2.getPosition().cpy().sub(avatar2.curPlanet.getPosition()));
                angle = -avatar2.contactDir.angleRad(new Vector2 (0, 1));
                avatar2.setAngle(angle);
                if (avatar.curPlanet == avatar2.curPlanet) { //If the two avatars are on the same planet, move inactive avatar
                    if (updateRope(avatar, avatar2, rope, 'c')) {
                        updateMovement(avatar2, avatar2.contactDir, (Planet)avatar2.curPlanet, true);
                    }
                }
                else { // Else if inactive is on a different planet, set it's location, restrict mvoement of other avatar
                    avatar2.setPosition(avatar2.lastPoint);
                    if (updateRope(avatar, avatar2, rope, 'p')) {
                        avatar.setPosition(avatar.lastPoint);
                    }
                }
            }

            else if (avatar2.isAnchored()) { //If inactive avatar is anchored restrict rope length
                if (updateRope(avatar, avatar2, rope, 'p')) {
                    avatar.setPosition(avatar.lastPoint);
                }
            }
        }
        else {
            avatar.setRotation(InputController.getInstance().getHorizontal());
            if (avatar2.getOnPlanet()) { //If the inactive avatar is on planet
                avatar2.setFixedRotation(true);
                avatar2.contactDir.set(avatar2.getPosition().cpy().sub(avatar2.curPlanet.getPosition()));
                angle = -avatar2.contactDir.angleRad(new Vector2 (0, 1));
                avatar2.setAngle(angle);
            }
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

        if (!isFailure() && (avatar2.getY() < - 2 || avatar2.getY() > bounds.height + 2
                || avatar2.getX() < -2)) {
            // || avatar2.getX() > bounds.getWidth() + 1)) {
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

        if (shifted()) {
            avatar.setActive(!avatar.isActive());
            avatar2.setActive(!avatar2.isActive());
            SoundController.getInstance().play(SWITCH_FILE,SWITCH_FILE,false,EFFECT_VOLUME);
        }

        if ((dist(avatar.getPosition(), enemy.getPosition()) < 1f || dist(avatar2.getPosition(), enemy.getPosition()) < 1f) && !testE)
            setFailure(true);

        if (pinkworm.getPosition().x > 19 || pinkworm.getPosition().x < 12) {
            pinkworm.setVX(-pinkworm.getVX());
        }

        if (greenworm.getPosition().x > 19 || greenworm.getPosition().x < 10) {
            greenworm.setVX(-greenworm.getVX());
        }

        avatar.setFixedRotation(false);
        avatar2.setFixedRotation(false);

        updateAnchor(avatar, avatar2);
        if (avatar.isAnchored()) avatar.setFixedRotation(true);
        if (avatar2.isAnchored()) avatar2.setFixedRotation(true);

        if (avatar.isActive()) {
            updateHelp(avatar, avatar2);
            if (testC) {
                avatar.setFixedRotation(true);
                avatar.setMovement(InputController.getInstance().getHorizontal());
                avatar.setMovementV(InputController.getInstance().getVertical());
            }
        }
        else { //if avatar2 is active
            updateHelp(avatar2, avatar);
            if (testC) {
                avatar2.setFixedRotation(true);
                avatar2.setMovement(InputController.getInstance().getHorizontal());
                avatar2.setMovementV(InputController.getInstance().getVertical());
            }
        }

//        if ((avatar.isJumping() || avatar2.isJumping()) && !avatar.isAnchored() && !avatar2.isAnchored() && !testC) {
//            avatar.setActive(!avatar.isActive());
//            avatar2.setActive(!avatar2.isActive());
//        }

        avatar.applyForce();
        avatar2.applyForce();

        enemy.update(dt);
        if (enemy.getOnPlanet()) {
            enemy.setFixedRotation(true);
            //enemy.setRotation(1);
            contactDirEn = contactPointEN.cpy().sub(curPlanetEN.getPosition());
            float angle = -contactDirEn.angleRad(new Vector2(0, 1));
            enemy.setAngle(angle);
            enemy.setPosition(contactPointEN);
            contactDirEn.rotateRad(-(float) Math.PI / 2);
            enemy.setPosition(contactPointEN.add(contactDirEn.setLength(BUG_SPEED)));
            enemy.setGravity(vectorWorld.getForce(enemy.getPosition()));
            enemy.applyForce();
        }

        avatar.lastPoint.set(avatar.getPosition());
        avatar2.lastPoint.set(avatar2.getPosition());

        //TODO Removed sound stuffs
//        if (avatar.isJumping() || avatar2.isJumping()) {
//            SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
//        }

//        avatar.setJumping(false);
//        avatar2.setJumping(false);

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

            //System.out.println(bd1N + bd2N);

            if ((bd1N.contains("avatar") || bd2N.contains("avatar")) && (
                    bd1N.contains("rope") || bd2N.contains("rope") ||
                            bd1N.contains("worm") || bd2N.contains("worm") ||
                            bd1N.contains("anchor") || bd2N.contains("anchor") ||
                            bd1N.contains("star") || bd2N.contains("star")
            ))
                barrier = true;
            else
                barrier = false;

            if ((bd1.getName().contains("worm") || bd2.getName().contains("worm"))
                    && (bd1.getName().contains("avatar") || bd2.getName().contains("avatar")) && !testE) {
                setFailure(true);
            }

            if ((bd1 == avatar || bd2 == avatar) && (bd1N.contains("planet") || bd2N.contains("planet")) && !barrier) {
                avatar.curPlanet = (bd1 == avatar) ? bd2 : bd1;
                //contactPoint.set(contact.getWorldManifold().getPoints()[0].cpy());
                avatar.setOnPlanet(true);
                // See if we have landed on the ground.
                if (((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                        (avatar.getSensorName().equals(fd1) && avatar != bd2))) {
                    avatar.setGrounded(true);
//                    avatar.setOnPlanet(true);
//                    contactPoint.set(avatar.getPosition());
                    sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

            if ((bd1 == avatar2 || bd2 == avatar2) && (bd1N.contains("planet") || bd2N.contains("planet")) && !barrier) {
                avatar2.curPlanet = (bd1 == avatar2) ? bd2 : bd1;
                //contactPoint2.set(contact.getWorldManifold().getPoints()[0].cpy());
                avatar2.setOnPlanet(true);
                // See if we have landed on the ground.
                if (((avatar2.getSensorName().equals(fd2) && avatar2 != bd1) ||
                        (avatar2.getSensorName().equals(fd1) && avatar2 != bd2))) {
                    avatar2.setGrounded(true);
//                    avatar2.setOnPlanet(true);
//                    contactPoint.set(avatar2.getPosition());
                    sensorFixtures.add(avatar2 == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

//            /** Force astronaut's position on planet */
//            if ((bd1 == avatar || bd2 == avatar) && bd1 != avatar2 && bd2 != avatar2 && !barrier) {
//                curPlanet = (bd1 == avatar) ? bd2 : bd1;
//
//                if (curPlanet.getName().contains("planet")) {
//                    //Vector2 angle = contact.getWorldManifold().getPoints()[0].cpy().sub(objCache.getCenter());
//                    //contactDir.set(contact.getWorldManifold().getPoints()[0].cpy().sub(curPlanet.getCenter()));
//                    //contactPoint.set(contact.getWorldManifold().getPoints()[0].cpy());
//                    contactPoint.set(contact.getWorldManifold().getPoints()[0].cpy());
//                    avatar.setOnPlanet(true);
//                }
//
//                // See if we have landed on the ground.
//                if (((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
//                        (avatar.getSensorName().equals(fd1) && avatar != bd2))) {
//                    avatar.setGrounded(true);
//                    avatar.setOnPlanet(true);
//                    contactPoint.set(avatar.getPosition());
//                    sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
//                }
//            }
//
//            if ((bd1 == avatar2 || bd2 == avatar2) && bd1 != avatar && bd2 != avatar && !barrier) {
//                curPlanet2 = (bd1 == avatar2) ? bd2 : bd1;
//
//                if (curPlanet2.getName().contains("planet")) {
//                    //Vector2 angle = contact.getWorldManifold().getPoints()[0].cpy().sub(objCache.getCenter());
//                    //contactDir.set(contact.getWorldManifold().getPoints()[0].cpy().sub(curPlanet.getCenter()));
//                    //contactPoint.set(contact.getWorldManifold().getPoints()[0].cpy());
//                    contactPoint2.set(contact.getWorldManifold().getPoints()[0].cpy());
//                    avatar2.setOnPlanet(true);
//                }
//                // See if we have landed on the ground.
//                if (((avatar2.getSensorName().equals(fd2) && avatar2 != bd1) ||
//                        (avatar2.getSensorName().equals(fd1) && avatar2 != bd2))) {
//                    avatar2.setGrounded(true);
//                    avatar2.setOnPlanet(true);
//                    contactPoint2.set(avatar2.getPosition());
//                    sensorFixtures.add(avatar2 == bd1 ? fix2 : fix1); // Could have more than one ground
//                }
//
//            }

            if ((bd1 == enemy || bd2 == enemy) && bd1 != avatar2 && bd2 !=avatar2 && bd1 != avatar && bd2 !=avatar) {
                curPlanetEN = (bd1 == enemy) ? bd2 : bd1;
                if (curPlanetEN.getName().contains("planet")) {
                    contactPointEN.set(contact.getWorldManifold().getPoints()[0].cpy());
                    enemy.setOnPlanet(true);
                }
                // See if we have landed on the ground.
                if ((enemy.getSensorName().equals(fd2) && avatar != bd1 && avatar2 != bd1) ||
                        (enemy.getSensorName().equals(fd1) && avatar != bd2 && avatar2 != bd2)) {
                    enemy.setGrounded(true);
                    enemy.setOnPlanet(true);
                    contactPointEN.set(enemy.getPosition());
                    sensorFixtures.add(enemy == bd1 ? fix2 : fix1); // Could have more than one ground
                }

            }

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

        if ((avatar2.getSensorName().equals(fd2) && avatar2 != bd1) ||
                (avatar2.getSensorName().equals(fd1) && avatar2 != bd2)) {
            sensorFixtures.remove(avatar2 == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar2.setGrounded(false);
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

            if (bd1N.contains("avatar1") || bd2N.contains("avatar1"))
                System.out.println(bd1.getName() + bd2.getName());

            //Disables all collisions w rope
            if (bd1.getName().contains("rope") || bd2.getName().contains("rope")) {
                contact.setEnabled(false);
            }
            //Disables all anchor and star collisions
            if(bd1N.contains("anchor") || bd2N.contains("anchor") || bd1N.contains("star") || bd2N.contains("star")){
                contact.setEnabled(false);
            }
            //Enables collisions between rope and anchor
            if (bd1.getName().contains("rope") && bd2.getName().contains("anchor")
                    || bd1.getName().contains("anchor") && bd2.getName().contains("rope")) {
                contact.setEnabled(true);
            }
            //Enables collisions between rope and planet
            if (bd1.getName().contains("rope") && bd2.getName().contains("planet")
                    || bd1.getName().contains("planet") && bd2.getName().contains("rope")) {
                contact.setEnabled(true);
            }
            //Disable collisions between astronauts
            if (bd1.getName().contains("avatar") && bd2.getName().contains("avatar")) {
                //print("yeet");
                contact.setEnabled(false);
            }
            //Disables collisions between astronauts and anchors
            if (bd1.getName().contains("avatar") && bd2.getName().contains("anchor")
                    || bd1.getName().contains("anchor") && bd2.getName().contains("avatar")) {
                contact.setEnabled(false);
            }
            //Disables collisions between astronauts and stars
            if (bd1.getName().contains("avatar") && bd2.getName().contains("star")
                    || bd1.getName().contains("star") && bd2.getName().contains("avatar")) {
                contact.setEnabled(false);
            }
            //Disable the collision between anchors and rope on avatar
            int n = rope.nLinks() - 1 ;//(int) BRIDGE_WIDTH*2-1;
            if ((bd1N.contains("anchor") || bd2N.contains("anchor")) && (
                    bd1N.equals("rope_plank0") || bd2N.equals("rope_plank0") ||
                    bd1N.equals("rope_plank"+n) || bd2N.equals("rope_plank"+n))) {
                contact.setEnabled(false);
            }

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