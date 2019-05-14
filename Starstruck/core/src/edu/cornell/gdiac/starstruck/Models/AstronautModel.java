/*
 * AstronautModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

//import edu.cornell.gdiac.starstruck.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.GameController;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.lang.reflect.Field;

/**
 * Avatar avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class AstronautModel extends CapsuleObstacle {
    // Physics constants
    /** The density of the character */
    private static final float DUDE_DENSITY = 1.0f;
    /** The factor to multiply by the input */
    private float DUDE_FORCE = 20.0f;
    /** The amount to slow the character down */
    private float DUDE_DAMPING = 10.0f;
    /** The dude is a slippery one */
    private static final float DUDE_FRICTION = 0.0f;
    /** The maximum character speed */
    private float DUDE_MAXSPEED = 3.2f;
    /** The maximum character rotation in space */
    private static final float DUDE_MAXROT = 6.5f;
    /** The impulse for the character jump */
    private float DUDE_JUMP = 10f;
    /** Cooldown (in animation frames) for jumping */
    private int JUMP_COOLDOWN = 30;
    /** Cooldown (in animation frames) for shooting */
    private static final int SHOOT_COOLDOWN = 40;
    /** Height of the sensor attached to the player's feet */
    private static final float SENSOR_HEIGHT = 0.05f;
    /** Identifier to allow us to track the sensor in ContactListener */
    private String SENSOR_NAME = "DudeGroundSensor";
    /** The color to paint the sensor in debug mode */
    private Color sensorColor;

    // This is to fit the image to a tigher hitbox
    /** The amount to shrink the body fixture (vertically) relative to the image */
    private static final float DUDE_VSHRINK = 0.95f;
    /** The amount to shrink the body fixture (horizontally) relative to the image */
    private static final float DUDE_HSHRINK = 0.7f;
    /** The amount to shrink the sensor fixture (horizontally) relative to the image */
    private static final float DUDE_SSHRINK = 0.6f;
    /** Player 1 glow color*/
    private static final Color p1glow = new Color(1, 1,1, 0.75f);
    /** Player 2 glow color*/
    private static final Color p2glow = p1glow;
    /** Scale for glow */
    private static final float GLOW_SCALE = 1.5f;

    /** The current horizontal movement of the character */
    private float   movement;
    /** The current vertical movement of the character */
    private float movementV;
    /** The rotation of this character */
    private float rotation;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** How long until we can jump again */
    private int jumpCooldown;
    /** Whether we are actively jumping */
    private boolean isJumping;
    /** The sound to make when jumping (the asset key) */
    private String jumpSound;
    /** How long until we can shoot again */
    private int shootCooldown;
    /** Whether our feet are on the ground */
    private boolean isGrounded;
    /** Whether we are actively shooting */
    private boolean isShooting;
    /** Ground sensor to represent our feet */
    private Fixture sensorFixture;
    private PolygonShape sensorShape;
    /** Force of gravity */
    public Vector2 gravity;
    /** Direction to apply force on planet */
    private Vector2 planetMove;
    /** Indicates whether astronaut is on planet */
    private boolean onPlanet;
    /** Direction the character should go when jumping off a planet */
    private Vector2 planetJump;
    /** Is this astronaut anchored */
    private boolean isAnchored;
    /** Whether the astronaut has collided with an anchor */
    private boolean anchorHit;
    /** What is the position of the current anchor? (May be null) */
    private Vector2 anchorPos;
    /** Is this astronaut the active character*/
    private boolean isActive;
    /** Texture region for the flow */
    private TextureRegion glowTexture;
    /** Filmstrip for idle animation */
    private FilmStrip idle;
    /** Origin of the glow texture */
    private Vector2 glowOrigin;
    /** Whether the astronaut is being moved, i.e. movement keys pressed */
    public boolean moving;
    /** Anchor astronaut is currently or last anchored on */
    private Anchor curAnchor;
    /** Is this player one?*/
    private boolean isPlayerOne;
    /** This astronaut's current planet */
    public Obstacle curPlanet;
    /** The previous position of this astronaut */
    public Vector2 lastPoint;
    /** The direction this astronaut is facing */
    public Vector2 contactDir;
    /** The previous linear velocity of this astronaut */
    public Vector2 lastVel;
    /** Did we just move? */
    private boolean justMoved;
    /** Velocity of astronaut to perserve when using portal */
    public Vector2 portalVel = new Vector2();
    /** Whether this astronaut is moving on its own */
    public boolean auto;
    /** Whether only to apply gravity */
    public boolean only;
    /** last planet speed */
    public float planetVel;
    /** Whether the astronaut is swinging off an anchor */
    public boolean swing;
    /** Whether the astronaut should go w the other one coming off an anchor */
    public boolean follow;
    /** Whether the astronaut should go to the planet coming off an anchor */
    public boolean toplanet;
    /** Whether the astronaut should "jump" off anchor */
    public boolean anchorhop;
    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();
    /** Two player? If true, don't draw glow */
    private boolean twoplayer;
    /** To keep or flip controls */
    private int lastFace = 1;

    /**
     * Set the glow texture
     *
     * @param value set the glow texture to value
     */
    public void setGlow(TextureRegion value) {
        glowTexture = value;
        glowOrigin = new Vector2(glowTexture.getRegionWidth()/2.0f, glowTexture.getRegionHeight()/2.0f);
    }

    public ModelColor getColor() {
        if (isPlayerOne) return ModelColor.PINK;
        return ModelColor.BLUE;
    }

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @return left/right movement of this character.
     */
    public float getMovement() {
        return movement;
    }

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times dude force.
     *
     * @param value left/right movement of this character.
     */
    public void setMovement(float value) {
        movement = value;
        // Change facing if appropriate
        if (movement < 0) {
            faceRight = false;
        } else if (movement > 0) {
            faceRight = true;
        }
    }

    public boolean getRight() {
        return faceRight;
    }

    public void setRight(boolean value) {
        faceRight = value;
    }

    public float getMovementV () { return movementV; }

    public void setMovementV(float value) {
        movementV = value;
    }

    public float getRotation() { return rotation; }

    public void setRotation(float value) {
        rotation = value;
    }

    /**
     * Returns true if the dude is actively firing.
     *
     * @return true if the dude is actively firing.
     */
    public boolean isShooting() {
        return isShooting && shootCooldown <= 0;
    }

    /**
     * Sets whether the dude is actively firing.
     *
     * @param value whether the dude is actively firing.
     */
    public void setShooting(boolean value) {
        isShooting = value;
    }

    /**
     * Returns true if the dude is actively jumping.
     *
     * @return true if the dude is actively jumping.
     */
    public boolean isJumping() {
        return isJumping;
    } //&& isGrounded && jumpCooldown <= 0

    /**
     * Sets whether the dude is actively jumping.
     *
     * @param value whether the dude is actively jumping.
     */
    public void setJumping(boolean value) {
        isJumping = value;
    }

    /**
     * Returns true if the dude is on the ground.
     *
     * @return true if the dude is on the ground.
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Sets whether the dude is on the ground.
     *
     * @param value whether the dude is on the ground.
     */
    public void setGrounded(boolean value) {
        isGrounded = value;
    }

    /**
     * Set gravity to vec
     *
     * @param vec The value to set gravity to
     */
    public void setGravity(Vector2 vec) {
        gravity.set(-vec.x, -vec.y);
    }

    public Vector2 getPlanetMove() { return planetMove; }

    public void setPlanetMove(Vector2 value) {
        planetMove.set(value);
    }

    /** Set two player */
    public void setTwoPlayer(boolean value) {
        twoplayer = value;
    }

    public int lastFace() { return lastFace; }

    public void setLastFace(int value) { lastFace = value; }

    /**
     * Returns how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the dude moving
     */
    public float getForce() {
        return DUDE_FORCE;
    }

    /**
     * Sets how much force to apply to get the dude moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @param value	how much force to apply to get the dude moving
     */
    public void setForce(float value) {
        DUDE_FORCE = value;
    }

    /**
     * Returns ow hard the brakes are applied to get a dude to stop moving
     *
     * @return ow hard the brakes are applied to get a dude to stop moving
     */
    public float getDamping() {
        return DUDE_DAMPING;
    }

    /**
     * Sets how hard the brakes are applied to get a dude to stop moving
     *
     * @param value	how hard the brakes are applied to get a dude to stop moving
     */
    public void setDamping(float value) {
        DUDE_DAMPING = value;
    }

    /**
     * Returns the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on dude left-right movement.
     */
    public float getMaxSpeed() {
        return DUDE_MAXSPEED;
    }

    /**
     * Sets the upper limit on dude left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @param value	the upper limit on dude left-right movement.
     */
    public void setMaxSpeed(float value) {
        DUDE_MAXSPEED = value;
    }

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return SENSOR_NAME;
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    public boolean getOnPlanet() { return onPlanet; }

    public void setOnPlanet(boolean value) {
        onPlanet = value;
        if (!value)
            setFixedRotation(false);
    }

    /**
     * Sets the direction to launch off planet.
     *
     * @param dir the direction the astronaut should jump
     */
    public void setPlanetJump(Vector2 dir) { planetJump.set(dir); }

    /**
     * Returns whether the astronoaut is anchored
     *
     * @return true if the astronaut is anchored, false otherwise
     */
    public boolean isAnchored() { return isAnchored; }

    /**
     * Sets astronaut to be anchored
     *
     * @param anchor the anchor the astronaut is glued to
     */
    public void setAnchored(Anchor anchor) {
        anchorPos = anchor.getPosition();
        curAnchor = anchor;
        isAnchored = true;
    }

    /**
     * Sets astronaut to be unanchored
     */
    public void setUnAnchored() { isAnchored = false; }

    public boolean anchorHit() { return anchorHit; }

    public void setAnchorHit(boolean value) { anchorHit = value; }

    /**
     * Whether astronaut is acitive
     *
     * @return true if the astronaut is active, false otherwise
     */
    public boolean isActive() { return isActive; }

    /**
     * Sets whether the astronaut is active
     *
     * @param active whether the astronaut is active
     */
    public void setActive(boolean active) { isActive = active; }

    /**
     * Gets curAnchor for this astronaut
     *
     * @return Anchor curAnchor
     */
    public Anchor getCurAnchor() { return curAnchor; }


    /**
     * Set curAnchor for this astronaut
     *
     * @param a anchor
     */
    public void setCurAnchor(Anchor a) { curAnchor = a; }

    /**
     * Returns the upward impulse for a jump.
     *
     * @return the upward impulse for a jump.
     */
    public float getJumpPulse() {
        return DUDE_JUMP;
    }

    /**
     * Sets the upward impulse for a jump.
     *
     * @param value	the upward impulse for a jump.
     */
    public void setJumpPulse(float value) {
        DUDE_JUMP = value;
    }

    /**
     * Returns the cooldown limit between jumps
     *
     * @return the cooldown limit between jumps
     */
    public int getJumpLimit() {
        return JUMP_COOLDOWN;
    }

    /**
     * Sets the cooldown limit between jumps
     *
     * @param value	the cooldown limit between jumps
     */
    public void setJumpLimit(int value) {
        JUMP_COOLDOWN = value;
    }

    /**
     * Returns the sound to play when the character jumps
     *
     * This is not the sound asset, but the key.  We do it this way
     * because Sound classes are complicated and it is not really
     * safe to access them outside of SoundController.
     *
     * @return the sound to play when the character jumps
     */
    public String getJumpSound() {
        return jumpSound;
    }

    /**
     * Sets the sound to play when the character jumps
     *
     * This is not the sound asset, but the key.  We do it this way
     * because Sound classes are complicated and it is not really
     * safe to access them outside of SoundController.
     *
     * @param sound	the sound to play when the character jumps
     */
    public void setJumpSound(String sound) {
        jumpSound = sound;
    }

    /**
     * Creates a new dude at the origin.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public AstronautModel(float width, float height, boolean active, boolean playerOne) {
        this(0,0,width,height, active, playerOne);
    }

    /**
     * Creates a new dude with degenerate settings
     *
     * The main purpose of this constructor is to set the initial capsule orientation.
     */
    public AstronautModel(boolean playerOne) {
        this(0,0,0.5f, 1.0f, playerOne, playerOne);
    }

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * drawing to work properly, you MUST set the drawScale. The drawScaled
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public AstronautModel(float x, float y, float width, float height, boolean active, boolean playerOne) {
        super(x,y,width*DUDE_HSHRINK,height*DUDE_VSHRINK);
        setDensity(DUDE_DENSITY);
        setFriction(DUDE_FRICTION);  /// HE WILL STICK TO WALLS IF YOU FORGET
        //setFixedRotation(true);

        // Gameplay attributes
        isGrounded = false;
        isShooting = false;
        isJumping = false;
        faceRight = true;
        gravity = new Vector2();
        planetMove = new Vector2();
        onPlanet = false;
        planetJump = new Vector2();
        /** Is this astronaut anchored */
        isAnchored = false;
        anchorHit = false;
        /** Is this astronaut the active character*/
        isActive = active;
        isPlayerOne = playerOne;
        lastPoint = new Vector2();
        contactDir = new Vector2();
        lastVel = new Vector2();

        shootCooldown = 0;
        jumpCooldown = 0;
        setName("dude");

        anchorPos = null;

        sensorColor = Color.RED;

        String film = playerOne ? "astronaut 1 idle" : "astronaut 2 idle";
        idle = JsonAssetManager.getInstance().getEntry(film, FilmStrip.class);
        justMoved = false;
    }

    /**
     * Initializes the dude via the given JSON value
     *
     * The JSON value has been parsed and is part of a bigger level file.  However,
     * this JSON value is limited to the dude subtree
     *
     * @param json	the JSON subtree defining the dude
     */
    public void initialize(JsonValue json) {
        setName(json.name());
        float[] pos  = json.get("pos").asFloatArray();
        float[] size = json.get("size").asFloatArray();
        setPosition(pos[0],pos[1]);
        setDimension(size[0],size[1]);

        // Technically, we should do error checking here. TODO
        // A JSON field might accidentally be missing
        setBodyType(json.get("bodytype").asString().equals("static") ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody);
        setDensity(json.get("density").asFloat());
        setFriction(json.get("friction").asFloat());
        setRestitution(json.get("restitution").asFloat());
        setForce(json.get("force").asFloat());
        setDamping(json.get("damping").asFloat());
        setMaxSpeed(json.get("maxspeed").asFloat());
        setJumpPulse(json.get("jumppulse").asFloat());
        setJumpLimit(json.get("jumplimit").asInt());

        // Reflection is best way to convert name to color
        Color debugColor;
        try {
            String cname = json.get("debugcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            debugColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            debugColor = null; // Not defined
        }
        int opacity = json.get("debugopacity").asInt();
        debugColor.mul(opacity/255.0f);
        setDebugColor(debugColor);

        // Now get the texture from the AssetManager singleton
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setTexture(texture);

        key = json.get("glow texture").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        setGlow(texture);

        // Get the sensor information
        Vector2 sensorCenter = new Vector2(0, -getHeight()/2);
        float[] sSize = json.get("sensorsize").asFloatArray();
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(sSize[0], sSize[1], sensorCenter, 0.0f);

        // Reflection is best way to convert name to color
        try {
            String cname = json.get("sensorcolor").asString().toUpperCase();
            Field field = Class.forName("com.badlogic.gdx.graphics.Color").getField(cname);
            sensorColor = new Color((Color)field.get(null));
        } catch (Exception e) {
            sensorColor = null; // Not defined
        }
        opacity = json.get("sensoropacity").asInt();
        sensorColor.mul(opacity/255.0f);
        SENSOR_NAME = json.get("sensorname").asString();

        // Store the key to the jump sound
        setJumpSound(json.get("jumpsound").asString());
    }

    /**
     * Return a new astronaut with parameters specified by the JSON
     * @param json A JSON containing data for one astronaut
     * @param scale The scale to convert physics units to drawing units
     * @param active Whether this astronaut is player 1/active.
     * @return An astronaut created according to the specifications in the JSON
     */
    public static AstronautModel fromJson(JsonValue json, Vector2 scale, boolean active) {
        float[] pos  = json.get("pos").asFloatArray();
        float posX = pos[0], posY = pos[1];
        float[] size = json.get("size").asFloatArray();
        float sizeX = size[0], sizeY = size[1];
        AstronautModel astro = new AstronautModel(posX, posY, sizeX, sizeY, active, active);
        astro.setDrawScale(scale);

        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        astro.setTexture(texture);

        key = json.get("glow texture").asString();
        texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        astro.setGlow(texture);

        return astro;
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write position and size
        Vector2 pos = getPosition();

        JsonValue position = new JsonValue(JsonValue.ValueType.array);
        position.addChild(new JsonValue(pos.x));
        position.addChild(new JsonValue(pos.y));

        JsonValue size = new JsonValue(JsonValue.ValueType.array);
        size.addChild(new JsonValue(getWidth()/DUDE_HSHRINK));
        size.addChild(new JsonValue(getHeight()/DUDE_VSHRINK));

        json.addChild("pos", position);
        json.addChild("size", size);

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(texture)));
        json.addChild("glow texture", new JsonValue(JsonAssetManager.getInstance().getKey(glowTexture)));

        return json;
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        // Ground Sensor
        // -------------
        // We only allow the dude to jump when he's on the ground.
        // Double jumping is not allowed.
        //
        // To determine whether or not the dude is on the ground,
        // we create a thin sensor under his feet, which reports
        // collisions with the world but has no collision response.
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = DUDE_DENSITY;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(DUDE_SSHRINK*getWidth()/2.0f, SENSOR_HEIGHT, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());

        return true;
    }


    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {

        if (!getOnPlanet()) {
            if (Math.abs(getAngularVelocity()) >= DUDE_MAXROT && !(isAnchored && !isActive)) {
                body.setAngularVelocity(Math.signum(getAngularVelocity()) * DUDE_MAXROT);
            }
            else if (!(isAnchored && !isActive)) {
                //body.applyTorque(-getRotation(), true);
                body.setAngularVelocity(getAngularVelocity() - 0.1f * getRotation());
            }
        }

        if (getOnPlanet()) {
//            Vector2 dir = curPlanet.getPosition().cpy().sub(getPosition());
//            float angle = getLinearVelocity().angleRad(dir);
//            System.out.println(getName() + ": " + (angle * (180/Math.PI)));
//            float speed = getLinearVelocity().len() * (float)Math.sin(angle);
//            System.out.println(speed);
            float speed = getLinearVelocity().len();
            if (speed > 0.05) {
            }
            Vector2 reset = new Vector2();
            //Damp out player motion
//            if (!moving) {
//                forceCache.set(planetMove.scl(-1).setLength(DUDE_MAXSPEED*100));
//            }
//
//            else {
//                forceCache.set(planetMove.setLength(DUDE_MAXSPEED));
//            }
//            body.applyForce(forceCache, getPosition(), true);

            // Don't want to be moving. Damp out player motion
            if (!moving) {
                body.setLinearVelocity(reset);
                body.applyLinearImpulse(gravity, getPosition(), true);
            }
            //forceCache.set(0, 0);
//            else if (speed >= DUDE_MAXSPEED) {
//                forceCache.set(planetMove.setLength(DUDE_MAXSPEED));
//            }
            else {
                int force = 4;
                forceCache.set(planetMove.setLength(DUDE_MAXSPEED));
                if (auto) {
                    //body.applyLinearImpulse(gravity, getPosition(), true);
                    body.setLinearVelocity(forceCache.scl(2));
                }
                else if (!only) {
                    if (speed >= DUDE_MAXSPEED) {
                        forceCache.set(reset);
                    }
                    //body.applyLinearImpulse(gravity, getPosition(), true);
                    body.applyForce(forceCache.scl(force), getPosition(), true);
                    //System.out.println(forceCache);
                }
            }
            body.applyLinearImpulse(gravity, getPosition(), true);
            //body.applyForce(gravity, getPosition(), true);
            //body.setLinearVelocity(forceCache);
        }
        justMoved = moving;
        moving = false;
        auto = false;

        // Jump!
        if (isJumping()) {
            forceCache.set(planetJump.setLength(DUDE_JUMP));
            body.setLinearVelocity(forceCache);//,getPosition(),true);
            body.setAwake(true);
            setJumping(false);
        }

        // Gravity from planets
        if (!GameController.testC && !only) {
            body.applyForce(gravity, getPosition(), true);
        }
        only = false;

        if (GameController.testC) {
            forceCache.set(getMovement(), getMovementV());
            body.setLinearVelocity(forceCache.scl(4));
        }

    }

    public Planet getCurPlanet() {
        try {
            return (Planet) curPlanet;
        } catch (ClassCastException e) {
            return null;
        } catch (NullPointerException n) {
            return null;
        }
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        // Apply cooldowns

        if (onPlanet) {
            if (justMoved || !idle.justReset()) idle.tick();
//            else if (!idle.justReset()) idle.kcit();
        }

        if (!onPlanet && !idle.justReset()) idle.reset();

        if (isAnchored){
            setLinearVelocity(curAnchor.getLinearVelocity());
            setPosition(anchorPos);
            //setBodyType(BodyDef.BodyType.StaticBody);
            setBodyType(BodyDef.BodyType.KinematicBody);
        } else {
            setBodyType(BodyDef.BodyType.DynamicBody);
        }

//        if (isJumping()) {
//            jumpCooldown = JUMP_COOLDOWN;
//            setJumping(false);
//        } else {
//            jumpCooldown = Math.max(0, jumpCooldown - 1);
//            setJumping(false);
//        }

        if (isShooting()) {
            shootCooldown = SHOOT_COOLDOWN;
        } else {
            shootCooldown = Math.max(0, shootCooldown - 1);
        }

        super.update(dt);
    }

    public String toString() {
        String out = "Player with {";

        out += "pos: " + getPosition();
        out += "}";

        return out;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? 1.0f : -1.0f;
        if (isActive() && !twoplayer) {
            Color color = isPlayerOne ? p1glow : p2glow;
            canvas.draw(glowTexture, color, glowOrigin.x, glowOrigin.y, (getX()) * drawScale.x,
                    (getY()) * drawScale.y, getAngle(), effect * GLOW_SCALE, GLOW_SCALE);
        }
        if (onPlanet){
            canvas.draw(idle,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,
                    getY()*drawScale.y,getAngle(),effect,1.0f);
        } else {
            canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x,
                    getY() * drawScale.y, getAngle(), effect, 1.0f);
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        if (sensorColor != null) canvas.drawPhysics(sensorShape,sensorColor,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }

    public ObstacleType getType() { return ObstacleType.PLAYER;}
}