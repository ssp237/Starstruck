/*
 * PlatformController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.planetdemo;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.*;
import java.util.*;

import edu.cornell.gdiac.util.*;
//import edu.cornell.gdiac.physics.*;
//import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class PlatformController extends WorldController implements ContactListener {
    /** The texture file for the background*/
    private static final String BACKGROUND_FILE = "platform/background.png";
    /** The texture file for the character avatar (no animation) */
    private static final String DUDE_FILE  = "platform/bloop.png";
    /** The texture file for the spinning barrier */
    private static final String BARRIER_FILE = "platform/barrier.png";
    /** The texture file for the bullet */
    private static final String BULLET_FILE  = "platform/bullet.png";
    /** The texture file for the bridge plank */
    private static final String ROPE_FILE  = "platform/ropebridge.png";
    /** The texture file for the Star*/
    private static final String STAR_FILE = "platform/star.png";

    /** The sound file for a jump */
    private static final String JUMP_FILE = "platform/jump.mp3";
    /** The sound file for a bullet fire */
    private static final String PEW_FILE = "platform/pew.mp3";
    /** The sound file for a bullet collision */
    private static final String POP_FILE = "platform/plop.mp3";

    /** Background texture for start-up */
    private Texture background;
    /** Texture asset for character avatar */
    private TextureRegion avatarTexture;
    /** Texture asset for the spinning barrier */
    private TextureRegion barrierTexture;
    /** Texture asset for the bullet */
    private TextureRegion bulletTexture;
    /** Texture asset for the bridge plank */
    private TextureRegion bridgeTexture;
    /** Texture asset for the star */
    private TextureRegion starTexture;

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /** Cache variable to store current planet being drawn*/
    private WheelObstacle planetCache;

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

        manager.load(DUDE_FILE, Texture.class);
        assets.add(DUDE_FILE);
        manager.load(BARRIER_FILE, Texture.class);
        assets.add(BARRIER_FILE);
        manager.load(BULLET_FILE, Texture.class);
        assets.add(BULLET_FILE);
        manager.load(ROPE_FILE, Texture.class);
        assets.add(ROPE_FILE);
        manager.load(STAR_FILE, Texture.class);
        assets.add(STAR_FILE);

        manager.load(JUMP_FILE, Sound.class);
        assets.add(JUMP_FILE);
        manager.load(PEW_FILE, Sound.class);
        assets.add(PEW_FILE);
        manager.load(POP_FILE, Sound.class);
        assets.add(POP_FILE);

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

        avatarTexture = createTexture(manager,DUDE_FILE,false);
        barrierTexture = createTexture(manager,BARRIER_FILE,false);
        bulletTexture = createTexture(manager,BULLET_FILE,false);
        bridgeTexture = createTexture(manager,ROPE_FILE,false);
        starTexture = createTexture(manager, STAR_FILE, false);

        //TODO Sound stuffs
