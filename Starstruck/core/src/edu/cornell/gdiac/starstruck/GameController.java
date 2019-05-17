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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import java.awt.*;
import java.util.*;

//import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.starstruck.Models.*;
import edu.cornell.gdiac.starstruck.MenuMode;
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

    /** Delays */
    private int count;
    private int collectCount;

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
    /** Did we just die? (used to tell if we need to start loop with death screen) */
    private boolean justDead;
    private Vector2 deathPos;
    private FilmStrip deathSprite;
    private int deathAnimLoop = 0;
    /**Maximum animation loops for death */
    private static int MAX_ANIM = 1;
    private Vector2 winPos;
    private FilmStrip winSprite;
    private int winAnimLoop;
    private boolean replaying = false;

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

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
    /** All stars collected glow for status bar */
    private Texture statusGlow;
    /** Star glow asset for star bar */
    private Texture starGlow;

    /** The width of the progress bar */
    private int widthBar = 448;
    /** The x-coordinate of the center of the progress bar */
    private int initCenterX = 10;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;

    private int totalStars;

    private static Music music = MenuMode.getMusic();
    private String music_name = "menu";

    public static Music getMusic() {return music;}

    /**StarCount bar */

    private void drawStarBar(GameCanvas canvas) {
        OrthographicCamera camera = (OrthographicCamera) canvas.getCamera();

        Color tinge = Color.WHITE;
        if (gal == Galaxy.SOMBRERO)
            tinge = Color.WHITE;

        float centerY = camera.position.y + ((float) canvas.getHeight())/2 - 80;
        float centerX = camera.position.x - ((float) canvas.getWidth())/2 + 45;
        canvas.draw(statusBkgLeft, tinge, centerX - widthBar / (2*scale.x), centerY, PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
        canvas.draw(statusBkgRight, tinge, centerX + statusBkgMiddle.getRegionWidth() + 23, centerY, PROGRESS_CAP_RIGHT, PROGRESS_HEIGHT);
        canvas.draw(statusBkgMiddle, tinge, centerX - widthBar / (2*scale.x) + PROGRESS_CAP_LEFT, centerY, widthBar - 2 * PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);

        canvas.draw(statusFrgLeft, tinge, centerX - widthBar / (2*scale.x), centerY, PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
        if (starCount > 0 && starCount != totalStars) {
            float span = starCount * ((PROGRESS_MIDDLE - 2 * PROGRESS_CAP_RIGHT)) / totalStars;
            //canvas.draw(statusFrgRight, tinge, initCenterX*scale.x + (camera.position.x - (float) canvas.getWidth()/2) + span/scale.x, centerY, PROGRESS_CAP_RIGHT, PROGRESS_HEIGHT);
            canvas.draw(statusFrgMiddle, tinge, centerX - widthBar / (2*scale.x) + PROGRESS_CAP_LEFT, centerY, span, PROGRESS_HEIGHT);
        } else if (starCount == totalStars) {
            canvas.draw(statusFrgLeft, tinge, centerX - widthBar / (2*scale.x), centerY, PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
            canvas.draw(statusFrgRight, tinge, centerX + statusBkgMiddle.getRegionWidth() + 23, centerY, PROGRESS_CAP_RIGHT, PROGRESS_HEIGHT);
            canvas.draw(statusFrgMiddle, tinge, centerX - widthBar / (2*scale.x) + PROGRESS_CAP_LEFT, centerY, widthBar - 2 * PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
        }
        else {
            canvas.draw(statusFrgLeft, tinge, centerX - widthBar / (2*scale.x), centerY, PROGRESS_CAP_LEFT, PROGRESS_HEIGHT);
        }
        if (openGoal) {
            canvas.draw(starGlow, tinge, centerX-16, centerY-8, starGlow.getWidth(), starGlow.getHeight());
            //canvas.draw(starGlow, centerX-16, centerY-8);
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


        death = JsonAssetManager.getInstance().getEntry("dead background", Texture.class);
        deathSprite = JsonAssetManager.getInstance().getEntry("you dead", FilmStrip.class);
        winSprite = JsonAssetManager.getInstance().getEntry("you win", FilmStrip.class);

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
    private static final float EFFECT_VOLUME = 0.6f;
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
    //private boolean useController = true;

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
    /** List of tutorial points */
    private ArrayList<TutorialPoint> tutorialpoints = new ArrayList<TutorialPoint>();
    /** Planets */
    private PlanetList planets;
    /** Planets */
    private Galaxy galaxy = Galaxy.WHIRLPOOL;
    private Galaxy gal;
    /** Non planet objects when checking collisions */
    private boolean barrier;
    /** Rope */
    private Rope rope;
    /** Star collection count */
    private int starCount;
    /** Whether a star should be collected */
    private boolean collection;
    /** List of stars TODO make private*/
    public ArrayList<Star> stars = new ArrayList<Star>();
    /** List of stars to be removed */
    private ArrayList<Star> removed = new ArrayList<Star>();
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
    /** cache for reel direction */
    private Vector2 reelCache;
    /** Obstacle cache */
    private Obstacle obstacleCache;
    /** tutorial cache */
    private TutorialPoint tutPointCache;
    /** The task to currently draw */
    private TutorialPoint tutDrawCache;
    /** Whether tutorial is active */
    private boolean tutorial;

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
        deathPos = new Vector2(0,0);
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
        deathPos = new Vector2(0,0);
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
        level.populate(levelFormat);
        level.getWorld().setContactListener(this);

        justDead = isFailure();

        setComplete(false);
        setFailure(false);
        assignLevelFields();
        populateLevel(); //Just to add enemies and special lists of objects

        count = 5;
        collectCount = 60;
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

        starGlow = JsonAssetManager.getInstance().getEntry("starglow", Texture.class);

        statusGlow = JsonAssetManager.getInstance().getEntry("starbarglow", Texture.class);

        displayFont = JsonAssetManager.getInstance().getEntry("retro game", BitmapFont.class);

//        if (music != null) {
////            music.stop();
////            music.dispose();
////            music = null;
////        }

        if (MenuMode.getMusic() != null) {
            MenuMode.getMusic().stop();
            MenuMode.getMusic().dispose();
        }

        if (LevelSelect.getMusic() != null) {
            LevelSelect.getMusic().stop();
            LevelSelect.getMusic().dispose();
        }
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

        xBound = (1280*level.xPlay) / scale.x;
        yBound = (720*level.yPlay) / scale.y;

        stars = level.stars;
        anchors = level.anchors;
        portalpairs = level.portalpairs;
        tutorialpoints = level.tutpoints;
        for (TutorialPoint tut : tutorialpoints) {
            tut.setTwoplayer(twoplayer);
        }
        tutorial = false;
        tutPointCache = null;
        tutDrawCache = null;

        if (!anchors.isEmpty())
            ANCHOR_DIST = anchors.get(0).getRadius()*2;
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

        gal = level.getGalaxy();

        //rope.setReelForce(REEL_FORCE);
        //setSettings();
    }

    /**
     * Set the settings at the beginning of the level.
     */
    public void setSettings() {
        twoplayer = false;
        for (TutorialPoint tut : tutorialpoints) {
            tut.setTwoplayer(twoplayer);
        }
        switchOnJump = false;
        switchOnAnchor = false;
    }

    /**
     * Sets whether the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * Additionally, set the value for the
     *
     * @param value whether the level is failed.
     */
    public void setFailure(boolean value) {
        super.setFailure(value);
        if (death != null && !isFailure() && !justDead) {
            deathPos = new Vector2(-death.getWidth(), 0);
            //print(deathPos);
            deathAnimLoop = 0;
        }
    }

    /**
     * Sets whether the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @param value whether the level is completed.
     */
    public void setComplete(boolean value) {
        super.setComplete(value);
        if (death != null && !replaying) {
            winPos = new Vector2(-death.getWidth(), 0);
            winAnimLoop = 0;
        }
    }

    /**
     * Set win position when replaying a level. Additionaly, set replaying to true in order to have the screen wipe.
     * @param old Old win position
     */
    public void setWinPos(Vector2 old) {
        winPos = old.cpy();
        replaying = true;
        winAnimLoop = MAX_ANIM;
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
        if (avatar1.anchorHit()) {
            avatar.setAnchorHit(false);
            if (avatar1.isActive() && !avatar1.getOnPlanet() && !avatar1.isAnchored() && anchord() && !twoplayer
                    || twoplayer && !avatar1.getOnPlanet() && !avatar1.isAnchored() && anchord1()) {
                //print(avatar1.anchorHit());
                SPIN_POS.set(avatar1.getCurAnchor().getPosition());
                if (dist(avatar1.getPosition(), SPIN_POS) < ANCHOR_DIST) {
                    anchorHelp(avatar1, avatar2, avatar1.getCurAnchor());
                    if (avatar2.isAnchored() && rope.stretched(dt, 3)) {
                        if (avatar1.getCurAnchor().getLinearVelocity().len() != 0 || avatar2.getCurAnchor().getLinearVelocity().len() != 0)
                            avatar2.setUnAnchored();
                    }
                    if (switchOnAnchor && !twoplayer) {
                        avatar1.setActive(false);
                        avatar2.setActive(true);
                    }
                    return;
                }
            }
        }

        if (avatar2.anchorHit()) {
            avatar2.setAnchorHit(false);
            if (avatar2.isActive() && !avatar2.getOnPlanet() && !avatar2.isAnchored() && anchord() && !twoplayer
                    || twoplayer && !avatar2.getOnPlanet() && !avatar2.isAnchored() && anchord2()) {
                SPIN_POS.set(avatar2.getCurAnchor().getPosition());
                if (dist(avatar2.getPosition(), SPIN_POS) < ANCHOR_DIST) {
                    anchorHelp(avatar2, avatar1, avatar2.getCurAnchor());
                    if (avatar1.isAnchored() && rope.stretched(dt, 3)) {
                        if (avatar1.getCurAnchor().getLinearVelocity().len() != 0 || avatar2.getCurAnchor().getLinearVelocity().len() != 0)
                            avatar1.setUnAnchored();
                    }
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
            Vector2 anchorVel = avatar1.getCurAnchor().getLinearVelocity();
            if (anchorVel.len() != 0) {
                if (avatar2.getLinearVelocity().len() < anchorVel.len())
                    avatar2.setLinearVelocity(avatar2.getLinearVelocity().setLength(anchorVel.len()));
                if (avatar2.getLinearVelocity().len() == 0 && avatar2.curJumping)
                    avatar2.setLinearVelocity(anchorVel.cpy().scl(2));
//                if (!rope.stretched(dt, 3)) {
//                    Vector2 dir = avatar2.getPosition().cpy().sub(avatar1.getPosition()).setLength(anchorVel.len()/2);
//                    avatar2.setLinearVelocity(dir);
//                }
                if (!avatar2.getOnPlanet() && !avatar2.isAnchored() && !(avatar2.curJumping && !rope.stretched(dt, 3))) {
                    avatar2.curJumping = false;
                    if (!twoplayer && avatar2.isActive())
                        avatar2.control = true;
                    else if (twoplayer)
                        avatar2.control = true;
                }
            }
            else if (!avatar2.getOnPlanet() && !avatar2.isAnchored()) { //If avatar2 is in space, swing avatar2
                if (rope.stretched(dt, 3)) {
                    avatar2.swing = true;
                    if (!avatar2.isActive() && !twoplayer)
                        avatar2.setFixedRotation(true);
                }
            }
            //else if (avatar2.getOnPlanet() || avatar2.isAnchored()) avatar2.bossSwing = false;
            if (anchord() && avatar1.isActive() && !twoplayer
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
            Vector2 anchorVel = avatar2.getCurAnchor().getLinearVelocity();
            if (anchorVel.len() != 0) {
                if (avatar1.getLinearVelocity().len() < anchorVel.len())
                    avatar1.setLinearVelocity(avatar1.getLinearVelocity().setLength(anchorVel.len()));
                if (avatar1.getLinearVelocity().len() == 0 && avatar1.curJumping)
                    avatar1.setLinearVelocity(anchorVel.cpy().scl(2));
                if (!avatar1.getOnPlanet() && !avatar1.isAnchored() && !(avatar1.curJumping && !rope.stretched(dt, 3))) {
                    avatar1.curJumping = false;
                    if (!twoplayer && avatar1.isActive())
                        avatar1.control = true;
                    else if (twoplayer)
                        avatar1.control = true;
                }
            }
            else if (!avatar2.getOnPlanet() && !avatar2.isAnchored()) {
                if (rope.stretched(dt, 3)) {
                    avatar1.swing = true;
                    if (!avatar1.isActive() && !twoplayer)
                        avatar1.setFixedRotation(true);
                }
            }
            if (anchord() && avatar2.isActive() && !twoplayer
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
        Vector2 anchorVel = new Vector2(avatarAnchor.getCurAnchor().getLinearVelocity());
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
            Vector2 dir = avatar.getLinearVelocity().cpy();
//            if (avatarAnchor.getCurAnchor().getLinearVelocity().len() != 0) {
//                dir = anchorVel.cpy().scl(2);
//                avatar.setLinearVelocity(avatar.getLinearVelocity().cpy().add(anchorVel));
//            }
            avatarAnchor.setLinearVelocity(dir);
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
            if (anchorVel.len() != 0)
                dir.add(anchorVel);
            avatarAnchor.setLinearVelocity(dir);
            avatarAnchor.anchorhop = false;
        }
        if (avatar.control) {
            InputController input = InputController.getInstance();
            Vector2 dir = avatar.getPosition().cpy().sub(avatarAnchor.getPosition());
            if (!twoplayer){
                if (input.getHorizontal() != 0) {
                    int flip = (int)input.getHorizontal();
                    if (dir.y > 0) flip = -flip;
                    dir.rotate90(flip);
                }
                else {
                    if (avatar.lastVel.angle(dir) > 0)
                        dir.rotate90(-1);
                    else
                        dir.rotate90(1);
                }
            }
            else { //twoplayer
                if (avatar == this.avatar && input.getHorizontal() != 0) {
                    int flip = (int)input.getHorizontal();
                    if (dir.y > 0) flip = -flip;
                    dir.rotate90(flip);
                }
                else if (avatar == this.avatar2 && input.getHorizontal2() != 0) {
                    int flip = (int)input.getHorizontal2();
                    if (dir.y > 0) flip = -flip;
                    dir.rotate90(flip);
                }
                else {
                    if (avatar.lastVel.angle(dir) > 0)
                        dir.rotate90(-1);
                    else
                        dir.rotate90(1);
                }
            }
            dir.setLength(anchorVel.len()*2);
            dir.add(anchorVel);
            if (!rope.stretched(dt, 3)) {
                dir.add(avatar.getPosition().cpy().sub(avatarAnchor.getPosition()).setLength(anchorVel.len()/2));
            }
            avatar.setLinearVelocity(dir);
            avatar.control = false;
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

    private boolean controllerConnected(InputController input) {
        return input.getControlType() == ControllerType.CTRLONE || input.getControlType() == ControllerType.CTRLTWO;
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
            }
            if (input.didXbox()) {
                if (!auto) {
                    Vector2 dir = new Vector2(1, 0).rotateRad(input.getAngle());
                    avatar.setPlanetMove(dir);
                    avatar.moving = true;
                    boolean vertical = avatar.getAngle() <= 0 && input.xboxDown();
                    boolean horizontal = avatar.getAngle() > -Math.PI / 2 && avatar.getAngle() <= Math.PI / 2 && input.didRight();
                    avatar.setRight(vertical || horizontal);
                }
                else {
                    Vector2 dir = rope.getCenterPlank().getPosition().cpy().sub(avatar.getPosition());
                    float vertMove = input.getVertical();
                    avatar.setPlanetMove(contactDir.scl((float)Math.sqrt(move * move + vertMove * vertMove)));
                    if (Math.abs(contactDir.angle(dir)) > 90) {
                        //print("here");
                        avatar.setPlanetMove(contactDir.cpy().scl(-1));
                    }
                }
                avatar.moving = true;
            }
            if (input.didRight() && !input.leftPrevious() || input.didLeft() && !input.rightPrevious()) {
                avatar.moving = true;
            }
            if (anchord() && !auto && !testC) {
                //print(contactPoint);
                avatar.curJumping = true;
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
                }
                if (input.didXbox2()) {
                    Vector2 dir = new Vector2(1,0).rotateRad(input.getAngle2());
                    avatar.setPlanetMove(dir);
                    avatar.moving = true;
                    boolean vertical = avatar.getAngle() <= 0 && input.xboxDown2();
                    boolean horizontal = avatar.getAngle() > -Math.PI/2 && avatar.getAngle() <= Math.PI/2 && input.heldD();
                    avatar.setRight(vertical || horizontal);

                }
                if (input.heldD() && !input.aPrevious() || input.heldA() && !input.dPrevious()) {
                    avatar.moving = true;
                }
                if (input.didW()) {
                    avatar.curJumping = true;
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
                }
                if (input.didXbox()) {
                    Vector2 dir = new Vector2(1,0).rotateRad(input.getAngle());
                    //print(dir.scl(10));
                    avatar.setPlanetMove(dir);
                    avatar.moving = true;
                    boolean vertical = avatar.getAngle() <= 0 && input.xboxDown();
                    boolean horizontal = avatar.getAngle() > -Math.PI/2 && avatar.getAngle() <= Math.PI/2 && input.didRight();
                    avatar.setRight(vertical || horizontal);
                }
                if (input.didRight() && !input.leftPrevious() || input.didLeft() && !input.rightPrevious()) {
                    avatar.moving = true;
                }
                if (input.didPrimary()) {
                    avatar.curJumping = true;
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

//    private void updateMovementBug(Bug buggy, Vector2 contactDir, Planet curPlanet, boolean auto) {
//        //contactDir = contactPoint.cpy().sub(curPlanet.getPosition());
//        contactDir.rotateRad(-(float) Math.PI / 2);
//        //float move = InputController.getInstance().getHorizontal();
//        //if (InputController.getInstance().didRight() || InputController.getInstance().didLeft()) {
//            avatar.setPlanetMove(contactDir.scl(1));
//            //avatar.moving = true;
//        }
//
//        if (InputController.getInstance().didPrimary() && !auto && !testC) {
//            //print(contactPoint);
//            avatar.setJumping(true);
//            SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
//            contactDir.set(avatar.getPosition().cpy().sub(curPlanet.getPosition()));
//            avatar.setPlanetJump(contactDir);
//            avatar.setOnPlanet(false);
//            avatar.moving = false;
//        }
//    }

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
     * Find the Tutorial Point of this point. Returns null if portal can't be found, means something's wrong
     * @param point
     * @return
     */
    private TutorialPoint findTutPoint(Star point) {
        for(TutorialPoint p : tutorialpoints) {
            if (p.getName().equals(point.getTutName()))
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
            for (TutorialPoint tut : tutorialpoints) {
                tut.setTwoplayer(twoplayer);
            }
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
//        if (input.didFour()) {
//            useController = !useController;
//            print("Toggled setting use controller: " + useController);
//        }
    }

    private void updateTutorial() {
        if (!tutorialpoints.isEmpty()) {
            tutorial = true;
            tutPointCache = tutorialpoints.get(0);
            if (tutPointCache.complete()) {
                tutDrawCache = tutPointCache;
                Star pink = tutDrawCache.getPinkPoint();
                Star blue = tutDrawCache.getBluePoint();
                pink.deactivatePhysics(world);
                blue.deactivatePhysics(world);
                objects.remove(pink);
                objects.remove(blue);
                tutorialpoints.remove(tutDrawCache);
                if (!tutorialpoints.isEmpty()) {
                    tutPointCache = tutorialpoints.get(0);
                }
                else
                    tutorial = false;
            }
            //if (tutPointCache != null) tutPointCache.getTask().tick();
        }
        else tutorial = false;
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
                        avatar2.setLastFace(avatar.lastFace());
                        avatar2.setRight(avatar.getRight());
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
                if (!avatar.control)
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
                    if (!avatar.control)
                        avatar.setRotation(InputController.getInstance().getHorizontal());
                }
                else {
                    if (!avatar.control)
                        avatar.setRotation(InputController.getInstance().getHorizontal2());
                }
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

        //if (justDead) return false;

        InputController input = InputController.getInstance();
        input.readInput(bounds, scale);
        if (listener == null) {
            return true;
        }

        // Toggle debug
        if (input.didDebug()) {
            setDebug(!isDebug());
        }

        // Handle resets
        if (input.didGameReset()) {
            reset();
        }

        // Now it is time to maybe switch screens.
        if (input.exitUp()) {
            setFailure(false);
            listener.exitScreen(this, EXIT_SELECT);
            return false;
        } else if (input.didAdvance()) {
            setFailure(false);
            listener.exitScreen(this, EXIT_SELECT);
            return false;
        } else if (input.didRetreat()) {
            setFailure(false);
            listener.exitScreen(this, EXIT_EDIT);
            return false;
        } else if (countdown > 0) {
            countdown--;
        } else if (countdown == 0) {
            if (isFailure()) {
                reset();
            } else if (isComplete()) {
                listener.exitScreen(this, EXIT_SELECT, winSprite, winAnimLoop, winPos);
                return false;
            }
        }

        // +/- 1 for a little bit of buffer space because astronaut position is at its center
        if (!isFailure() && !isComplete() && ((avatar.getY() < -1 || avatar.getY() > yBound+1 || avatar.getX() < -1 || avatar.getX() > xBound+1)
                || (avatar2.getY() < -1 || avatar2.getY() > yBound+1 || avatar2.getX() < -1 || avatar2.getX() > xBound+1))
                && !avatar.getOnPlanet() && !avatar2.getOnPlanet() && !avatar.isAnchored() && !avatar2.isAnchored()) {
            if (portalpairCache != null && !portalpairCache.isGoal() || portalpairCache == null) {
                setFailure(true);
                return false;
            }
        }

        return true;
    }

    /**
     * Processes physics
     *
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }

        // Turn the physics engine crank.
        if (!justDead) world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                if (obj.getType() != ObstacleType.AZTEC_WHEEL || !justDead) {
                    obj.update(dt);
                }
            }
        }
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
        Urchin.tickTextures();

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

        updateSettings();

        updateTutorial();

        if (switched()) {
            avatar.setActive(!avatar.isActive());
            avatar2.setActive(!avatar2.isActive());
            if (avatar.isActive() && avatar.getOnPlanet()) {
                avatar.setLinearVelocity(reset);
            }
            if (avatar2.isActive() && avatar2.getOnPlanet()) {
                avatar2.setLinearVelocity(reset);
            }
            //SoundController.getInstance().play(SWITCH_FILE,SWITCH_FILE,false,EFFECT_VOLUME);
        }

        //TODO hardcoded bug enemy
//        if ((dist(avatar.getPosition(), enemy.getPosition()) < 1f && avatar.getOnPlanet()
//                || dist(avatar2.getPosition(), enemy.getPosition()) < 1f && avatar2.getOnPlanet()) && !isComplete() && !testE)
//            setFailure(true);

        for (Enemy e : enemies) {
//            if (e.getType() == ObstacleType.WORM) {
//                ((Worm)e).setRight_bound(canvas.getCamera().position.x/scale.x + 640/scale.x);
//            }
            if (e.getType() == ObstacleType.COLORED_BUG) {
                ColoredBug bug = (ColoredBug) e;
                AstronautModel astro = avatar;
                switch (bug.getColor()) {
                    case BLUE:
                        bug.setSleeping(dist(avatar.getPosition(), bug.getPosition()) > bug.range &&
                                (!avatar.getOnPlanet() || avatar.getCurPlanet() != bug.getCurPlanet()));
                        break;
                    case PINK:
                        bug.setSleeping(dist(avatar2.getPosition(), bug.getPosition()) > bug.range &&
                                (!avatar2.getOnPlanet() || avatar2.getCurPlanet() != bug.getCurPlanet()));
                        astro = avatar2;
                        break;
                }
//                if (!bug.isSleeping()) {
//                    bug.setSpeedSign(astro.getPosition().x < bug.getPosition().x ? -1 : 1);
//                }
            }
        }

//        avatar.setFixedRotation(false);
//        avatar2.setFixedRotation(false);

        updateAnchor(avatar, avatar2, dt);

        float angVel = 0.1f;
        if (avatar.isAnchored()) {
            if (avatar.getCurAnchor().getLinearVelocity().len() != 0) {
                if (rope.stretched(dt, 3))
                    avatar2.setOnPlanet(false);
            }
            if ((!avatar.isActive() && !twoplayer) || avatar.getRotation() == 0) { //Dampen rotation
                if (avatar.getAngularVelocity() > 0) {
                    if (avatar.getAngularVelocity() < angVel)
                        angVel = avatar.getAngularVelocity();
                    avatar.setAngularVelocity(avatar.getAngularVelocity() - angVel);
                } else if (avatar.getAngularVelocity() < 0) {
                    if (avatar.getAngularVelocity() > -angVel)
                        angVel = -avatar.getAngularVelocity();
                    avatar.setAngularVelocity(avatar.getAngularVelocity() + angVel);
                }
            }
            anchorMove(avatar, avatar2, dt);
        }

        angVel = 0.1f;
        if (avatar2.isAnchored()) {
            if (avatar2.getCurAnchor().getLinearVelocity().len() != 0) {
                if (rope.stretched(dt, 3))
                    avatar.setOnPlanet(false);
            }
            if ((!avatar2.isActive() && !twoplayer) || avatar2.getRotation() == 0) { //Dampen
                if (avatar2.getAngularVelocity() > 0) {
                    if (avatar2.getAngularVelocity() < angVel)
                        angVel = avatar2.getAngularVelocity();
                    avatar2.setAngularVelocity(avatar2.getAngularVelocity() - angVel);
                } else if (avatar2.getAngularVelocity() < 0) {
                    if (avatar2.getAngularVelocity() > -angVel)
                        angVel = -avatar2.getAngularVelocity();
                    avatar2.setAngularVelocity(avatar2.getAngularVelocity() + angVel);
                }
            }
            anchorMove(avatar2, avatar, dt);
        }

        //Collect star
        if (collection) {
            SoundController.getInstance().play(SWITCH_FILE, SWITCH_FILE, false, EFFECT_VOLUME);
            starCache.deactivatePhysics(world);
            removed.add(starCache);
            if (!stars.remove(starCache)) print("star collection error in game controller");
            //if (!objects.remove(starCache)) print("star collection error in game controller");
            starCount++;
            collection = false;
        }
        for (Star s : removed) {
            if (!s.removed) {
                if (s.countdown > 0) {
                    s.countdown--;
                    s.shrinkStar();
                } else if (s.sparkleCount > 0) {
                    s.sparkleCount--;
                    s.setSparkling(true);
                } else {
                    s.setRemove();
                }
                if (s.removeStar()) {
                    if (!objects.remove(s)) print("star collection error in game controller");
                    s.removed = true;
                }
            }

        }
        if (starCount >= winCount && !openGoal) { //&& tutorialpoints.isEmpty()
            //SOUNDS
            //COME HERE
            SoundController.getInstance().play(SWITCH_FILE,SWITCH_FILE,false,EFFECT_VOLUME);
            openGoal = true;
            goal.getPortal1().setOpen(true);
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
                if (!isComplete()) {
                    setComplete(true);
                }
            }
            if (avatarCache == avatar) {
                avatar2.setOnPlanet(false);
                avatar2.setUnAnchored();
                Vector2 dir = portalpairCache.trailPortal.getPosition().cpy().sub(avatar2.getPosition());
                dir.setLength(avatar.portalVel.len() * 2);
                avatar2.setLinearVelocity(dir);
                avatar.setLinearVelocity(avatar.portalVel);
                //avatar.getBody().applyForce(avatar.portalVel, avatar.getPosition(), true);
            } else {
                avatar.setOnPlanet(false);
                avatar.setUnAnchored();
                Vector2 dir = portalpairCache.trailPortal.getPosition().cpy().sub(avatar.getPosition());
                dir.setLength(avatar2.portalVel.len() * 2);
                avatar.setLinearVelocity(dir);
                avatar2.setLinearVelocity(avatar2.portalVel);
                //avatar2.getBody().applyForce(avatar2.portalVel, avatar2.getPosition(), true);
            }
        }

        portalCount--;
        portal = false;

        if (twoplayer) {
            Vector2 offset = new Vector2();
            float speed;
            if (reeled() && !avatar2.getOnPlanet() && !avatar2.isAnchored()) {
                reelCache = avatar.getPosition().cpy().sub(avatar2.getPosition());
//                reelCache.setLength(REEL_FORCE);
//                avatar2.getBody().applyForceToCenter(reelCache, true);
                if (!avatar.getOnPlanet()) {
//                    speed = avatar.getLinearVelocity().len();
                    offset = avatar2.getLinearVelocity().cpy().add(avatar.getLinearVelocity()).scl(0.5f);
                    //offset = avatar.getLinearVelocity().cpy();
                }
                rope.reel(true, reelCache, offset, !avatar.getOnPlanet());
            } else rope.setLinearVelocity(reset);
            updateHelp(avatar, avatar2, dt);

            offset.set(reset);
            if (reeled2() && !avatar.getOnPlanet() && !avatar.isAnchored()) {
                reelCache = avatar2.getPosition().cpy().sub(avatar.getPosition());
//                reelCache.setLength(REEL_FORCE);
//                avatar.getBody().applyForceToCenter(reelCache, true);
                if (!avatar2.getOnPlanet()) {
//                    speed = avatar2.getLinearVelocity().len();
                    offset = avatar.getLinearVelocity().cpy().add(avatar2.getLinearVelocity()).scl(0.5f);
                    //offset = avatar2.getLinearVelocity().cpy();
                }
                rope.reel(false, reelCache, offset, !avatar2.getOnPlanet());
            } else rope.setLinearVelocity(reset);
            updateHelp(avatar2, avatar, dt);
        } else { //twoplayer off
            if (avatar.getOnPlanet() && !avatar2.getOnPlanet()) {
                if (reeled()) {
                    reelCache = avatar.getPosition().cpy().sub(avatar2.getPosition());
                    rope.reel(true, reelCache, reset, false);
                } else rope.setLinearVelocity(reset);
            } else if (avatar2.getOnPlanet() && !avatar.getOnPlanet()) {
                if (reeled()) {
                    reelCache = avatar2.getPosition().cpy().sub(avatar.getPosition());
                    rope.reel(false, reelCache, reset, false);
                } else rope.setLinearVelocity(reset);
            }

            if (avatar.isActive()) {
                updateHelp(avatar, avatar2, dt);
                if (testC) {
                    avatar.setFixedRotation(true);
                    avatar.setMovement(InputController.getInstance().getHorizontal());
                    avatar.setMovementV(InputController.getInstance().getVertical());
                }
            } else { //if avatar2 is active
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


        //MUSIC

        if (level.getGalaxy() == Galaxy.DEFAULT) {
            if (music != null && !music_name.equals("tutorial")) {
                music.stop();
                music.dispose();
                music = null;
            }
            if (music == null || !music.isPlaying()) {
                music = Gdx.audio.newMusic(Gdx.files.internal("audio/tutorial_music.mp3"));
                music.play();
                music.setLooping(true);
                music_name = "tutorial";
            }
        } else if (level.getGalaxy() == Galaxy.WHIRLPOOL) {
            if (music != null && !music_name.equals("whirlpool")) {
                music.stop();
                music.dispose();
                music = null;
            }
            if (music == null || !music.isPlaying()) {
                music = Gdx.audio.newMusic(Gdx.files.internal("audio/whirlpool_music.mp3"));
                music.play();
                music.setLooping(true);
                music_name = "whirlpool";
            }

        } else if (level.getGalaxy() == Galaxy.MILKYWAY) {
            if (music != null && !music_name.equals("milkyway")) {
                music.stop();
                music.dispose();
                music = null;
            }
            if (music == null || !music.isPlaying()) {
                music = Gdx.audio.newMusic(Gdx.files.internal("audio/milky_way.mp3"));
                music.play();
                music.setLooping(true);
                music_name = "milkyway";
            }
        } else if (level.getGalaxy() == Galaxy.SOMBRERO) {
            if (music != null && !music_name.equals("sombrero")) {
                music.stop();
                music.dispose();
                music = null;
            }
            if (music == null || !music.isPlaying()) {
                music = Gdx.audio.newMusic(Gdx.files.internal("audio/sombrero_beat.mp3"));
                music.play();
                music.setLooping(true);
                music_name = "sombrero";
            }
//            System.out.println("in sombrero and music " + music.isPlaying());
//            System.out.println("music name = " + music_name);
        }


        if (level.getTalkingBoss() != null) {
            if (level.getTalkingBoss().getPosition().x + level.getTalkingBoss().getWidth() / 2 + 4 < 0) {
                level.remove(level.getTalkingBoss());
            }
        }
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

            if (!isComplete() && ((bd1 == avatar || bd1 == avatar2)&& (bd2 instanceof Enemy && !((Enemy) bd2).isSleeping())
                    || (bd2 == avatar || bd2 == avatar2) && (bd1 instanceof Enemy && !((Enemy) bd1).isSleeping()))) {
                setFailure(true);
            }

            //Star collection
            if ((bd1.getType() == ObstacleType.STAR || bd2.getType() == ObstacleType.STAR) && collectCount < 0) {
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
                portalCache = (Portal)bd2;
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
                if (!avatar.getOnPlanet()) {
                    avatar.setLinearVelocity(reset);
                }
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
                if (!avatar2.getOnPlanet()) {
                    avatar2.setLinearVelocity(reset);
                }
                avatar2.setOnPlanet(true);
                // See if we have landed on the ground.
                if (((avatar2.getSensorName().equals(fd2) && avatar2 != bd1) ||
                        (avatar2.getSensorName().equals(fd1) && avatar2 != bd2))) {
                    avatar2.setGrounded(true);
                    sensorFixtures.add(avatar2 == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

//            if ((bd1 == enemy || bd2 == enemy) && bd1 != avatar2 && bd2 !=avatar2 && bd1 != avatar && bd2 !=avatar) {
//                curPlanetEN = (bd1 == enemy) ? bd2 : bd1;
//                if (curPlanetEN.getName().contains("planet")) {
//                    contactPointEN.set(contact.getWorldManifold().getPoints()[0].cpy());
//                    enemy.setOnPlanet(true);
//                }
//                // See if we have landed on the ground.
//                if ((enemy.getSensorName().equals(fd2) && avatar != bd1 && avatar2 != bd1) ||
//                        (enemy.getSensorName().equals(fd1) && avatar != bd2 && avatar2 != bd2)) {
//                    enemy.setGrounded(true);
//                    enemy.setOnPlanet(true);
//                    contactPointEN.set(enemy.getPosition());
//                    sensorFixtures.add(enemy == bd1 ? fix2 : fix1); // Could have more than one ground
//                }
//
//            }

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

            if (bd1.getType() == ObstacleType.OCTO_LEG || bd2.getType() == ObstacleType.OCTO_LEG) {
                contact.setEnabled(false);
            }

            if (bd1.getType() == ObstacleType.AZTEC_WHEEL || bd2.getType() == ObstacleType.AZTEC_WHEEL) {
                contact.setEnabled(false);
            }

            if ((bd1 == avatar2 && bd2N.contains("cbugblue")) || (bd2 == avatar2 && bd1N.contains("cbugblue"))) {
                contact.setEnabled(false);
            }

            if ((bd1 == avatar && bd2N.contains("cbugpink")) || (bd2 == avatar && bd1N.contains("cbugpink"))) {
                contact.setEnabled(false);
            }

            //Disable all collisions with portal
            if (bd1.getType() == ObstacleType.PORTAL || bd2.getType() == ObstacleType.PORTAL) {
                contact.setEnabled(false);
            }


            //Disable all collisions with talking boss
            if (bd1.getType() == ObstacleType.TALKING_BOSS || bd2.getType() == ObstacleType.TALKING_BOSS) {
                contact.setEnabled(false);
            }
            //disable collisions with astronauts and buggy
            if ((bd1.getType() ==  ObstacleType.BUG || bd2.getType() ==  ObstacleType.BUG)
                    && (bd1.getName().contains("avatar") || bd2.getName().contains("avatar"))) {
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

            //If astronaut 1 hits an anchor
            if (bd1 == avatar && bd2.getType() == ObstacleType.ANCHOR) {
                avatar.setAnchorHit(true);
                avatar.setCurAnchor((Anchor)bd2);
            }
            else if (bd1.getType() == ObstacleType.ANCHOR && bd2 == avatar) {
                avatar.setAnchorHit(true);
                avatar.setCurAnchor((Anchor)bd1);
            }
//            else {
//                avatar.setAnchorHit(false);
//            }
            //If astronaut 2 hits an anchor
            if (bd1 == avatar2 && bd2.getType() == ObstacleType.ANCHOR) {
                avatar2.setAnchorHit(true);
                avatar2.setCurAnchor((Anchor)bd2);
            }
            else if (bd1.getType() == ObstacleType.ANCHOR && bd2 == avatar2) {
                avatar2.setAnchorHit(true);
                avatar2.setCurAnchor((Anchor)bd1);
            }
//            else {
//                avatar2.setAnchorHit(false);
//            }

            //If there is an active task
            if (tutorial) {
                if (bd1 == avatar && bd2 == tutPointCache.getPinkPoint() || bd2 == avatar && bd1 == tutPointCache.getPinkPoint()) {
                    tutPointCache.setPinkHit(true);
                }
                if (bd1 == avatar2 && bd2 == tutPointCache.getBluePoint() || bd2 == avatar2 && bd1 == tutPointCache.getBluePoint()) {
                    tutPointCache.setBlueHit(true);
                }
                tutPointCache.setComplete(tutPointCache.pinkHit() && tutPointCache.blueHit());
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

        if (count >= 0 && collectCount >= 0) {
            count --;
            collectCount--;
            return;
        }

        if (collectCount >=0) {
            collectCount--;
        }

        OrthographicCamera cam = (OrthographicCamera)canvas.getCamera();

        canvas.clear();
        level.draw(canvas);

        canvas.begin();
        drawStarBar(canvas);
        OrthographicCamera camera = (OrthographicCamera) canvas.getCamera();
        if (tutDrawCache != null) {
            TextureRegion text = tutDrawCache.getTask();
            float xPos = camera.position.x - (float) text.getRegionWidth() / 2;
            float yPos = camera.position.y - camera.viewportHeight / 2 + (float) text.getRegionHeight()*0.15f;
            canvas.draw(text, Color.WHITE, 0, 0, xPos, yPos, 0, 1, 1);
        }
        if (tutorial) {
            if (!tutPointCache.pinkHit()) {
                tutPointCache.getPinkPoint().draw(canvas);
            }
            if (!tutPointCache.blueHit()) {
                tutPointCache.getBluePoint().draw(canvas);
            }
        }
        canvas.end();

        //Start death anim
        if (isFailure() && deathPos.x == -death.getWidth()) {
            deathPos.x += (float) death.getWidth()/ (EXIT_COUNT);
        }

        //Death phase 3: Move off screen if animation is done
        if (deathAnimLoop >= MAX_ANIM && !isFailure()) {
            canvas.begin(); // DO NOT SCALE
            //print(deathPos.x + cam.position.x - canvas.getWidth()/2 + death.getWidth()/2);
            Color drawColor = new Color(1,1,1, 1);
            canvas.draw(death, drawColor, deathPos.x + cam.position.x - (float) canvas.getWidth()/2,
                    deathPos.y + cam.position.y - (float) canvas.getHeight()/2, death.getWidth(), death.getHeight());
            canvas.draw(deathSprite, Color.WHITE,(float) deathSprite.getRegionWidth()/2,(float) deathSprite.getRegionHeight()/2,
                    deathPos.x + cam.position.x - (float) canvas.getWidth()/2 + (float) death.getWidth()/2,
                    (deathPos.y + cam.position.y ),0,1,1.0f);;
            canvas.end();
            deathPos.x += (float) death.getWidth()/ (EXIT_COUNT);

            if (deathPos.x > canvas.getWidth()/15) justDead = false;
            if (deathPos.x > canvas.getWidth()) {
                deathAnimLoop = 0;
                deathPos.x = -death.getWidth(); //Reset
            }
        }

        //print(animLoop);

        //Death phase 2: once death screen is in place, animate
        if ((deathPos.x + death.getWidth()/2) >= canvas.getWidth()/2 && (deathAnimLoop < MAX_ANIM || isFailure())) {
            canvas.begin(); // DO NOT SCALE
            Color drawColor = new Color(1,1,1, 1);
            canvas.draw(death, drawColor, deathPos.x + cam.position.x - (float) canvas.getWidth()/2,
                    deathPos.y + cam.position.y - (float) canvas.getHeight()/2, death.getWidth(), death.getHeight());
            canvas.draw(deathSprite, Color.WHITE,(float) deathSprite.getRegionWidth()/2,(float) deathSprite.getRegionHeight()/2,
                    deathPos.x + cam.position.x - (float) canvas.getWidth()/2 + (float) death.getWidth()/2,
                    (deathPos.y + cam.position.y ),0,1,1.0f);
            canvas.end();
            deathSprite.tick();
            if (deathSprite.justReset()) deathAnimLoop++;
        }

        //Death phase 1: Move death screen into place
        if (deathPos.x != -death.getWidth() && (deathPos.x + death.getWidth()/2) < canvas.getWidth()/2) {
            canvas.begin(); // DO NOT SCALE
            //print(deathPos.x + cam.position.x - canvas.getWidth()/2 + death.getWidth()/2);
            Color drawColor = new Color(1,1,1, 1);
            canvas.draw(death, drawColor, deathPos.x + cam.position.x - (float) canvas.getWidth()/2,
                    deathPos.y + cam.position.y - (float) canvas.getHeight()/2, death.getWidth(), death.getHeight());
            canvas.draw(deathSprite, Color.WHITE,(float) deathSprite.getRegionWidth()/2,(float) deathSprite.getRegionHeight()/2,
                    deathPos.x + cam.position.x - (float) canvas.getWidth()/2 + (float) death.getWidth()/2,
                    (deathPos.y + cam.position.y ),0,1,1.0f);;
            canvas.end();
            deathPos.x += (float) death.getWidth()/ (EXIT_COUNT);
        }

        //Start win anim
        if (isComplete() && winPos.x == -death.getWidth()){
            winPos.x += (float) death.getWidth()/ (EXIT_COUNT);
        }

        //WIn phase 1: move win screen into place
        if (winPos.x != -death.getWidth() && (winPos.x + death.getWidth()/2) < canvas.getWidth()/2) {
            canvas.begin(); // DO NOT SCALE
            //print(deathPos.x + cam.position.x - canvas.getWidth()/2 + death.getWidth()/2);
            Color drawColor = new Color(1,1,1, 1);
            canvas.draw(death, drawColor, winPos.x + cam.position.x - (float) canvas.getWidth()/2,
                    winPos.y + cam.position.y - (float) canvas.getHeight()/2, death.getWidth(), death.getHeight());
            canvas.draw(winSprite, Color.WHITE,(float) winSprite.getRegionWidth()/2,(float) winSprite.getRegionHeight()/2,
                    winPos.x + cam.position.x - (float) canvas.getWidth()/2 + (float) death.getWidth()/2,
                    (winPos.y + cam.position.y + 3 * (float) canvas.getHeight() / 4 - (float) canvas.getHeight()/2),0,1,1.0f);;
            canvas.end();
            winPos.x += (float) death.getWidth()/ (EXIT_COUNT);
        }

        //Win phase 2: Animate (will be cut off by screen swtich)
        if ((winPos.x + death.getWidth()/2) >= canvas.getWidth()/2 && winAnimLoop < MAX_ANIM) {
            canvas.begin(); // DO NOT SCALE
            Color drawColor = new Color(1,1,1, 1);
            canvas.draw(death, drawColor, winPos.x + cam.position.x - (float) canvas.getWidth()/2,
                    winPos.y + cam.position.y - (float) canvas.getHeight()/2, death.getWidth(), death.getHeight());
            canvas.draw(winSprite, Color.WHITE,(float) winSprite.getRegionWidth()/2,(float) winSprite.getRegionHeight()/2,
                    winPos.x + cam.position.x - (float) canvas.getWidth()/2 + (float) death.getWidth()/2,
                    (winPos.y + cam.position.y + 3 * (float) canvas.getHeight() / 4 - (float) canvas.getHeight()/2 ),0,1,1.0f);
            canvas.end();
            winSprite.tick();
            if (winSprite.justReset()) winAnimLoop++;
        }

        // Win phase 3: move off screen (if started from winning)
        if (winAnimLoop >= MAX_ANIM) {
            canvas.begin(); // DO NOT SCALE
            //print(deathPos.x + cam.position.x - canvas.getWidth()/2 + death.getWidth()/2);
            Color drawColor = new Color(1,1,1, 1);
            canvas.draw(death, drawColor, winPos.x,
                    winPos.y + cam.position.y - (float) canvas.getHeight()/2, death.getWidth(), death.getHeight());
            canvas.draw(winSprite, Color.WHITE,(float) winSprite.getRegionWidth()/2,(float) winSprite.getRegionHeight()/2,
                    winPos.x + (float) death.getWidth()/2,
                    (winPos.y + 3 * (float) canvas.getHeight() / 4 ) + cam.position.y - (float) canvas.getHeight()/2,0,1,1.0f);;
            canvas.end();
            winPos.x += (float) death.getWidth()/ (EXIT_COUNT);

            if (winPos.x > canvas.getWidth()) {
                winAnimLoop = 0;
                replaying = false;
                winPos = new Vector2(-death.getWidth(), 0);
            }
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