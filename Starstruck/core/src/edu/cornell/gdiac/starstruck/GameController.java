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

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import java.util.*;

//import edu.cornell.gdiac.physics.*;
import com.sun.org.apache.bcel.internal.generic.LAND;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.starstruck.Gravity.*;
import edu.cornell.gdiac.util.FilmStrip;

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
    /** The texture file for the background*/
    private static final String BACKGROUND_FILE = "platform/background.png";
    /** The texture file for the character avatar (no animation) */
    private static final String DUDE1_FILE  = "platform/astronaut1.png";
    private static final String DUDE2_FILE  = "platform/astronaut2.png";
    /** The texture file for the spinning barrier */
    private static final String BARRIER_FILE = "platform/barrier.png";
    /** The texture file for the bullet */
    private static final String BULLET_FILE  = "platform/bullet.png";
    /** The texture file for the bridge plank */
    private static final String ROPE_FILE  = "platform/ropebridge.png";
    /** The texture file for the Star*/
    private static final String STAR_FILE = "platform/star.png";
    /** The texture file for indicating active astronaut */
    private static final String ACTIVE_FILE = "platform/static_glow_v1.png";
    /** The texture file for the Enemy*/
    private static final String ENEMY_FILE = "platform/enemy.png";
    /** The texture file for the Enemy*/
    private static final String PINKWORM_FILE = "platform/pink_worm.png";
    /** The texture file for the Enemy*/
    private static final String GREENWORM_FILE = "platform/green_worm.png";

    /** The sound file for a jump */
    private static final String JUMP_FILE = "audio/jump/jump8.mp3";
    /** The sound file for a landing */
    private static final String LAND_FILE = "audio/jump/quick_land.mp3";
    /** The sound file for a collision */
    private static final String COLLISION_FILE = "audio/anchor.mp3";
    /** The sound file for a character switch */
    private static final String SWITCH_FILE = "audio/collecting stars/star_collect.mp3";
    /** The sound file to anchor */
    private static final String ANCHOR_FILE = "audio/anchor.mp3";
    /** Space sounds */
    private static final String SPACE_SOUNDS = "audio/sounds from space/VanB-2017-04-05-2229.mp3";


    /** Background texture for start-up */
    private Texture background;
    /** Background texture region for tiling */
    private TextureRegion backgroundTR;
    /** Texture asset for character avatar */
    private TextureRegion avatar1Texture;
    /** Texture asset for second character avatar */
    private TextureRegion avatar2Texture;
    /** Texture asset for the spinning barrier */
    private TextureRegion barrierTexture;
    /** Texture asset for the bullet */
    private TextureRegion bulletTexture;
    /** Texture asset for the bridge plank */
    private TextureRegion bridgeTexture;
    /** Texture asset for the star */
    private TextureRegion starTexture;
    /** Texture asset for the active glow */
    private TextureRegion activeTexture;
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

        background = new Texture(BACKGROUND_FILE);

        manager.load(DUDE1_FILE, Texture.class);
        assets.add(DUDE1_FILE);
        manager.load(DUDE2_FILE, Texture.class);
        assets.add(DUDE2_FILE);
        manager.load(BARRIER_FILE, Texture.class);
        assets.add(BARRIER_FILE);
        manager.load(BULLET_FILE, Texture.class);
        assets.add(BULLET_FILE);
        manager.load(ROPE_FILE, Texture.class);
        assets.add(ROPE_FILE);
        manager.load(STAR_FILE, Texture.class);
        assets.add(STAR_FILE);
        manager.load(ACTIVE_FILE, Texture.class);
        assets.add(ACTIVE_FILE);
        manager.load(ENEMY_FILE, Texture.class);
        assets.add(ENEMY_FILE);
        manager.load(PINKWORM_FILE, Texture.class);
        assets.add(PINKWORM_FILE);
        manager.load(GREENWORM_FILE, Texture.class);
        assets.add(GREENWORM_FILE);


        manager.load(JUMP_FILE, Sound.class);
        assets.add(JUMP_FILE);
        manager.load(LAND_FILE, Sound.class);
        assets.add(LAND_FILE);
        manager.load(COLLISION_FILE, Sound.class);
        assets.add(COLLISION_FILE);
        manager.load(SWITCH_FILE, Sound.class);
        assets.add(SWITCH_FILE);
        manager.load(ANCHOR_FILE, Sound.class);
        assets.add(ANCHOR_FILE);
        manager.load(SPACE_SOUNDS, Sound.class);
        assets.add(SPACE_SOUNDS);

        super.preLoadContent(manager);
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

        avatar1Texture = createTexture(manager,DUDE1_FILE,false);
        avatar2Texture = createTexture(manager,DUDE2_FILE,false);
        barrierTexture = createTexture(manager,BARRIER_FILE,false);
        bulletTexture = createTexture(manager,BULLET_FILE,false);
        bridgeTexture = createTexture(manager,ROPE_FILE,false);
        starTexture = createTexture(manager, STAR_FILE, false);
        activeTexture = createTexture(manager, ACTIVE_FILE, false);
        enemyTexture = createFilmStrip(manager, ENEMY_FILE, 1,3,3);
        pinkwormTexture = createFilmStrip(manager, PINKWORM_FILE, 1,14,14);
        greenwormTexture = createFilmStrip(manager, GREENWORM_FILE, 1,14,14);

        // TODO sound
        SoundController sounds = SoundController.getInstance();
        sounds.allocate(manager, JUMP_FILE);
        sounds.allocate(manager, LAND_FILE);
        sounds.allocate(manager, COLLISION_FILE);
        sounds.allocate(manager, SWITCH_FILE);
        sounds.allocate(manager, ANCHOR_FILE);
        sounds.allocate(manager, SPACE_SOUNDS);


        super.loadContent(manager);
        platformAssetState = AssetState.COMPLETE;

        planets = new PlanetList(manager, galaxy, scale);
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


    // Location, radius, and drawscale of all the planets.
    // Each row is a planet. 1st col is x, 2nd is y, 3rd is radius, 4th is mass, 6th is sprite to use and
    // 5th is gravitational pull range.
    // Force setting mass is temporary fix -- in future add dynmaic planet to pin and fix rotation?
    // Better solution for drawing?
    private static final float[][] PLANETS = {
            {1f, 1f, 4f, 2500f, 4, 0},
            {6f, 12f, 2.5f, 3700f, 2, 0.8f},
            {15f, 17f, 3f, 4000f, 2.5f, 1.7f},
            {26f, 8f, 3f, 2000f, 3, 2f},
            {30f, 15f, 1.5f, 3000f, 2, 2.7f},
            {37f, 5f, 3f, 2000f, 2, 3.5f},
            {48f, 17f, 3.5f, 2500f, 3, 5},
            {50f, 25f, 1f, 4000f, 1, 3},
            {52f, 6f, 2.5f, 4000f, 2, 3},
    };

    // Location of each star (TODO add more fields later, SHOULD MAKE INTO A CLASS)
    private static final float[][] STARS = {
            {35f, 15.75f},
            {35.5f, 16.5f},
            {35.25f, 15f},
            {16f, 4f},
            {17f, 3f},
            {16.5f, 2.5f},
//            {5f, 14f},
//            {6f, 14f},
//            {5.5f, 13f},
    };

    // Location of anchor points (TODO add more fields later, SHOULD MAKE INTO A CLASS)
    private static final float[][] ANCHORS = {
            {33.5f, 17f},
            {37f, 18f},
            {35.5f, 13.5f},
            {14f, 2.75f},
            {18.5f, 3f},
            {16f, 5f},
            {17f, 1f},
//            {7f, 15f},
//            {3f, 16f},
//            {4f, 11f},
    };


    // Other game objects
    /** The goal door position */
    private static Vector2 GOAL_POS = new Vector2(4.0f,14.0f);
    /** The position of the spinning barrier */
    private static Vector2 SPIN_POS = new Vector2(13.0f,12.5f);
    /** The initial position of the dude */
    private static Vector2 DUDE_POS = new Vector2(2.5f, 5.0f);
    /** The initial position of the second dude */
    private static Vector2 DUDE2_POS = new Vector2(3.5f, 6.5f);
    /** Variable caches used for setting position on planet for avatar 1*/
    private Obstacle curPlanet;
    //private Vector2 contactPoint = new Vector2(); //Does not appear to be necessary anymore?
    private Vector2 lastPoint = new Vector2();

    /** Variable caches for setting position on planet for avatar 2 */
    private Obstacle curPlanet2;
    //private Vector2 contactPoint2 = new Vector2(); //Does not appear to be necessary anymore?
    private Vector2 lastPoint2 = new Vector2();

    /** Variable caches used for setting position on planet for enemy*/
    private Obstacle curPlanetEN;
    private Vector2 contactPointEN = new Vector2();
    private Vector2 contactDirEn = new Vector2();

    /** Cache for the direction of jump */
    private Vector2 contactDir = new Vector2();
    private Vector2 contactDir2 = new Vector2();

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
        Vector2 gravity = new Vector2(world.getGravity() );

        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        for(Planet p : planets.getPlanets()){
            p.deactivatePhysics(world);
        }
        objects.clear();
        planets.clear();
        stars.clear();
        anchors.clear();
        addQueue.clear();
        world.dispose();

        vectorWorld = new VectorWorld();
        world = new World(new Vector2(0,0), false);
        world.setContactListener(this);
        setComplete(false);
        setFailure(false);
        populateLevel();
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth;
        float dheight;

        // Create dude
        dwidth  = avatar1Texture.getRegionWidth()/scale.x;
        dheight = avatar1Texture.getRegionHeight()/scale.y;
        avatar = new AstronautModel(DUDE_POS.x, DUDE_POS.y, dwidth, dheight, true, true);
        avatar.setDrawScale(scale);
        avatar.setTexture(avatar1Texture);
        avatar.setGlow(activeTexture);
        avatar.setName("avatar");

        //avatar.setAngle((float)Math.PI/2);
        addObject(avatar);

        avatar2 = new AstronautModel(DUDE2_POS.x + 1, DUDE2_POS.y, dwidth, dheight, false, false);
        avatar2.setDrawScale(scale);
        avatar2.setTexture(avatar2Texture);
        avatar2.setGlow(activeTexture);
        avatar2.setName("avatar2");
        addObject(avatar2);

        objects.remove(avatar); objects.remove(avatar2);

        // Create rope
        dwidth  = bridgeTexture.getRegionWidth()/scale.x;
        dheight = bridgeTexture.getRegionHeight()/scale.y;
        rope = new Rope(avatar.getX() + 0.5f, avatar.getY() + 0.5f, BRIDGE_WIDTH, dwidth, dheight, avatar, avatar2);
        rope.setTexture(bridgeTexture);
        rope.setDrawScale(scale);
        rope.setName("rope");
        addObject(rope);

        objects.add(avatar); objects.add(avatar2);

        planets.addPlanets(PLANETS, world, vectorWorld);

        // Create star
        dwidth  = starTexture.getRegionWidth()/scale.x;
        dheight = starTexture.getRegionHeight()/scale.y;
        String sname = "star";
        for (int ii = 0; ii < STARS.length; ii++) {
            Star star = new Star(STARS[ii][0],STARS[ii][1],dwidth,dheight);
            star.setName(sname + ii);
            star.setDensity(0f);
            star.setBodyType(BodyDef.BodyType.StaticBody);
            star.setDrawScale(scale);
            star.setTexture(starTexture);
            stars.add(star);
            addObject(star);
        }

        //add anchor
        dwidth = bulletTexture.getRegionWidth()/scale.x;
        dheight = bulletTexture.getRegionHeight()/scale.y;
        String aname = "anchor";
        for (int ii = 0; ii < ANCHORS.length; ii++){
            Anchor anchor = new Anchor(ANCHORS[ii][0],ANCHORS[ii][1],dwidth,dheight);
            anchor.setName(aname + ii);
            anchor.setBodyType(BodyDef.BodyType.StaticBody);
            anchor.setDensity(0f);
            anchor.setDrawScale(scale);
            anchor.setTexture(bulletTexture);
            anchors.add(anchor);
            addObject(anchor);
        }

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
     * Was spaced pressed
     *
     * @return true if space was pressed
     */
    private boolean spaced() {
        return InputController.getInstance().didSpace();
    }

    /**
     * Was shift pressed
     *
     * @return true if shift was pressed
     */
    private boolean shifted() {
        return InputController.getInstance().didShift();
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
        if (!avatar1.isAnchored() && !avatar2.isAnchored() && spaced()) {
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
            if (spaced() && !avatar2.getOnPlanet()) { //If space was hit and avatar2 is not on planet -- couldb e anchored
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
            if (spaced() && !avatar1.getOnPlanet()) { //If space was hit and avatar1 is not on planet -- could be anchored
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

        if (InputController.getInstance().didPrimary() && !auto) {
            //print(contactPoint);
            avatar.setJumping(true);
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
                    avatar2.setGravity(reset);
                    avatar2.setLinearVelocity(reset);
                    avatar1.setLinearVelocity(reset);
                    return true;
                }
            }
            // If avatar2 is anchored
            else if ((avatar2.isAnchored()) && !avatar1.getOnPlanet()) { //avatar2.getOnPlanet() ||
                if (dist >= length) {
                    avatar1.setGravity(reset);
                    avatar1.setLinearVelocity(reset);
                    avatar2.setLinearVelocity(reset);
                    return true;
                }
            }
        }

        //avatar1 is active, avatar2 is not
        else if (mode == 'c') {
            float theta = 2 * (float) Math.asin((dist/2) / ((Planet)curPlanet).getRadius());
            Float arc = (float) (2 * Math.PI * ((Planet)curPlanet).getRadius() * (theta / (2*Math.PI)));
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
     * @param contactDir Direction of active avatar
     * @param contactDir2 Direction of inactive avatar
     * @param lastPos Last position of the active avatar
     * @param lastPos2 last position of ianctive avatar
     */
    private void updateHelp(AstronautModel avatar, AstronautModel avatar2, Vector2 contactDir, Vector2 contactDir2,
                            Planet curPlanet, Planet curPlanet2, Vector2 lastPos, Vector2 lastPos2) {
        avatar.setGravity(vectorWorld.getForce(avatar.getPosition())); //gravity will be applied no matter what
        avatar2.setGravity(vectorWorld.getForce(avatar2.getPosition()));
        float angle;
        if (avatar.getOnPlanet()) { //If avatar is on the planet update control movement
            avatar.setFixedRotation(true);
            contactDir.set(avatar.getPosition().cpy().sub(curPlanet.getPosition()));
            angle = -contactDir.angleRad(new Vector2 (0, 1));
            avatar.setAngle(angle);
            updateMovement(avatar, contactDir, curPlanet, false);
            if (avatar2.getOnPlanet()) { //Inactive astronaut
                avatar2.setFixedRotation(true);
                contactDir2.set(avatar2.getPosition().cpy().sub(curPlanet2.getPosition()));
                angle = -contactDir2.angleRad(new Vector2 (0, 1));
                avatar2.setAngle(angle);
                if (curPlanet == curPlanet2) { //If the two avatars are on the same planet, move inactive avatar
                    if (updateRope(avatar, avatar2, rope, 'c')) {
                        updateMovement(avatar2, contactDir2, curPlanet2, true);
                    }
                }
                else { // Else if inactive is on a different planet, set it's location, restrict mvoement of other avatar
                    avatar2.setPosition(lastPos2);
                    if (updateRope(avatar, avatar2, rope, 'p')) {
                        avatar.setPosition(lastPos);
                    }
                }
            }
        }
        else {
            avatar.setRotation(InputController.getInstance().getHorizontal());
            if (avatar2.getOnPlanet()) { //If the inactive avatar is on planet
                avatar2.setFixedRotation(true);
                contactDir2.set(avatar2.getPosition().cpy().sub(curPlanet2.getPosition()));
                angle = -contactDir2.angleRad(new Vector2 (0, 1));
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

        if (avatar.isActive())
            updateHelp(avatar, avatar2, contactDir, contactDir2, (Planet)curPlanet, (Planet)curPlanet2, lastPoint, lastPoint2);
        else //if avatar2 is active
            updateHelp(avatar2, avatar, contactDir2, contactDir, (Planet)curPlanet2, (Planet)curPlanet, lastPoint2, lastPoint);

        if (avatar.isJumping() || avatar2.isJumping()) {
            avatar.setActive(!avatar.isActive());
            avatar2.setActive(!avatar2.isActive());
        }

        avatar.applyForce();
        avatar2.applyForce();



        enemy.update(dt);
        if (enemy.getOnPlanet()) {
            enemy.setFixedRotation(true);
            //enemy.setRotation(1);
            Vector2 contactDirEn = contactPointEN.cpy().sub(curPlanetEN.getPosition());
            float angle = -contactDirEn.angleRad(new Vector2(0, 1));
            enemy.setAngle(angle);
            enemy.setPosition(contactPointEN);
            contactDirEn.rotateRad(-(float) Math.PI / 2);
            enemy.setPosition(contactPointEN.add(contactDirEn.setLength(BUG_SPEED)));
            enemy.setGravity(vectorWorld.getForce(enemy.getPosition()));
            enemy.applyForce();
        }

        lastPoint.set(avatar.getPosition());
        lastPoint2.set(avatar2.getPosition());

        // Add a bullet if we fire
        if (avatar.isShooting()) {
            createBullet();
        }

        //TODO Removed sound stuffs
        if (avatar.isJumping() || avatar2.isJumping()) {
            SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
        }

//        avatar.setJumping(false);
//        avatar2.setJumping(false);

//         If we use sound, we must remember this.
        SoundController.getInstance().update();
    }

    /**
     * Add a new bullet to the world and send it in the right direction.
     */
    private void createBullet() {
        float offset = (avatar.isFacingRight() ? BULLET_OFFSET : -BULLET_OFFSET);
        float radius = bulletTexture.getRegionWidth()/(2.0f*scale.x);
        WheelObstacle bullet = new WheelObstacle(avatar.getX()+offset, avatar.getY(), radius);

        bullet.setName("bullet");
        bullet.setDensity(HEAVY_DENSITY);
        bullet.setDrawScale(scale);
        bullet.setTexture(bulletTexture);
        bullet.setBullet(true);
        bullet.setGravityScale(0);

        // Compute position and velocity
        float speed  = (avatar.isFacingRight() ? BULLET_SPEED : -BULLET_SPEED);
        bullet.setVX(speed);
        addQueuedObject(bullet);

//        SoundController.getInstance().play(PEW_FILE, PEW_FILE, false, EFFECT_VOLUME);
    }

    /**
     * Remove a new bullet from the world.
     *
     * @param  bullet   the bullet to remove
     */
    public void removeBullet(Obstacle bullet) {
        bullet.markRemoved(true);
//        SoundController.getInstance().play(POP_FILE,POP_FILE,false,EFFECT_VOLUME);
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
                curPlanet = (bd1 == avatar) ? bd2 : bd1;
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
                curPlanet2 = (bd1 == avatar2) ? bd2 : bd1;
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

            // Test bullet collision with world
            if (bd1.getName().equals("bullet") && bd2 != avatar) {
                removeBullet(bd1);
            }

            if (bd2.getName().equals("bullet") && bd1 != avatar) {
                removeBullet(bd2);
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

            //System.out.println(bd1.getName() + bd2.getName());

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

        // Draw background unscaled.
        canvas.begin();

        float x = (float) Math.floor((canvas.getCamera().position.x - canvas.getWidth()/2)/canvas.getWidth()) * canvas.getWidth();

        canvas.draw(background, Color.WHITE, x, 0,canvas.getWidth(),canvas.getHeight());
        canvas.draw(background, Color.WHITE, x + canvas.getWidth(), 0,canvas.getWidth(),canvas.getHeight());

        for(Planet p : planets.getPlanets()){
            p.draw(canvas);
        }
//        for (Anchor a : anchors) {
//            a.draw(canvas);
//        }
//        for (Star s : stars) {
//            s.draw(canvas);
//        }
        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }
        canvas.end();

        if (isDebug()) {
            canvas.beginDebug();
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
//            for(Anchor a : anchors) {
//                a.drawDebug(canvas);
//            }
//            for(Star s: stars) {
//                s.drawDebug(canvas);
//            }
            for(Planet p : planets.getPlanets()) {
                p.drawDebug(canvas);
            }
            canvas.endDebug();
        }

        if (isFailure()) {
            displayFont.setColor(Color.RED);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("u ded :(", displayFont, 0.0f);
            canvas.end();
        }
    }
}