//        SoundController sounds = SoundController.getInstance();
//        sounds.allocate(manager, JUMP_FILE);
//        sounds.allocate(manager, PEW_FILE);
//        sounds.allocate(manager, POP_FILE);
        super.loadContent(manager);
        platformAssetState = AssetState.COMPLETE;
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
    private static final float  BRIDGE_WIDTH = 14.0f;
    /** Offset for bullet when firing */
    private static final float  BULLET_OFFSET = 0.2f;
    /** The speed of the bullet after firing */
    private static final float  BULLET_SPEED = 20.0f;
    /** The volume for sound effects */
    private static final float EFFECT_VOLUME = 0.8f;
    /** The distance from an anchor at which an astronaut will be able to anchor */
    private static float ANCHOR_DIST = 1f;

    // Since these appear only once, we do not care about the magic numbers.
    // In an actual game, this information would go in a data file.
    // Wall vertices
    private static final float[][] WALLS = {
            {16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
                    1.0f,  0.0f,  0.0f,  0.0f,  0.0f, 18.0f},
            {32.0f, 18.0f, 32.0f,  0.0f, 31.0f,  0.0f,
                    31.0f, 17.0f, 16.0f, 17.0f, 16.0f, 18.0f}
    };

    /** The outlines of all of the platforms */
    private static final float[][] PLATFORMS = {
            { 1.0f, 3.0f, 6.0f, 3.0f, 6.0f, 2.5f, 1.0f, 2.5f},
            { 6.0f, 4.0f, 9.0f, 4.0f, 9.0f, 2.5f, 6.0f, 2.5f},
            {23.0f, 4.0f,31.0f, 4.0f,31.0f, 2.5f,23.0f, 2.5f},
            {26.0f, 5.5f,28.0f, 5.5f,28.0f, 5.0f,26.0f, 5.0f},
            {29.0f, 7.0f,31.0f, 7.0f,31.0f, 6.5f,29.0f, 6.5f},
            {24.0f, 8.5f,27.0f, 8.5f,27.0f, 8.0f,24.0f, 8.0f},
            {29.0f,10.0f,31.0f,10.0f,31.0f, 9.5f,29.0f, 9.5f},
            {23.0f,11.5f,27.0f,11.5f,27.0f,11.0f,23.0f,11.0f},
            {19.0f,12.5f,23.0f,12.5f,23.0f,12.0f,19.0f,12.0f},
            { 1.0f,12.5f, 7.0f,12.5f, 7.0f,12.0f, 1.0f,12.0f}
    };

    // Location, radius, and drawscale of all the planets.
    // Each row is a planet. 1st col is x, 2nd is y, 3rd is radius, 4th is mass, 5th is scale for drawing.
    // Force setting mass is temporary fix -- in future add dynmaic planet to pin and fix rotation?
    // Better solution for drawing?
    private static final float[][] PLANETS = {
            {-7f, -7f, 14.1f, 57000f, 1.99f},
            {13f, 15f, 4f, 6000f, 0.43f},
            {30f, 5f, 4f, 6000f, 0.81f},
            {25f, 15f, 3f, 2500f, 0.43f},
            {18f, 0f, 3f, 2500f, 0.43f},
    };

    // Location of each star (can add more fields later, SHOULD MAKE INTO A CLASS)
    private static final float[][] STARS = {
            {5f, 14f},
            {6f, 14f},
            {5.5f, 13f},
    };

    // Location of anchor points (can add more fields later, SHOULD MAKE INTO A CLASS)
    private static final float[][] ANCHORS = {
            {7f, 15f},
            {3f, 16f},
            {4f, 11f},
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
    /** The position of the rope bridge */
    private static Vector2 BRIDGE_POS  = new Vector2(9.0f, 3.8f);
    /** Temporary vatrables used for setting position on planet */
    private Obstacle curPlanet;
    private Vector2 contactPoint = new Vector2();

    /** Temprary obstacle used for setting position on planet for avatar 2*/
    private Obstacle curPlanet2;
    private Vector2 contactPoint2 = new Vector2();

    // Physics objects for the game
    /** Reference to the character avatar */
    private DudeModel avatar;
    /** Reference to the second character avatar*/
    private DudeModel avatar2;
    /** List of anchors, temporary quick solution */
    private ArrayList<Spinner> anchors = new ArrayList<Spinner>();
    /** WHY GRAVITY */
    private ArrayList<Spinner> stars = new ArrayList<Spinner>();

    /** Reference to the goalDoor (for collision detection) */
//    private BoxObstacle goalDoor;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * Return a reference to the primary avatar
     * @return Return a reference to the primary avatar.
     */
    public DudeModel getAvatar() {return avatar;}

    /**
     * Return a reference to the secondary avatar.
     * @return Return a reference to the secondary avatar.
     */
    public DudeModel getAvatar2() {return avatar2;}

    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public PlatformController() {
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
        objects.clear();
        addQueue.clear();
        world.dispose();

        vectorWorld = new VectorWorld(bounds);
        world = vectorWorld.getWorld();
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
        float dwidth;//  = goalTile.getRegionWidth()/scale.x;
        float dheight;// = goalTile.getRegionHeight()/scale.y;
//        goalDoor = new BoxObstacle(GOAL_POS.x,GOAL_POS.y,dwidth,dheight);
//        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
//        goalDoor.setDensity(0.0f);
//        goalDoor.setFriction(0.0f);
//        goalDoor.setRestitution(0.0f);
//        goalDoor.setSensor(true);
//        goalDoor.setDrawScale(scale);
//        goalDoor.setTexture(goalTile);
//        goalDoor.setName("goal");
//        addObject(goalDoor);

//        String wname = "wall";
//        for (int ii = 0; ii < WALLS.length; ii++) {
//            PolygonObstacle obj;
//            obj = new PolygonObstacle(WALLS[ii], 0, 0);
//            obj.setBodyType(BodyDef.BodyType.StaticBody);
//            obj.setDensity(BASIC_DENSITY);
//            obj.setFriction(BASIC_FRICTION);
//            obj.setRestitution(BASIC_RESTITUTION);
//            obj.setDrawScale(scale);
//            obj.setTexture(earthTile);
//            obj.setName(wname+ii);
//            addObject(obj);
//        }

//        String pname = "platform";
//        for (int ii = 0; ii < PLATFORMS.length; ii++) {
//            PolygonObstacle obj;
//            obj = new PolygonObstacle(PLATFORMS[ii], 0, 0);
//            obj.setBodyType(BodyDef.BodyType.StaticBody);
//            obj.setDensity(BASIC_DENSITY);
//            obj.setFriction(BASIC_FRICTION);
//            obj.setRestitution(BASIC_RESTITUTION);
//            obj.setDrawScale(scale);
//            obj.setTexture(earthTile);
//            obj.setName(pname+ii);
//            addObject(obj);
//        }

        String ptname = "planet";
        for (int i = 0; i < PLANETS.length; i++) {
            WheelObstacle obj = new WheelObstacle(PLANETS[i][0], PLANETS[i][1], PLANETS[i][2]);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(500f);
            obj.setFriction(BASIC_FRICTION);
            obj.setRestitution(BASIC_RESTITUTION);
            obj.setDrawScale(scale);
            int numPlanet = i % 4;
            switch(numPlanet) {
                case 0:
                    obj.setTexture(planet1);
                    break;
                case 1:
                    obj.setTexture(planet2);
                    break;
                case 2:
                    obj.setTexture(planet3);
                    break;
                case 3:
                    obj.setTexture(planet4);
                    break;
                default:
                    obj.setTexture(planet1);
                    break;
            }
            obj.setName(ptname+i);
            obj.scaleDraw = PLANETS[i][4];
            addObject(obj);
            //Vector2 pos = new Vector2(obj.getBody().getPosition().x, obj.getBody().getPosition().y - obj.getRadius());
            //vectorWorld.addPlanet(obj, PLANETS[i][3], obj.getCenter()); //Radius parameter is temporary fix for why center is off
             vectorWorld.addPlanet(obj, PLANETS[i][3]);
        }

        // Create dude
        dwidth  = avatarTexture.getRegionWidth()/scale.x;
        dheight = avatarTexture.getRegionHeight()/scale.y;
        avatar = new DudeModel(DUDE_POS.x, DUDE_POS.y, dwidth, dheight);
        avatar.setDrawScale(scale);
        avatar.setTexture(avatarTexture);
        //avatar.setAngle((float)Math.PI/2);
        addObject(avatar);

        avatar2 = new DudeModel(DUDE2_POS.x + 1, DUDE2_POS.y, dwidth, dheight);
        avatar2.setDrawScale(scale);
        avatar2.setTexture(avatarTexture);
        addObject(avatar2);

//        // Create rope bridge
//        dwidth  = bridgeTexture.getRegionWidth()/scale.x;
//        dheight = bridgeTexture.getRegionHeight()/scale.y;
//        RopeBridge bridge = new RopeBridge(avatar.getX() + 0.5f, avatar.getY() + 0.5f, BRIDGE_WIDTH, dwidth, dheight, avatar, avatar2);
//        bridge.setTexture(bridgeTexture);
//        bridge.setDrawScale(scale);
//        addObject(bridge);

        // Create spinning platform
//        dwidth  = barrierTexture.getRegionWidth()/scale.x;
//        dheight = barrierTexture.getRegionHeight()/scale.y;
//        Spinner spinPlatform = new Spinner(SPIN_POS.x,SPIN_POS.y,dwidth,dheight);
//        spinPlatform.setDrawScale(scale);
//        spinPlatform.setTexture(barrierTexture);
//        addObject(spinPlatform);

        // Create star


        dwidth  = starTexture.getRegionWidth()/scale.x;
        dheight = starTexture.getRegionHeight()/scale.y;
        String sname = "star";
        for (int ii = 0; ii < STARS.length; ii++) {
            Spinner star = new Spinner(STARS[ii][0],STARS[ii][1],dwidth,dheight);
            star.setName(sname + ii);
            star.setDensity(0f);
            star.setBodyType(BodyDef.BodyType.StaticBody);
            star.setDrawScale(scale);
            star.setTexture(starTexture);
            stars.add(star);
            //addObject(star);
        }

        //add anchor
        dwidth = bulletTexture.getRegionWidth()/scale.x;
        dheight = bulletTexture.getRegionHeight()/scale.y;
        String aname = "anchor";
        for (int ii = 0; ii < ANCHORS.length; ii++){
            Spinner anchor = new Spinner(ANCHORS[ii][0],ANCHORS[ii][1],dwidth,dheight);
            anchor.setName(aname + ii);
            anchor.setBodyType(BodyDef.BodyType.StaticBody);
            anchor.setDensity(0f);
            anchor.setDrawScale(scale);
            anchor.setTexture(bulletTexture);
            anchors.add(anchor);
            //addObject(anchor);
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

        if (!isFailure() && (avatar.getY() < -1 || avatar.getY() > bounds.height + 1
        || avatar.getX() < -1 || avatar.getX() > bounds.getWidth() + 1)) {
            setFailure(true);
            return false;
        }

        if (!isFailure() && (avatar2.getY() < -1|| avatar2.getY() > bounds.height + 1
                || avatar2.getX() < -1 || avatar2.getX() > bounds.getWidth() + 1)) {
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
        avatar.setFixedRotation(false);
        avatar2.setFixedRotation(false);
        boolean anchorChange = false;

        if (InputController.getInstance().didSpace()) {
            if (avatar.isAnchored) {
                for (Spinner a : anchors) {
                    SPIN_POS.set(a.getPosition());
                    if (!avatar2.getOnPlanet() && avatar2.getPosition().dst(SPIN_POS.x - 1.0f, SPIN_POS.y - 1.0f) < ANCHOR_DIST) {
                        avatar2.isAnchored = true;
                        avatar2.setPosition(SPIN_POS.x, SPIN_POS.y);
                        avatar2.setLinearVelocity(new Vector2(0, 0));
                        avatar2.setAngularVelocity(0);
                        avatar.isAnchored = false;
//                        avatar.isAnchored = false;
//                        anchorChange = true;
                        break;
                    }
//                else {
//                    avatar.isAnchored = false;
//                }
                }
                //avatar.isAnchored = false;
            }

            else if (avatar2.isAnchored) {
                anchorChange = false;
                for (Spinner a : anchors) {
                    SPIN_POS = a.getPosition();
                    if (!avatar.getOnPlanet() && avatar.getPosition().dst(SPIN_POS.x - 1.0f, SPIN_POS.y - 1.0f) < ANCHOR_DIST) {
                        avatar.isAnchored = true;
                        avatar.setPosition(SPIN_POS.x, SPIN_POS.y);
                        avatar.setLinearVelocity(new Vector2(0, 0));
                        avatar.setAngularVelocity(0);
                        avatar2.isAnchored = false;
//                        avatar2.isAnchored = false;
//                        anchorChange = true;
                        break;
                    }
//                else {
//                    avatar2.isAnchored = false;
//                }
                }
                //avatar2.isAnchored = false;
            }

            else if ((!avatar.getOnPlanet() || !avatar2.getOnPlanet()) && !avatar.isAnchored && !avatar2.isAnchored){
                for (Spinner a : anchors) {
                    SPIN_POS = a.getPosition();
                    if (!avatar.getOnPlanet() && avatar.getPosition().dst(SPIN_POS.x - 1.0f, SPIN_POS.y - 1.0f) < ANCHOR_DIST) {
                        avatar.isAnchored = true;
                        avatar.setPosition(SPIN_POS.x, SPIN_POS.y);
                        avatar.setLinearVelocity(new Vector2(0, 0));
                        avatar.setAngularVelocity(0);
                        break;
                    }

                    else if (!avatar2.getOnPlanet() && avatar2.getPosition().dst(SPIN_POS.x - 1.0f, SPIN_POS.y - 1.0f) < ANCHOR_DIST) {
                        avatar2.isAnchored = true;
                        avatar2.setPosition(SPIN_POS.x, SPIN_POS.y);
                        avatar2.setLinearVelocity(new Vector2(0, 0));
                        avatar2.setAngularVelocity(0);
                        break;
                    }
                }
            }
        }

        if (!avatar.isAnchored) {
            // Process actions in object model
            //avatar.setMovement(InputController.getInstance().getHorizontal() *avatar.getForce());
            avatar.setJumping(InputController.getInstance().didPrimary());
//        avatar.setShooting(InputController.getInstance().didSecondary());
            avatar.setRotation(InputController.getInstance().getHorizontal());
            //System.out.println(avatar.isGrounded());
            if (avatar.getOnPlanet()) {
//            Vector2 dir = new Vector2(0, 1);
//            dir.rotateRad(avatar.getAngle());
                avatar.setLinearVelocity(new Vector2(0,0));
                avatar.setFixedRotation(true);
                Vector2 contactDir = contactPoint.cpy().sub(curPlanet.getPosition());
                //System.out.println(contactDir);
                float angle = -contactDir.angleRad(new Vector2 (0, 1));
                //System.out.println(angle*(float)(180/Math.PI));
                avatar.setAngle(angle);
                avatar.setPosition(contactPoint);
                if (InputController.getInstance().didRight()) {
                    contactDir = contactPoint.cpy().sub(curPlanet.getPosition());
                    contactDir.rotateRad(-(float) Math.PI / 2);
                    avatar.setPosition(contactPoint.add(contactDir.setLength(0.03f)));
                }
                if (InputController.getInstance().didLeft()) {
                    //contactDir = contactPoint.cpy().sub(curPlanet.getPosition());
                    contactDir.rotateRad(-(float) Math.PI / 2);
                    avatar.setPosition(contactPoint.sub(contactDir.setLength(0.03f)));
                }
                //System.out.println(avatar.getPosition() + ", " + curPlanet.getPosition());
                if (avatar.isJumping()) {
                    contactDir.set(avatar.getPosition().cpy().sub(curPlanet.getPosition()));
                    //System.out.println(contactDir);
                    avatar.setOnPlanet(false);
                    avatar.dudeJump.set(contactDir);
                }
            }
            //if (!avatar.getOnPlanet() && vectorWorld.getForce(avatar.getPosition()) != null)
            avatar.setGravity(vectorWorld.getForce(avatar.getPosition()));
            avatar.applyForce();
        }

        if (!avatar2.isAnchored) {
            avatar2.setRotation(InputController.getInstance().getHorizontal2());
            avatar2.setJumping(InputController.getInstance().didSecondary());
            if (avatar2.getOnPlanet()) {
                avatar2.setLinearVelocity(new Vector2(0,0));
                avatar2.setFixedRotation(true);
                Vector2 contactDir = contactPoint2.cpy().sub(curPlanet2.getPosition());
                float angle = -contactDir.angleRad(new Vector2 (0, 1));
                avatar2.setAngle(angle);
                avatar2.setPosition(contactPoint2);
                if (InputController.getInstance().didD()) {
                    contactDir = contactPoint2.cpy().sub(curPlanet2.getPosition());
                    contactDir.rotateRad(-(float) Math.PI / 2);
                    avatar2.setPosition(contactPoint2.add(contactDir.setLength(0.03f)));
                }
                if (InputController.getInstance().didA()) {
                    contactDir.rotateRad(-(float) Math.PI / 2);
                    avatar2.setPosition(contactPoint2.sub(contactDir.setLength(0.03f)));
                }
                if (avatar2.isJumping()) {
                    contactDir.set(avatar2.getPosition().cpy().sub(curPlanet2.getPosition()));
                    avatar2.setOnPlanet(false);
                    avatar2.dudeJump.set(contactDir);
                }
            }
            avatar2.setGravity(vectorWorld.getForce(avatar2.getPosition()));
            avatar2.applyForce();
        }

        // Add a bullet if we fire
        if (avatar.isShooting()) {
            createBullet();
        }

        //TODO Removed sound stuffs
//        if (avatar.isJumping()) {
//            SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
//        }

        // If we use sound, we must remember this.
//        SoundController.getInstance().update();
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
            //System.out.println(bd1.getName() + bd2.getName());

            if (bd1.getName().contains("star") || bd1.getName().contains("anchor") || bd2.getName().contains("star") || bd2.getName().contains("anchor")){
                return;
            }

            /** Force astronaut's position on planet */
            if ((bd1 == avatar || bd2 == avatar) && bd1 != avatar2 && bd2 !=avatar2) {
                curPlanet = (bd1 == avatar) ? bd2 : bd1;
                if (curPlanet.getName().contains("planet")) {
                    //Vector2 angle = contact.getWorldManifold().getPoints()[0].cpy().sub(objCache.getCenter());
                    //contactDir.set(contact.getWorldManifold().getPoints()[0].cpy().sub(curPlanet.getCenter()));
                    //contactPoint.set(contact.getWorldManifold().getPoints()[0].cpy());
                    contactPoint.set(contact.getWorldManifold().getPoints()[0].cpy());
                    avatar.setOnPlanet(true);
                }
                // See if we have landed on the ground.
                if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                        (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
                    avatar.setGrounded(true);
                    avatar.setOnPlanet(true);
                    contactPoint.set(avatar.getPosition());
                    sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
                }
            }

            if ((bd1 == avatar2 || bd2 == avatar2) && bd1 != avatar && bd2 !=avatar) {
                curPlanet2 = (bd1 == avatar2) ? bd2 : bd1;
                if (curPlanet2.getName().contains("planet")) {
                    //Vector2 angle = contact.getWorldManifold().getPoints()[0].cpy().sub(objCache.getCenter());
                    //contactDir.set(contact.getWorldManifold().getPoints()[0].cpy().sub(curPlanet.getCenter()));
                    //contactPoint.set(contact.getWorldManifold().getPoints()[0].cpy());
                    contactPoint2.set(contact.getWorldManifold().getPoints()[0].cpy());
                    avatar2.setOnPlanet(true);
                }
                // See if we have landed on the ground.
                if ((avatar2.getSensorName().equals(fd2) && avatar2 != bd1) ||
                        (avatar2.getSensorName().equals(fd1) && avatar2 != bd2)) {
                    avatar2.setGrounded(true);
                    avatar2.setOnPlanet(true);
                    contactPoint2.set(avatar2.getPosition());
                    sensorFixtures.add(avatar2 == bd1 ? fix2 : fix1); // Could have more than one ground
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
        avatar.setOnPlanet(false);
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
    public void preSolve(Contact contact, Manifold oldManifold) {}

    /**
     * Draw the physics objects to the canvas and the background
     *
     * The method draws all objects in the order that they were added.
     *
     * @param delta The delay in seconds since the last update
     */
    public void draw(float delta) {
        canvas.clear();

        // Draw background unscaled.
        canvas.begin();
        canvas.draw(background, Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());
        canvas.end();

        canvas.begin();
        for(Obstacle obj : objects) {
            //System.out.println(obj.getName());
            if (obj.getName().contains("planet")) {
                //System.out.println("yeet");
                planetCache = (WheelObstacle) obj;
                canvas.draw(planetCache.getTexture(),Color.WHITE,planetCache.origin.x,planetCache.origin.y,
                        planetCache.getX()*planetCache.drawScale.x,planetCache.getY()*planetCache.drawScale.x,
                        planetCache.getAngle(),planetCache.scaleDraw,planetCache.scaleDraw);

            }
            else
                obj.draw(canvas);
        }
        for (Spinner a : anchors) {
            a.draw(canvas);
        }
        for (Spinner s : stars) {
            s.draw(canvas);
        }
        canvas.end();

        if (isDebug()) {
            canvas.beginDebug();
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }


    }
}