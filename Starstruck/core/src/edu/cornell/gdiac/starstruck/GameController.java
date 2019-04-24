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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import java.util.*;

//import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.starstruck.Models.Enemy;
import edu.cornell.gdiac.starstruck.Models.Worm;
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

    int count;


    /** Time to delay after dying */

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


    /** The background for DEATH*/
    private Texture death;
    /** Opacity countdown for death screen */
    private float deathOp = 0f;

    /** Texture asset for the enemy */
    private FilmStrip enemyTexture;
    /** Texture asset for the enemy */
    private FilmStrip pinkwormTexture;
    /** Texture asset for the enemy */
    private FilmStrip greenwormTexture;
    /** Texture asset for rope */
    //private TextureRegion ropeTexture;

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** Location and animation information for enemy */
    private Enemy enemy;
    private Enemy pinkworm;
    private Enemy greenworm;

    /** Texture file for star bar*/
    private static final String PROGRESS_FILE = "default/starbar.png";

    /** Height of the star bar */
    private static int PROGRESS_HEIGHT = 54;
    /** Width of the rounded cap on left*/
    private static int PROGRESS_CAP_LEFT = 80;
    /** Width of the rounded cap on right*/
    private static int PROGRESS_CAP_RIGHT = 10;
    /** Width of the middle portion in texture atlas */
    private static int PROGRESS_MIDDLE = 338;


    /** Texture atlas to support a progress bar */
    private Texture statusBar;



    /** Left cap to the status background (grey region) */
    private TextureRegion statusBkgLeft;
    /** Middle portion of the status background (grey region) */
    private TextureRegion statusBkgMiddle;
    /** Right cap to the status background (grey region) */
    private TextureRegion statusBkgRight;
    /** Left cap to the status forground (colored region) */
    private TextureRegion statusFrgLeft;
    /** Middle portion of the status forground (colored region) */
    private TextureRegion statusFrgMiddle;
    /** Right cap to the status forground (colored region) */
    private TextureRegion statusFrgRight;

    /** The width of the progress bar */
    private int widthBar = 448;
    /** The x-coordinate of the center of the progress bar */
    private int initCenterX = 10;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

    private int totalStars;

    /**StarCount bar */

    private void drawStarBar(GameCanvas canvas) {
        OrthographicCamera camera = (OrthographicCamera) canvas.getCamera();

        float centerY = camera.position.y - ((float) canvas.getHeight())/2 + 3;
        float centerX = camera.position.x - ((float) canvas.getWidth())/2 + 10;

        //print(centerX*scale.x + (widthBar /2) - PROGRESS_CAP_RIGHT*scale.x);
        canvas.draw(statusBkgLeft, Color.WHITE, centerX - widthBar / (2*scale.x), centerY, PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
        canvas.draw(statusBkgRight, Color.WHITE, initCenterX*scale.x + (camera.position.x - (float) canvas.getWidth()/2) + (widthBar /2) - PROGRESS_CAP_RIGHT*0.56f*scale.x, centerY, PROGRESS_CAP_RIGHT, PROGRESS_HEIGHT);
        canvas.draw(statusBkgMiddle, Color.WHITE, centerX - widthBar / (2*scale.x) + PROGRESS_CAP_LEFT, centerY, widthBar - 2 * PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);

        canvas.draw(statusFrgLeft, Color.WHITE, centerX - widthBar / (2*scale.x), centerY, PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
        if (starCount > 0 && starCount != totalStars) {
            float span = starCount * ((PROGRESS_MIDDLE - 2 * PROGRESS_CAP_RIGHT)) / totalStars;
            //canvas.draw(statusFrgRight, Color.WHITE, initCenterX*scale.x + (camera.position.x - (float) canvas.getWidth()/2) + span/scale.x, centerY, PROGRESS_CAP_RIGHT, PROGRESS_HEIGHT);
            canvas.draw(statusFrgMiddle, Color.WHITE, centerX - widthBar / (2*scale.x) + PROGRESS_CAP_LEFT, centerY, span, PROGRESS_HEIGHT);
       } else if (starCount == totalStars) {
            canvas.draw(statusFrgLeft, Color.WHITE, centerX - widthBar / (2*scale.x), centerY, PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
            canvas.draw(statusFrgRight, Color.WHITE, initCenterX*scale.x + (camera.position.x - (float) canvas.getWidth()/2) + (widthBar /2) - PROGRESS_CAP_RIGHT*0.56f*scale.x, centerY, PROGRESS_CAP_RIGHT, PROGRESS_HEIGHT);
            canvas.draw(statusFrgMiddle, Color.WHITE, centerX - widthBar / (2*scale.x) + PROGRESS_CAP_LEFT, centerY, widthBar - 2 * PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
        }
        else {
            canvas.draw(statusFrgLeft, Color.WHITE, centerX - widthBar / (2*scale.x), centerY, PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
        }
    }

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


        death = JsonAssetManager.getInstance().getEntry("death screen", Texture.class);

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
    /** Camera zoom */
    private static final float ZOOM_FACTOR = 1f;
    /** Max max extension of rope */
    private static final int MAX_EXTEND = 50;
    /** Rope timer reset */
    private static final int ROPE_RESET = 9;
    /** Speed of camera in screen coordinates */
    private static final float CAMERA_SPEED = 10f;
    /** Gentle force to send avatar to planet cmoing off anchor */
    private static final float TO_PLANET = 100f;
    /** Reel force */
    private static final float REEL_FORCE = 7.5f;

    // Other game objects
    /** The position of the spinning barrier */
    private static Vector2 SPIN_POS = new Vector2(13.0f,12.5f);
    /** The initial position of the dude */
    private static Vector2 DUDE_POS = new Vector2(2.5f, 5.0f);

    /** Variable caches used for setting position on planet for enemy*/
    private Obstacle curPlanetEN;
    private Vector2 contactPointEN = new Vector2();
    private Vector2 contactDirEn = new Vector2();

    /** Settings of the game */
    private boolean switchOnJump = false;
    private boolean switchOnAnchor = false;
    private boolean twoplayer = false;

    // Physics objects for the game
    /** Reference to the character avatar */
    private AstronautModel avatar;
    /** Reference to the second character avatar*/
    private AstronautModel avatar2;
    /** End black hole */
    private PortalPair goal;
    /** List of anchors, temporary quick solution */
    private ArrayList<Anchor> anchors = new ArrayList<Anchor>();
    /** List of Rope */
    private ArrayList<Rope> ropes = new ArrayList<Rope>();
    /** List of PortalPairs */
    private ArrayList<PortalPair> portalpairs = new ArrayList<PortalPair>();
    /** Planets */
    private PlanetList planets;
    /** Planets */
    private Galaxy galaxy = Galaxy.WHIRLPOOL;
    /** Non planet objects when checking collisions */
    private boolean barrier;
    /** Rope */
    private Rope rope;
    /** Star collection count */
    private int starCount;
    /** Whether a star should be collected */
    private boolean collection;
    /** List of stars */
    public ArrayList<Star> stars = new ArrayList<Star>();
    /** Number of stars needed to open portal */
    private int winCount;
    /** Whether the goal is open */
    private boolean openGoal;
    /** Viewport width and height */
    private float camWidth;
    private float camHeight;
    /** Level bounds */
    private float xBound;
    private float yBound;
    /** Amount rope is extended */
    private int extendInt = 0;
    /** Level to load */
    private String loadFile;
    /** Listener for load data */
    private SaveListener loader;
    /** List of the planks in rope, used for presolve */
    ArrayList<Obstacle> ropeList;
    /** Whether astronaut hit a portal */
    private boolean portal;
    /** Countdown timer for portal */
    private int portalCount;
    /** Target for camera position */
    private Vector3 camTarget = new Vector3();

    /** Cache variable to store current planet being drawn*/
    private WheelObstacle planetCache;
    /** Portal cache */
    private Portal portalCache;
    private PortalPair portalpairCache;
    /** cache for stars */
    private Star starCache;
    /** Astronaut cache for portals*/
    private AstronautModel avatarCache;
    /** cache for reel directrion */
    private Vector2 reelCache;
    /** Obstacle cache */
    private Obstacle obstacleCache;

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
    public GameController(String loadFile) {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
        jsonReader = new JsonReader();
        level = new LevelModel();
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
        this.loadFile = loadFile;
        loader = new SaveListener();
    }
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
        loadFile = "main/tutorial.json";
        loader = new SaveListener();
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        level.dispose();
        //enemies.clear();
        levelFormat = jsonReader.parse(Gdx.files.internal("levels/" + loadFile));
        //levelFormat = jsonReadesystem.r.parse(Gdx.files.internal("levels/" + loadFile));
        level.populate(levelFormat);
        level.getWorld().setContactListener(this);

        setComplete(false);
        setFailure(false);
        assignLevelFields();
        populateLevel(); //Just to add enemies and special lists of objects

        count = 5;
        deathOp = 0f;
        portalpairCache = null;

        statusBar  = JsonAssetManager.getInstance().getEntry("starbar", Texture.class);
        // Break up the status bar texture into regions
        statusBkgLeft   = new TextureRegion(statusBar,0,0,PROGRESS_CAP_LEFT,PROGRESS_HEIGHT);
        statusBkgRight  = new TextureRegion(statusBar,statusBar.getWidth()-PROGRESS_CAP_RIGHT,0,PROGRESS_CAP_RIGHT,PROGRESS_HEIGHT);
        statusBkgMiddle = new TextureRegion(statusBar,PROGRESS_CAP_LEFT,0,PROGRESS_MIDDLE,PROGRESS_HEIGHT);

        int offset = statusBar.getHeight()-PROGRESS_HEIGHT;
        statusFrgLeft   = new TextureRegion(statusBar,0,offset,PROGRESS_CAP_LEFT,PROGRESS_HEIGHT);
        statusFrgRight  = new TextureRegion(statusBar,statusBar.getWidth()-PROGRESS_CAP_RIGHT,offset,PROGRESS_CAP_RIGHT,PROGRESS_HEIGHT);
        statusFrgMiddle = new TextureRegion(statusBar,PROGRESS_CAP_LEFT,offset,PROGRESS_MIDDLE,PROGRESS_HEIGHT);

        displayFont = JsonAssetManager.getInstance().getEntry("retro game", BitmapFont.class);

    }

    /**
     * Assign the fields of the game controller state to reference the fields of the level
     */
    private void assignLevelFields() {
        avatar = level.getPlayer1(); avatar2 = level.getPlayer2();
        avatar.setTwoPlayer(twoplayer); avatar2.setTwoPlayer(twoplayer);
        rope = level.getRope();
        objects = level.objects; planets = level.getPlanets();
        world = level.getWorld(); vectorWorld = level.getVectorWorld();
        enemies = level.getEnemies();
        goal = level.getGoal();
        //System.out.println("here ye here ye");
        //System.out.println(enemies);
        //System.out.println(enemies.size());
        //System.out.println(level.getEnemies());
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
     * Set level to a new json file
     * @param json Json file name to set
     */
    public void setJson(String json) {
        loadFile = json;
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        //Set zoom of camera
        Camera camera = canvas.getCamera();
        camWidth = 1280*ZOOM_FACTOR;
        camHeight = 720*ZOOM_FACTOR;
        camera.viewportWidth = camWidth;
        camera.viewportHeight = camHeight;
        camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 0);

        float a1x = avatar.getPosition().x * avatar.drawScale.x;
        float a2x = avatar2.getPosition().x * avatar2.drawScale.x;
        float xCam = (a1x + a2x) / 2;
        float a1y = avatar.getPosition().y * avatar.drawScale.y;
        float a2y = avatar2.getPosition().y * avatar2.drawScale.y;
        float yCam = (a1y + a2y) / 2;
        if (xCam < camWidth/2)
            xCam = camWidth/2;
        else if (xCam > xBound*scale.x - camWidth/2)
            xCam = xBound*scale.x - camWidth/2;
        if (yCam < camHeight/2)
            yCam = camHeight/2;
        else if (yCam > yBound*scale.y - camHeight/2)
            yCam = yBound*scale.y - camHeight/2;
        camera.position.set(xCam, yCam, 0);

        xBound = (1280*1.5f) / scale.x;
        yBound = (720*1.5f) / scale.y;

        stars = level.stars;
        anchors = level.anchors;
        portalpairs = level.portalpairs;

        winCount = level.winCount;
        openGoal = false;

        portal = false;
        portalCount = 0;

        // Add level goal
        float dwidth;
        float dheight;
        starCount = 0;
        collection = false;

        totalStars = stars.size();

        //rope.setReelForce(REEL_FORCE);

        //setSettings();

        // Create enemy TODO hardcoded bug enemy
//        dwidth  = enemyTexture.getRegionWidth()/scale.x;
//        dheight = enemyTexture.getRegionHeight()/scale.y;
//        enemy = new Enemy(26 + 2, 8 + 2, dwidth, dheight);
//        enemy.setDrawScale(scale);
//        enemy.setTexture(enemyTexture, 3, 10);
//        enemy.setName("bug");
//        addObject(enemy);

    }

    /**
     * Set the settings at the beginning of the level.
     */
    public void setSettings() {
        twoplayer = false;
        switchOnJump = false;
        switchOnAnchor = false;
    }

    /**
     * Was anchor pressed
     *
     * @return true if anchored was pressed
     */
    private boolean anchord() {
        return InputController.getInstance().didAnchor();
    }

    /**
     * For two player, was player 2 anchor pressed
     *
     * @return true if anchored2 was pressed
     */
    private boolean anchord1() { return InputController.getInstance().didAnchor1(); }

    /**
     * For two player, was player 1 anchor pressed
     *
     * @return true if anchored2 was pressed
     */
    private boolean anchord2() { return InputController.getInstance().didAnchor2(); }

    /**
     * Was switch pressed
     *
     * @return true if switch was pressed
     */
    private boolean switched() {
        return InputController.getInstance().didSwitch() && !twoplayer;
    }

    /**
     * Is the reel button being pressed
     *
     * @return true if down is being pressed
     */
    private boolean reeled() {
        return InputController.getInstance().heldDown() || InputController.getInstance().didDown();
    }

    private boolean reeled2() {
        return InputController.getInstance().heldS() || InputController.getInstance().didS();
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
        //avatar1.setActive(false);
        avatar1.setPosition(SPIN_POS.x, SPIN_POS.y);
        //avatar1.setLinearVelocity(reset);
        avatar1.setAngularVelocity(0);
        //avatar2.setUnAnchored();
        //avatar2.setActive(true);
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
    private void updateAnchor(AstronautModel avatar1, AstronautModel avatar2, float dt) {
        //If unanchored and anchor is hit
        if (avatar1.isActive() && !avatar1.getOnPlanet() && !avatar1.isAnchored() && anchord() && !twoplayer
                || twoplayer && !avatar1.getOnPlanet() && !avatar1.isAnchored() && anchord1()) {
            for (Anchor a : anchors) {
                SPIN_POS.set(a.getPosition());
                if (dist(avatar1.getPosition(), SPIN_POS) < ANCHOR_DIST) {
                    anchorHelp(avatar1, avatar2, a);
                    if (switchOnAnchor && !twoplayer) {
                        avatar1.setActive(false);
                        avatar2.setActive(true);
                    }
                    return;
                }
            }
        }

        if (avatar2.isActive() && !avatar2.getOnPlanet() && !avatar2.isAnchored() && anchord() && !twoplayer
                || twoplayer && !avatar2.getOnPlanet() && !avatar2.isAnchored() && anchord2()) {
            for (Anchor a : anchors) {
                SPIN_POS.set(a.getPosition());
                if (dist(avatar2.getPosition(), SPIN_POS) < ANCHOR_DIST) {
                    anchorHelp(avatar2, avatar1, a);
                    if (switchOnAnchor && !twoplayer) {
                        avatar1.setActive(true);
                        avatar2.setActive(false);
                    }
                    return;
                }
            }
        }

        //If avatar1 is already anchored check if anchored was hit
        if (avatar1.isAnchored()) {
            if (!avatar2.getOnPlanet() && !avatar2.isAnchored()) { //If avatar2 is in space, swing avatar2
                if (rope.stretched(dt, 3)) {
                    avatar2.swing = true;
                    if (!avatar2.isActive() && !twoplayer)
                        avatar2.setFixedRotation(true);
                }
            }
            if (InputController.getInstance().didPrimary() && avatar1.isActive() && !twoplayer
                    || twoplayer && InputController.getInstance().didPrimary()) { //If anchored was hit unanchor, uananchor and move
                //avatar1.setUnAnchored();
                //avatar2.swing = false;
                avatar2.setFixedRotation(false);
                if (!avatar2.getOnPlanet() && !avatar2.isAnchored()) { //set avatar1 to follow avatar2
                    //avatar.setVelocity(avatar2.getVelocity)
                    avatar1.follow = true;
                }

                else if (avatar2.getOnPlanet()) { // Apply a gentle force on avatar1 to planet
                    //setlinearvelocity(0)
                    //dir = avatar.curplanet -getposition, apply a gentle force
                    //avatar1.toplanet = true;
                    avatar1.anchorhop = true;
                }

                else if (avatar2.isAnchored()) { //If avatar2 is anchored Anchor hop
                    avatar1.anchorhop = true;
                }

                //if avatar.isanchored
                //vector2 dir = new Vector2(0, 1)
                //angle = avatar.getanchor
                //dir.rotaterad(angle)
                //dir.setlength(avatar.dude_jump)
                //avatar.setLinearVelocity(dir)
            }
        }

        //If avatar2 is already anchored check if anchored was hit
        if (avatar2.isAnchored()) {
            if (!avatar1.getOnPlanet() && !avatar1.isAnchored()) { //If avatar1 is in space, swing avatar1
                if (rope.stretched(dt, 3)) {
                    avatar1.swing = true;
                    if (!avatar1.isActive() && !twoplayer)
                        avatar1.setFixedRotation(true);
                }
            }
            if (InputController.getInstance().didPrimary() && avatar2.isActive() && !twoplayer
                    || twoplayer && InputController.getInstance().didW()) { //If anchored was hit unanchor, uananchor and move
                //avatar2.setUnAnchored();
                //avatar1.swing = false;
                avatar1.setFixedRotation(false);
                if (!avatar1.getOnPlanet() && !avatar1.isAnchored()) { //set avatar2 to follow avatar1
                    //avatar.setVelocity(avatar2.getVelocity)
                    avatar2.follow = true;
                }

                else if (avatar1.getOnPlanet()) { // Apply a gentle force on avatar2 to planet
                    //setlinearvelocity(0)
                    //dir = avatar.curplanet -getposition, apply a gentle force
                    //avatar2.toplanet = true;
                    avatar2.anchorhop = true;
                }

                else if (avatar1.isAnchored()) { //If avatar1 is anchored Anchor hop
                    avatar2.anchorhop = true;
                }
            }
        }
    }

    /**
     * Helper method for movement off the anchors
     *
     * @param avatarAnchor
     * @param avatar
     * @param dt
     */
    private void anchorMove(AstronautModel avatarAnchor, AstronautModel avatar, float dt) {
        float speed = avatar.lastVel.len();
        if (avatar.swing) {
            Vector2 dir = avatar.getPosition().cpy().sub(avatarAnchor.getPosition());
            if (avatar.lastVel.angle(dir) > 0)
                dir.rotate90(-1);
            else
                dir.rotate90(1);
            avatar.setLinearVelocity(dir.setLength(speed));
            avatar.swing = false;
        }
        if (avatarAnchor.follow) {
            avatarAnchor.setUnAnchored();
            avatarAnchor.setLinearVelocity(avatar.getLinearVelocity());
            avatarAnchor.follow = false;
        }
        if (avatarAnchor.toplanet) {
            avatarAnchor.setUnAnchored();
            avatarAnchor.setLinearVelocity(reset);
            Vector2 dir = avatar.curPlanet.getPosition().cpy().sub(avatarAnchor.getPosition());
            avatarAnchor.getBody().applyForce(dir.setLength(TO_PLANET), avatarAnchor.getPosition(), true);
            avatarAnchor.toplanet = false;
        }
        if (avatarAnchor.anchorhop) {
            avatarAnchor.setUnAnchored();
            Vector2 dir = new Vector2(0, 1);
            float angle = avatarAnchor.getAngle();
            dir.rotateRad(angle);
            dir.setLength(avatarAnchor.getJumpPulse());
            avatarAnchor.setLinearVelocity(dir);
            avatarAnchor.anchorhop = false;
        }
    }

    /**
     * Helper method to update camera
     */
    private void updateCamera() {
        OrthographicCamera camera = (OrthographicCamera) canvas.getCamera();
        float a1x = avatar.getPosition().x * avatar.drawScale.x;
        float a2x = avatar2.getPosition().x * avatar2.drawScale.x;
        float xCam = (a1x + a2x) / 2;
        float a1y = avatar.getPosition().y * avatar.drawScale.y;
        float a2y = avatar2.getPosition().y * avatar2.drawScale.y;
        float yCam = (a1y + a2y) / 2;

        if (portalpairCache != null && portalpairCache.isActive()) {
            boolean update = true;
            if (portalpairCache.isGoal()) {
                xCam = camera.position.x;
                yCam = camera.position.y;
            }
            else {
                a1x = portalpairCache.getPortal1().getPosition().x * scale.x;
                a2x = portalpairCache.getPortal2().getPosition().x * scale.x;
                xCam = (a1x + a2x) / 2;
                a1y = portalpairCache.getPortal1().getPosition().y * scale.y;
                a2y = portalpairCache.getPortal2().getPosition().y * scale.y;
                yCam = (a1y + a2y) / 2;
            }
        }

        if (isComplete()) {
            xCam = camera.position.x;
            yCam = camera.position.y;
            canvas.resetCamera();
        }

        if (xCam < camWidth/2)
            xCam = camWidth/2;
        else if (xCam > xBound*scale.x - camWidth/2)
            xCam = xBound*scale.x - camWidth/2;
        if (yCam < camHeight/2)
            yCam = camHeight/2;
        else if (yCam > yBound*scale.y - camHeight/2)
            yCam = yBound*scale.y - camHeight/2;

        camTarget.set(xCam, yCam, 0);
        Vector3 dir = camTarget.sub(camera.position);
        if (dir.len() >= CAMERA_SPEED)
            dir.setLength(CAMERA_SPEED);
        camera.position.add(dir);
        camera.update();
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
        InputController input = InputController.getInstance();
        //contactDir = contactPoint.cpy().sub(curPlanet.getPosition());
        contactDir.rotateRad(-(float) Math.PI / 2);
        if (!twoplayer) {
            float move = input.getHorizontal();
            if (input.didRight() || input.didLeft()) {
                avatar.setPlanetMove(contactDir.scl(move));
                avatar.setRight(input.didRight());
                if (input.didRight() && !input.leftPrevious() || input.didLeft() && !input.rightPrevious())
                    avatar.moving = true;
            }

            if (input.didPrimary() && !auto && !testC) {
                //print(contactPoint);
                avatar.setJumping(true);
                SoundController.getInstance().play(JUMP_FILE, JUMP_FILE, false, EFFECT_VOLUME);
                contactDir.set(avatar.getPosition().cpy().sub(curPlanet.getPosition()));
                avatar.setPlanetJump(contactDir);
                avatar.setOnPlanet(false);
                avatar.moving = false;
            }
        }

        else if (twoplayer) {
            if (avatar == avatar2) {
                float move = input.getHorizontal2();
                if (input.heldD() || input.heldA()) {
                    avatar.setPlanetMove(contactDir.scl(move));
                    avatar.setRight(input.heldD());
                    if (input.heldD() && !input.aPrevious() || input.heldA() && !input.dPrevious())
                        avatar.moving = true;
                }

                if (input.didW()) {
                    avatar.setJumping(true);
                    SoundController.getInstance().play(JUMP_FILE, JUMP_FILE, false, EFFECT_VOLUME);
                    contactDir.set(avatar.getPosition().cpy().sub(curPlanet.getPosition()));
                    avatar.setPlanetJump(contactDir);
                    avatar.setOnPlanet(false);
                    avatar.moving = false;
                }
            }

            if (avatar == this.avatar) {
                float move = input.getHorizontal();
                if (input.didRight() || input.didLeft()) {
                    avatar.setPlanetMove(contactDir.scl(move));
                    avatar.setRight(input.didRight());
                    if (input.didRight() && !input.leftPrevious() || input.didLeft() && !input.rightPrevious())
                        avatar.moving = true;
                }

                if (input.didPrimary()) {
                    //print(contactPoint);
                    avatar.setJumping(true);
                    SoundController.getInstance().play(JUMP_FILE, JUMP_FILE, false, EFFECT_VOLUME);
                    contactDir.set(avatar.getPosition().cpy().sub(curPlanet.getPosition()));
                    avatar.setPlanetJump(contactDir);
                    avatar.setOnPlanet(false);
                    avatar.moving = false;
                }
            }
        }
    }

    /**
     * Helper method to determine whether the rope is ok
     *
     * 'a' - compare rope length off of anchors
     * 'c' - compare rope length and circumference when both astronauts are on the same planet
     * 'p' - compare rope length when astronauts are on different planets
     * 'j' - uses force on rope joint
     *
     * @param avatar1 avatar1
     * @param avatar2 avatar2
     * @param rope rope
     * @param mode Which situation is rope being tested in
     * @return false if there is rope left, true if the rope is at its end
     */
    private boolean updateRope(AstronautModel avatar1, AstronautModel avatar2, Rope rope, char mode, float dt) {
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

        //Both are on the same planet, uses force on joint to test when inactive astronaut should move
        else if (mode == 'j') {
            if (rope.stretched(dt, 3)) {
                return true;
            }

        }

        return false;
    }

    /**
     * Find the PortalPair of the portal. Returns null if portal can't be found, means something's wrong
     * @param portal
     * @return
     */
    private PortalPair findPortalPair(Portal portal) {
        for(PortalPair p : portalpairs) {
            if (portal.getPortName().equals(p.getPortalName()))
                return p;
        }
        return null;
    }

    /**
     * If settings were switched
     */
    private void updateSettings() {
        InputController input = InputController.getInstance();
        if (input.didOne()) {
            twoplayer = !twoplayer;
            print("Toggled setting two player: " + twoplayer);
            avatar.setTwoPlayer(twoplayer);
            avatar2.setTwoPlayer(twoplayer);
            if (!twoplayer) {
                avatar.setActive(true);
                avatar2.setActive(false);
            }
        }
        if (input.didTwo() && !twoplayer) {
            switchOnJump = !switchOnJump;
            print("Toggled setting switch on jump: " + switchOnJump);
        }
        if (input.didThree() && !twoplayer) {
            switchOnAnchor = !switchOnAnchor;
            print("Toggled setting switch on anchor: " + switchOnAnchor);
        }
    }

    /**
     * Helper for update
     *
     * @param avatar Active avatar
     * @param avatar2 Other avatar
     */
    private void updateHelp(AstronautModel avatar, AstronautModel avatar2, float dt) {
        avatar.setGravity(vectorWorld.getForce(avatar.getPosition())); //gravity will be applied no matter what
        avatar2.setGravity(vectorWorld.getForce(avatar2.getPosition()));
        float angle;
        if (avatar.getOnPlanet()) { //If avatar is on the planet update control movement
            avatar.setFixedRotation(true);
            avatar.contactDir.set(avatar.getPosition().cpy().sub(avatar.curPlanet.getPosition()));
            angle = -avatar.contactDir.angleRad(new Vector2 (0, 1));
            avatar.setAngle(angle);
            if ((rope.stretched(dt, 3) && (!avatar2.getOnPlanet() || avatar2.curPlanet != avatar.curPlanet))) { //TODO player model would be great here
                avatar.only = true;
            }
            updateMovement(avatar, avatar.contactDir, (Planet) avatar.curPlanet, false);

            if (avatar2.getOnPlanet() && !twoplayer) { //Inactive astronaut
                avatar2.setFixedRotation(true);
                avatar2.contactDir.set(avatar2.getPosition().cpy().sub(avatar2.curPlanet.getPosition()));
                angle = -avatar2.contactDir.angleRad(new Vector2 (0, 1));
                avatar2.setAngle(angle);
                if (avatar.curPlanet == avatar2.curPlanet) { //If the two avatars are on the same planet, move inactive avatar
                    if (updateRope(avatar, avatar2, rope, 'j', dt)) {
                        avatar2.auto = true;
                        updateMovement(avatar2, avatar2.contactDir, (Planet)avatar2.curPlanet, true);
                        //avatar2.setLinearVelocity((avatar2.contactDir.cpy().rotateRad(-(float) Math.PI / 2)).setLength(avatar2.getMaxSpeed()));
                    }
                    else {
                        avatar2.setLinearVelocity(reset);
                    }
                }
                else { // Else if inactive is on a different planet, set it's location, restrict mvoement of other avatar
                    avatar2.setPosition(avatar2.lastPoint);
                    if (updateRope(avatar, avatar2, rope, 'p', dt)) {
                        avatar.setPosition(avatar.lastPoint);
                    }
                }
            }
        }
        else {
            if (!twoplayer) {
                avatar.setRotation(InputController.getInstance().getHorizontal());
                if (avatar2.getOnPlanet()) { //If the inactive avatar is on planet
                    avatar2.setFixedRotation(true);
                    avatar2.contactDir.set(avatar2.getPosition().cpy().sub(avatar2.curPlanet.getPosition()));
                    angle = -avatar2.contactDir.angleRad(new Vector2(0, 1));
                    avatar2.setAngle(angle);
                }
            }

            else {
                if (avatar == this.avatar) {
                    avatar.setRotation(InputController.getInstance().getHorizontal());
                }
                else
                    avatar.setRotation(InputController.getInstance().getHorizontal2());
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

        // +/- 1 for a little bit of buffer space because astronaut position is at its center
        if (!isFailure() && !isComplete() && ((avatar.getY() < -1 || avatar.getY() > yBound+1 || avatar.getX() < -1 || avatar.getX() > xBound+1)
                || (avatar2.getY() < -1 || avatar2.getY() > yBound+1 || avatar2.getX() < -1 || avatar2.getX() > xBound+1))
                && !avatar.getOnPlanet() && !avatar2.getOnPlanet()){ //&& !avatar.isAnchored() && !avatar2.isAnchored()
            if (portalpairCache != null && !portalpairCache.isGoal() || portalpairCache == null) {
                setFailure(true);
                return false;
            }
        }

        return true;
    }

    /**
     * Try resetting the current level to the level in loader; return true if successful.
     * @return If the level was successfully reset.
     */
    private boolean loadNewFile() {
        try {
            levelFormat = jsonReader.parse(Gdx.files.internal("levels/" + loader.file));
            level.populate(levelFormat);
            loadFile = loader.file;
            print(loadFile);
            loader.file = null;

            return true;
        } catch (Exception e) {
            loader.file = null;
            e.printStackTrace();
            return false;
        }
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
        updateCamera();

        if (isFailure()) return;

        if (loader.file != null) {
            if (loadNewFile()) {
                reset();
                return;
            }
        }
        if (InputController.getInstance().shiftHeld() && InputController.getInstance().didO()) {
            Gdx.input.getTextInput(loader, "Load...", "level.json", "");
        }

//        if (avatar.getOnPlanet()) avatar.setLinearVelocity(reset);
//        if (avatar2.getOnPlanet()) avatar2.setLinearVelocity(reset);

        updateSettings();

        if (switched()) {
            avatar.setActive(!avatar.isActive());
            avatar2.setActive(!avatar2.isActive());
            SoundController.getInstance().play(SWITCH_FILE,SWITCH_FILE,false,EFFECT_VOLUME);
        }

        //TODO hardcoded bug enemy
//        if ((dist(avatar.getPosition(), enemy.getPosition()) < 1f && avatar.getOnPlanet()
//                || dist(avatar2.getPosition(), enemy.getPosition()) < 1f && avatar2.getOnPlanet()) && !isComplete() && !testE)
//            setFailure(true);

        for (Enemy e : enemies) {
            if (e.getType() == ObstacleType.WORM) {
                ((Worm)e).setRight_bound(canvas.getCamera().position.x/scale.x + 640/scale.x);
            }
        }

//        avatar.setFixedRotation(false);
//        avatar2.setFixedRotation(false);

        updateAnchor(avatar, avatar2, dt);

        float angVel = 0.1f;
        if (avatar.isAnchored()) {
            if ((!avatar.isActive() && !twoplayer) || avatar.getRotation() == 0) { //Dampen rotation
                if (avatar.getAngularVelocity() > 0) {
                    if (avatar.getAngularVelocity() < angVel)
                        angVel = avatar.getAngularVelocity();
                    avatar.setAngularVelocity(avatar.getAngularVelocity() - angVel);
                }
                else if (avatar.getAngularVelocity() < 0) {
                    if (avatar.getAngularVelocity() > -angVel)
                        angVel = -avatar.getAngularVelocity();
                    avatar.setAngularVelocity(avatar.getAngularVelocity() + angVel);
                }
            }
            anchorMove(avatar, avatar2, dt);
        }

        if (avatar2.isAnchored()) {
            if ((!avatar2.isActive() && !twoplayer) || avatar2.getRotation() == 0) { //Dampen
                if (avatar2.getAngularVelocity() > 0) {
                    if (avatar2.getAngularVelocity() < angVel)
                        angVel = avatar2.getAngularVelocity();
                    avatar2.setAngularVelocity(avatar2.getAngularVelocity() - angVel);
                }
                else if (avatar2.getAngularVelocity() < 0) {
                    if (avatar2.getAngularVelocity() > -angVel)
                        angVel = -avatar2.getAngularVelocity();
                    avatar2.setAngularVelocity(avatar2.getAngularVelocity() + angVel);
                }
            }
            anchorMove(avatar2, avatar, dt);
        }

        //Collect star
        if (collection) {
            starCache.deactivatePhysics(world);
            if (!stars.remove(starCache)) print("star collection error in game controller");
            if (!objects.remove(starCache)) print("star collection error in game controller");
            starCount++;
            if (starCount >= winCount && !openGoal) {
                openGoal = true;
                goal.getPortal1().setOpen(true);
            }
            collection = false;
        }


        //TODO win condition
//        if (stars.isEmpty()) {
//            setComplete(true);
//        }

        if (portal && portalCount <= 0) {
            portalpairCache = findPortalPair(portalCache);
            if (!portalpairCache.isGoal() || portalpairCache.isGoal() && openGoal) {
                portalpairCache.teleport(world, avatarCache, rope);
                portalCount = 5;
            }
        }
        if (portalpairCache != null && portalpairCache.isActive()) {
            if (portalpairCache.isGoal() && !isFailure()) { //By this point we have already confirmed that goal is open
                setComplete(true);
            }
            if (avatarCache == avatar) {
                avatar2.setOnPlanet(false);
                avatar2.setUnAnchored();
                Vector2 dir = portalpairCache.trailPortal.getPosition().cpy().sub(avatar2.getPosition());
                dir.setLength(avatar.portalVel.len()*2);
                avatar2.setLinearVelocity(dir);
                avatar.setLinearVelocity(avatar.portalVel);
                //avatar.getBody().applyForce(avatar.portalVel, avatar.getPosition(), true);
            }
            else {
                avatar.setOnPlanet(false);
                avatar.setUnAnchored();
                Vector2 dir = portalpairCache.trailPortal.getPosition().cpy().sub(avatar.getPosition());
                dir.setLength(avatar2.portalVel.len()*2);
                avatar.setLinearVelocity(dir);
                avatar2.setLinearVelocity(avatar2.portalVel);
                //avatar2.getBody().applyForce(avatar2.portalVel, avatar2.getPosition(), true);
            }
        }

        portalCount--;
        portal = false;

        if (twoplayer) {
            if (reeled() && !avatar2.getOnPlanet() && !avatar2.isAnchored()) {
//                reelCache = avatar.getPosition().cpy().sub(avatar2.getPosition());
//                reelCache.setLength(REEL_FORCE);
//                avatar2.getBody().applyForceToCenter(reelCache, true);
                rope.reel(true);
            }
            updateHelp(avatar, avatar2, dt);

            if (reeled2() && !avatar.getOnPlanet() && !avatar.isAnchored()) {
//                reelCache = avatar2.getPosition().cpy().sub(avatar.getPosition());
//                reelCache.setLength(REEL_FORCE);
//                avatar.getBody().applyForceToCenter(reelCache, true);
                rope.reel(false);
            }
            updateHelp(avatar2, avatar, dt);
        }

        else { //twoplayer off
            if (avatar.isActive()) {
                if (reeled() && !avatar2.getOnPlanet() && !avatar2.isAnchored()) {
//                    reelCache = avatar.getPosition().cpy().sub(avatar2.getPosition());
//                    reelCache.setLength(REEL_FORCE);
//                    avatar2.getBody().applyForceToCenter(reelCache, true);
                    rope.reel(true);
                }
                updateHelp(avatar, avatar2, dt);
                if (testC) {
                    avatar.setFixedRotation(true);
                    avatar.setMovement(InputController.getInstance().getHorizontal());
                    avatar.setMovementV(InputController.getInstance().getVertical());
                }
            } else { //if avatar2 is active
                if (reeled() && !avatar.getOnPlanet() && !avatar.isAnchored()) {
//                    reelCache = avatar2.getPosition().cpy().sub(avatar.getPosition());
//                    reelCache.setLength(REEL_FORCE);
//                    avatar.getBody().applyForceToCenter(reelCache, true);
                    rope.reel(false);
                }
                updateHelp(avatar2, avatar, dt);
                if (testC) {
                    avatar2.setFixedRotation(true);
                    avatar2.setMovement(InputController.getInstance().getHorizontal());
                    avatar2.setMovementV(InputController.getInstance().getVertical());
                }
            }
        }

        if (!twoplayer && switchOnJump && (avatar.isJumping() || avatar2.isJumping()) && !avatar.isAnchored() && !avatar2.isAnchored() && !testC) {
            avatar.setActive(!avatar.isActive());
            avatar2.setActive(!avatar2.isActive());
        }

        avatar.applyForce();
        avatar2.applyForce();

        //TODO harcoded bug enemy
//        enemy.update(dt);
//        if (enemy.getOnPlanet()) {
//            enemy.setFixedRotation(true);
//            //enemy.setRotation(1);
//            contactDirEn = contactPointEN.cpy().sub(curPlanetEN.getPosition());
//            float angle = -contactDirEn.angleRad(new Vector2(0, 1));
//            enemy.setAngle(angle);
//            enemy.setPosition(contactPointEN);
//            contactDirEn.rotateRad(-(float) Math.PI / 2);
//            enemy.setPosition(contactPointEN.add(contactDirEn.setLength(BUG_SPEED)));
//            enemy.setGravity(vectorWorld.getForce(enemy.getPosition()));
//            enemy.applyForce();
//        }

        avatar.lastPoint.set(avatar.getPosition());
        avatar2.lastPoint.set(avatar2.getPosition());
        avatar.lastVel.set(avatar.getLinearVelocity());
        avatar2.lastVel.set(avatar2.getLinearVelocity());

        //Removed sound stuffs
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

            if ((bd1N.contains("avatar") || bd2N.contains("avatar")) && (
                    bd1N.contains("rope") || bd2N.contains("rope") ||
                            bd1N.contains("worm") || bd2N.contains("worm") ||
                            bd1N.contains("anchor") || bd2N.contains("anchor") ||
                            bd1N.contains("star") || bd2N.contains("star")
            ))
                barrier = true;
            else
                barrier = false;

            //If worm and astronaut touch and astronaut is not on planet then lose
            if (!isComplete() && (bd1 == avatar && !avatar.getOnPlanet() && bd2.getType() == ObstacleType.WORM
                    || bd2 == avatar && !avatar.getOnPlanet() && bd1.getType() == ObstacleType.WORM)) {
                setFailure(true);
            }
            if (!isComplete() && (bd1 == avatar2 && !avatar2.getOnPlanet() && bd2.getType() == ObstacleType.WORM
                    || bd2 == avatar2 && !avatar2.getOnPlanet() && bd1.getType() == ObstacleType.WORM)) {
                setFailure(true);
            }

            if (!isComplete() && ((bd1 == avatar || bd1 == avatar2)&& bd2 instanceof Enemy
                    || (bd2 == avatar || bd2 == avatar2) && bd1 instanceof Enemy)) {
                setFailure(true);
            }

            //Star collection
            if ((bd1.getType() == ObstacleType.STAR || bd2.getType() == ObstacleType.STAR) && count < 0) {
                if (bd1.getType() == ObstacleType.STAR) {
                    starCache = (Star)bd1;
                    obstacleCache = bd2;
                }
                else {
                    starCache = (Star)bd2;
                    obstacleCache = bd1;
                }
                if (starCache.getLoc().equals("space")) {
                    if (obstacleCache.getName().contains("avatar") || obstacleCache.getName().contains("rope")) {
                        collection = true;
                    }
                }
                else {
                    if (obstacleCache == avatar && avatar.getOnPlanet() || obstacleCache == avatar2 && avatar2.getOnPlanet()) {
                        collection = true;
                    }
                }
            }

            //Portal stuff
            if (bd1 == avatar && bd2.getType() == ObstacleType.PORTAL) {
                portal = true;
                avatarCache = avatar;
            }
            if (bd1.getType() == ObstacleType.PORTAL && bd2 == avatar) {
                portal = true;
                portalCache = (Portal)bd1;
                avatarCache = avatar;
            }
            if (bd1 == avatar2 && bd2.getType() == ObstacleType.PORTAL) {
                portal = true;
                portalCache = (Portal)bd2;
                avatarCache = avatar2;
            }
            if (bd1.getType() == ObstacleType.PORTAL && bd2 == avatar2) {
                portal = true;
                portalCache = (Portal)bd1;
                avatarCache = avatar2;
            }

            if ((bd1 == avatar || bd2 == avatar) && (bd1N.contains("planet") || bd2N.contains("planet")) && !barrier) {
                avatar.curPlanet = (bd1 == avatar) ? bd2 : bd1;
                avatar.setOnPlanet(true);
                // See if we have landed on the ground.
                if (((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                        (avatar.getSensorName().equals(fd1) && avatar != bd2))) {
                    avatar.setGrounded(true);
                    sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

            if ((bd1 == avatar2 || bd2 == avatar2) && (bd1N.contains("planet") || bd2N.contains("planet")) && !barrier) {
                avatar2.curPlanet = (bd1 == avatar2) ? bd2 : bd1;
                avatar2.setOnPlanet(true);
                // See if we have landed on the ground.
                if (((avatar2.getSensorName().equals(fd2) && avatar2 != bd1) ||
                        (avatar2.getSensorName().equals(fd1) && avatar2 != bd2))) {
                    avatar2.setGrounded(true);
                    sensorFixtures.add(avatar2 == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

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

            //Disable all collisions for worms
            if (bd1.getType() == ObstacleType.WORM || bd2.getType() == ObstacleType.WORM) {
                contact.setEnabled(false);
            }

            //Disable all collisions with portal
            if (bd1.getType() == ObstacleType.PORTAL || bd2.getType() == ObstacleType.PORTAL) {
                contact.setEnabled(false);
            }

            //Disables all collisions w rope
            if (bd1.getName().contains("rope") || bd2.getName().contains("rope")) {
                contact.setEnabled(false);
            }
            //Disables all anchor and star collisions
            if(bd1N.contains("anchor") || bd2N.contains("anchor") || bd1N.contains("star") || bd2N.contains("star")){
                contact.setEnabled(false);
            }
            //Enables collisions between rope and anchor
//            if (bd1.getName().contains("rope") && bd2.getName().contains("anchor")
//                    || bd1.getName().contains("anchor") && bd2.getName().contains("rope")) {
//                contact.setEnabled(true);
//            }

            //Disables collisions between ends of rope and anchors
//            ropeList = rope.getPlanks();
//            BoxObstacle plank0 = (BoxObstacle)ropeList.get(0);
//            BoxObstacle plank1 = (BoxObstacle)ropeList.get(1);
//            BoxObstacle plank4 = (BoxObstacle)ropeList.get(2);
//            BoxObstacle plank2 = (BoxObstacle)ropeList.get(ropeList.size()-2);
//            BoxObstacle plank3 = (BoxObstacle)ropeList.get(ropeList.size()-1);
//            BoxObstacle plank5 = (BoxObstacle)ropeList.get(ropeList.size()-3);
//            if (bd1N.contains("anchor") && (bd2 == plank0 || bd2 == plank1 || bd2 == plank2 || bd2 == plank3)
//                    || bd2N.contains("anchor") && (bd1 == plank0 || bd1 == plank1 || bd1 == plank2 || bd1 == plank3)) {
//                contact.setEnabled(false);
//            }

            //Enables collisions between rope and planet
            if (bd1.getName().contains("rope") && bd2.getName().contains("planet")
                    || bd1.getName().contains("planet") && bd2.getName().contains("rope")) {
                contact.setEnabled(true);
            }
            //Disable collisions between astronauts
            if (bd1.getName().contains("avatar") && bd2.getName().contains("avatar")) {
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

        if (count >= 0) {
            count --;
            return;
        }

        OrthographicCamera cam = (OrthographicCamera)canvas.getCamera();

        canvas.clear();
        level.draw(canvas);

        canvas.begin();
        drawStarBar(canvas);
        canvas.end();

        if (isFailure()) {
            displayFont.setColor(Color.RED);
            canvas.begin(); // DO NOT SCALE
            //canvas.drawTextCentered("u ded :(", displayFont, 0.0f);
            //canvas.drawText("u ded :(", displayFont, cam.position.x-140, cam.position.y+30);
            //canvas.draw(background, Color.WHITE, x, y,canvas.getWidth(),canvas.getHeight());
//            deathOp += 0.0001f;
//            deathOp *= 1.05;
            deathOp += 0.01f;
            Color drawColor = new Color(1,1,1, deathOp);
            canvas.draw(death, drawColor, cam.position.x - canvas.getWidth()/2, cam.position.y - canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight());
            canvas.end();
        }

        if (isComplete()){
            displayFont.setColor(Color.GREEN);
            canvas.begin();
            canvas.drawText("Yay! :):)", displayFont, cam.position.x-140, cam.position.y+30);
            canvas.end();
        }

        if(isDebug()){
            canvas.beginDebug();
            for (Enemy e : enemies) {
                if (isDebug()) e.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }




}