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
package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

//import edu.cornell.gdiac.starstruck.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Obstacles.*;
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
    private float DUDE_MAXSPEED = 2.8f;
    /** The maximum character rotation in space */
    private static final float DUDE_MAXROT = 6.5f;
    /** The impulse for the character jump */
    private float DUDE_JUMP = 6f;
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
    protected Vector2 gravity;
    /** Direction to apply force on planet */
    private Vector2 planetMove;
    /** Indicates whether astronaut is on planet */
    private boolean onPlanet;
    /** Direction the character should go when jumping off a planet */
    private Vector2 planetJump;
    /** Is this astronaut anchored */
    private boolean isAnchored;
    /** What is the position of the current anchor? (May be null) */
    private Vector2 anchorPos;
    /** Is this astronaut the active character*/
    private boolean isActive;
    /** Texture region for the flow */
    private TextureRegion glowTexture;
    /** Origin of the glow texture */
    private Vector2 glowOrigin;
    /** Whether the astronaut is being moved, i.e. movement keys pressed */
    protected boolean moving;

    /** Is this player one?*/
    private boolean isPlayerOne;
    /** Player 1 glow color*/
    private static final Color p1glow = new Color(1, 1,1, 0.75f);
    /** Player 2 glow color*/
    private static final Color p2glow = p1glow;
    /** Scale for glow */
    private static final float GLOW_SCALE = 1.5f;

    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    /**
     * Set the glow texture
     *
     * @param value set the glow texture to value
     */
    public void setGlow(TextureRegion value) {
        glowTexture = value;
        glowOrigin = new Vector2(glowTexture.getRegionWidth()/2.0f, glowTexture.getRegionHeight()/2.0f);
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
    public void setAnchored(Anchor anchor) { anchorPos = anchor.getPosition(); isAnchored = true;}

    /**
     * Sets astronaut to be unanchored
     */
    public void setUnAnchored() { isAnchored = false; }

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
        /** Is this astronaut the active character*/
        isActive = active;
        isPlayerOne = playerOne;

        shootCooldown = 0;
        jumpCooldown = 0;
        setName("dude");

        anchorPos = null;

        sensorColor = Color.RED;
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

        // Technically, we should do error checking here.
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
            if (Math.abs(getAngularVelocity()) >= DUDE_MAXROT) {
                setAngularVelocity(Math.signum(getAngle()) * getMaxSpeed());
            } else {
                body.applyTorque(-getRotation(), true);
            }
        }

        if (getOnPlanet()) {
            float speed = getLinearVelocity().len();
            if (speed < 0)
                System.out.println("speed is less than 0 in apply force");

            // Don't want to be moving. Damp out player motion
            if (!moving)
                forceCache.set(0, 0);
//            else if (speed >= DUDE_MAXSPEED)
//                forceCache.set(planetMove.setLength(DUDE_MAXSPEED));

            else
                forceCache.set(planetMove.setLength(DUDE_MAXSPEED));
            body.applyLinearImpulse(gravity, getPosition(), true);
            body.setLinearVelocity(forceCache);
        }
        moving = false;

        // Jump!
        if (isJumping()) {
            forceCache.set(planetJump.setLength(DUDE_JUMP));
            body.setLinearVelocity(forceCache);//,getPosition(),true);
            body.setAwake(true);
            setJumping(false);
        }

        // Gravity from planets
        if (!GameController.testC ) {
            body.applyForce(gravity, getPosition(), true);
        }

        if (GameController.testC) {
            forceCache.set(getMovement(), getMovementV());
            body.setLinearVelocity(forceCache.scl(4));
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

        if (isAnchored) setPosition(anchorPos);

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

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float effect = faceRight ? -1.0f : 1.0f;
        if (isActive()) {
            Color color = isPlayerOne ? p1glow : p2glow;
            canvas.draw(glowTexture, color, glowOrigin.x, glowOrigin.y, (getX()) * drawScale.x,
                    (getY()) * drawScale.y, getAngle(), effect * GLOW_SCALE, GLOW_SCALE);
        }
        canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,
                getY()*drawScale.y,getAngle(),effect,1.0f);
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
}