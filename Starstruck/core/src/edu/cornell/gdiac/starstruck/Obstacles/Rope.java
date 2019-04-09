/*
 * Rope.java
 *
 * The class is a classic example of how to subclass ComplexPhysicsObject.
 * You have to implement the createJoints() method to stick in all of the
 * joints between objects.
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;

import java.util.*;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.*;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.util.JsonAssetManager;

/**
 * A bridge with planks connected by revolute joints.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class Rope extends ComplexObstacle {
    /** The debug name for the entire obstacle */
    private static final String BRIDGE_NAME = "rope_bridge";
    /** The debug name for each plank */
    private static final String PLANK_NAME = "rope_plank";
    /** The debug name for each anchor pin */
    private static final String BRIDGE_PIN_NAME = "rope_pin";
    /** The radius of each anchor pin */
    private static final float BRIDGE_PIN_RADIUS = 0.1f;
    /** The density of each plank in the bridge */
    private static final float BASIC_DENSITY = 1.0f;
    /** Max distance between rope links */
    private static final float MAX_LENGTH = 100f;
    /** 0 vector */
    private static final Vector2 reset = new Vector2(0, 0);

    // Invisible anchor objects
    /** The left side of the bridge */
    private WheelObstacle start = null;
    /** The right side of the bridge */
    private WheelObstacle finish = null;

    // Dimension information
    /** The size of the entire bridge */
    protected Vector2 dimension;
    /** The size of a single plank */
    protected Vector2 planksize;
    /* The length of each link */
    public float linksize = 1.0f;
    /** The spacing between each link */
    protected float spacing = 0.0f;
    /** Lengh of the rope */
    protected float length;
    /** Number of links in the rope */
    private int nlinks;
    /** Original number of links in the rope */
    public int initLinks;
    /** Initial size of joints list*/
    private int initJointSize;
    /** Initial size of planks list*/
    private int initPlankSize;
    /** Whether avatar2 was the one on anchor */
    private boolean isAvatar2 = false;

    private AstronautModel avatar;
    private AstronautModel avatar2;

    /** AstonautModel cache for extending rope */
    private AstronautModel astroCache;

    /**
     * Creates a new rope bridge at the given position.
     *
     * This bridge is straight horizontal. The coordinates given are the
     * position of the leftmost anchor.
     *
     * @param x  		The x position of the left anchor
     * @param y  		The y position of the left anchor
     * @param width		The length of the bridge
     * @param lwidth	The plank length
     * @param lheight	The bridge thickness
     * @param avatar 	avatar
     * @param avatar2   avatar2
     */
    public Rope(float x, float y, float width, float lwidth, float lheight, AstronautModel avatar, AstronautModel avatar2) {
        this(x, y, x+width, y, lwidth, lheight);
        this.avatar = avatar;
        this.avatar2 = avatar2;
    }

    /**
     * Creates a new rope bridge with the given anchors.
     *
     * @param x0  		The x position of the left anchor
     * @param y0  		The y position of the left anchor
     * @param x1  		The x position of the right anchor
     * @param y1  		The y position of the right anchor
     * @param lwidth	The plank length
     * @param lheight	The bridge thickness
     */
    public Rope(float x0, float y0, float x1, float y1, float lwidth, float lheight) {
        super(x0,y0);
        setName(BRIDGE_NAME);

        planksize = new Vector2(lwidth,lheight);
        linksize = planksize.x;

        // Compute the bridge length
        dimension = new Vector2(x1-x0,y1-y0);
        float length = dimension.len();
        Vector2 norm = new Vector2(dimension);
        norm.nor();

        // If too small, only make one plank.
        int nLinks = (int)(length / linksize);
        if (nLinks <= 1) {
            nLinks = 1;
            linksize = length;
            spacing = 0;
        } else {
            spacing = length - nLinks * linksize;
            spacing /= (nLinks-1);
        }

        // Create the planks
        planksize.x = linksize;
        Vector2 pos = new Vector2();
        for (int ii = 0; ii < nLinks; ii++) {
            float t = ii*(linksize+spacing) + linksize/2.0f;
            pos.set(norm);
            pos.scl(t);
            pos.add(x0,y0);
            BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
            plank.setName(PLANK_NAME+ii);
            plank.setDensity(BASIC_DENSITY);
            bodies.add(plank);
        }
        nlinks = nLinks;
        this.length = nLinks * linksize + nLinks * spacing;
        initLinks = nLinks;
        initPlankSize = bodies.size;
    }

    /**
     * Creates the joints for this object.
     *
     * This method is executed as part of activePhysics. This is the primary method to
     * override for custom physics objects.
     *
     * @param world Box2D world to store joints
     *
     * @return true if object allocation succeeded
     */
    protected boolean createJoints(World world) {
        assert bodies.size > 0;

        Vector2 anchor1 = new Vector2();
        Vector2 anchor2 = new Vector2(-linksize / 2, 0);

        //AstronautModel avatar =

        // Create the leftmost anchor
        // Normally, we would do this in constructor, but we have
        // reasons to not add the anchor to the bodies list.
//		Vector2 pos = avatar.getPosition();
//		pos.x -= linksize / 2;
//		start = new WheelObstacle(pos.x,pos.y,BRIDGE_PIN_RADIUS);
//		start.setName(BRIDGE_PIN_NAME+0);
//		start.setDensity(BASIC_DENSITY);
//		start.setBodyType(BodyDef.BodyType.DynamicBody);
//		start.activatePhysics(world);

        // Definition for a revolute joint
        RevoluteJointDef jointDef = new RevoluteJointDef();
        RopeJointDef ropeJointDef = new RopeJointDef();

        // Initial joint
        jointDef.bodyA = avatar.getBody();
        jointDef.bodyB = bodies.get(0).getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
        joints.add(joint);
//        ropeJointDef.bodyA = avatar.getBody();
//        ropeJointDef.bodyB = bodies.get(0).getBody();
//        ropeJointDef.localAnchorA.set(anchor1);
//        ropeJointDef.localAnchorB.set(anchor2);
//        ropeJointDef.collideConnected = false;
//        ropeJointDef.maxLength = linksize/2;//0.01f;
//        joint = world.createJoint(ropeJointDef);
//        joints.add(joint);

        // Link the planks together
        anchor1.x = linksize / 2;
        for (int ii = 0; ii < bodies.size-1; ii++) {
            //#region INSERT CODE HERE
            // Look at what we did above and join the planks
            jointDef.bodyA = bodies.get(ii).getBody();
            jointDef.bodyB = bodies.get(ii + 1).getBody();
            jointDef.localAnchorA.set(anchor1);
            jointDef.localAnchorB.set(anchor2);
            joint = world.createJoint(jointDef);
            joints.add(joint);
//            ropeJointDef.bodyA = avatar.getBody();
//            ropeJointDef.bodyB = bodies.get(ii+1).getBody();
//            ropeJointDef.localAnchorA.set(reset);
//            ropeJointDef.localAnchorB.set(anchor2);
//            //ropeJointDef.collideConnected = false;
//            ropeJointDef.maxLength = linksize*(ii+1) + linksize/2;
//            joint = world.createJoint(ropeJointDef);
//            joints.add(joint);

            initJointSize = joints.size;

            //#endregion
        }

        // Create the rightmost anchor
//		Obstacle last = bodies.get(bodies.size-1);
//
//		pos = avatar2.getPosition();
//		pos.x += linksize / 2;
//		finish = new WheelObstacle(pos.x,pos.y,BRIDGE_PIN_RADIUS);
//		finish.setName(BRIDGE_PIN_NAME+1);
//		finish.setDensity(BASIC_DENSITY);
//		finish.setBodyType(BodyDef.BodyType.DynamicBody);
//		finish.activatePhysics(world);

        // Final joint
        anchor2.x = 0;
        jointDef.bodyA = bodies.get(bodies.size - 1).getBody();
        jointDef.bodyB = avatar2.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        joint = world.createJoint(jointDef);
        joints.add(joint);
//        ropeJointDef.bodyA = avatar.getBody();
//        ropeJointDef.bodyB = avatar2.getBody();
//        ropeJointDef.localAnchorA.set(reset);
//        ropeJointDef.localAnchorB.set(anchor2);
//        ropeJointDef.collideConnected = false;
//        ropeJointDef.maxLength = linksize*nLinks;
//        joint = world.createJoint(ropeJointDef);
//        joints.insert(0, joint);
//
//        RopeJointDef ropeJoint = new RopeJointDef();
//        ropeJoint.bodyA = avatar.getBody();
//        ropeJoint.bodyB = avatar2.getBody();
//        ropeJoint.localAnchorA.set(anchor1);
//        ropeJoint.localAnchorB.set(anchor2);
//        ropeJoint.maxLength = length;
//        joint = world.createJoint(ropeJoint);
//        joints.insert(0, joint);

        return true;
    }

    public float getLength() { return length; }

    public int nLinks(){ return nlinks; }

    //public void setLength(int newLength) {length = newLength;}

    public Array<Joint> getJointList() { return joints; }


    public Body getLastPlankBody() {
        return bodies.get(bodies.size - 1).getBody();
    }

    public void removeLastJoint (Rope rope) {
        joints.removeIndex(joints.size - 1);
    }


    public void newPairPlank(World world, Rope rope) {
//        System.out.println(bodies.size);
//        System.out.println(planks.size());

        //need to remove last plank and replace it with two new ones
        removeLastJoint(rope);
        bodies.removeIndex(bodies.size-1);

        float x0 = rope.getPosition().x;
        float y0 = rope.getPosition().y;

        // Create the extra plank
        planksize.x = linksize;
        Vector2 pos = new Vector2();
        nlinks++;

        float t = (nlinks - 1) *(linksize+spacing) + linksize/2.0f;
        // pos.set(norm);
        pos.scl(t);
        pos.add(x0,y0);
        BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
        plank.setName(PLANK_NAME+(nlinks - 1));
        plank.setDensity(BASIC_DENSITY);
        bodies.add(plank);

        t = (nlinks) *(linksize+spacing) + linksize/2.0f;
        // pos.set(norm);
        pos.scl(t);
        pos.add(x0,y0);
        BoxObstacle plank2 = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
        plank2.setName(PLANK_NAME+(nlinks));
        plank2.setDensity(BASIC_DENSITY);
        bodies.add(plank2);



        //Create extra plank joints
        Vector2 anchor1 = new Vector2();
        Vector2 anchor2 = new Vector2(-linksize / 2, 0);
        anchor1.x = linksize / 2;

        RevoluteJointDef jointDef = new RevoluteJointDef();

        jointDef.bodyA = bodies.get(bodies.size-2).getBody();
        jointDef.bodyB = bodies.get(bodies.size-1).getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        Joint joint = world.createJoint(jointDef);
        joints.add(joint);


        anchor2.x = 0;
        jointDef.bodyA = bodies.get(bodies.size - 1).getBody();
        jointDef.bodyB = avatar2.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        joint = world.createJoint(jointDef);
        joints.add(joint);

        length = nlinks * linksize + nlinks * spacing;
    }

    /**
     * Extends the rope
     *
     * @param isAvatar2 Whether avatar2 is anchored. True for avatar2, false for avatar
     * @param world this world
     */
    public void extendRope(boolean isAvatar2, World world, TextureRegion ropeTexture) {
        this.isAvatar2 = isAvatar2;

        Joint lastJoint;
        BoxObstacle lastPlank;
        if (isAvatar2) {
            lastJoint = joints.get(joints.size - 1);
            lastPlank = (BoxObstacle) bodies.get(bodies.size - 1);
            astroCache = avatar2;
        }
        else {
            lastJoint = joints.get(0);
            lastPlank = (BoxObstacle) bodies.get(0);
            astroCache = avatar;
        }

        // Destroy the last joint
        if (!joints.removeValue(lastJoint, true)) { System.out.println("lastJoint wasn't removed from joints"); }
        world.destroyJoint(lastJoint);

        //Calculate position of end of lastPlank
        float angle = lastPlank.getAngle();
        Vector2 endPoint = new Vector2(1, (float)Math.tan(angle));
        endPoint.setLength(linksize/2);

        //Make the new plank
        Vector2 pos = new Vector2(lastPlank.getPosition().x + endPoint.x + linksize/2,
                lastPlank.getPosition().y + endPoint.y);
        BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
        if (isAvatar2)
            plank.setName(PLANK_NAME+bodies.size);
        else
            plank.setName(PLANK_NAME + (-bodies.size));
        plank.setDensity(BASIC_DENSITY);
        if (isAvatar2)
            bodies.add(plank);
        else
            bodies.insert(0, plank);
        plank.activatePhysics(world);
        plank.setTexture(ropeTexture);
        Vector2 plankPos = plank.getPosition();

        //Update nlinks and length
        nlinks++;
        this.length = nlinks * linksize + nlinks * spacing;

        Vector2 anchor1; Vector2 anchor2;
        if (isAvatar2) {
            anchor1 = new Vector2(linksize / 2, 0);
            anchor2 = new Vector2(-linksize / 2, 0);
        }
        else {
            anchor1 = new Vector2(-linksize/2, 0);
            anchor2 = new Vector2(linksize/2, 0);
        }
        RevoluteJointDef jointDef = new RevoluteJointDef();
        RopeJointDef ropeJointDef = new RopeJointDef();

        //Connect new plank to last plank
        jointDef.bodyA = lastPlank.getBody();
        jointDef.bodyB = plank.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
        if (isAvatar2)
            joints.add(joint);
        else
            joints.insert(0, joint);
//        ropeJointDef.bodyA = this.avatar.getBody();
//        ropeJointDef.bodyB = plank.getBody();
//        ropeJointDef.localAnchorA.set(reset);
//        ropeJointDef.localAnchorB.set(anchor2);
//        ropeJointDef.collideConnected = false;
//        ropeJointDef.maxLength = nlinks*linksize + linksize/2; //(nLinks-1)*linksize
//        joint = world.createJoint(ropeJointDef);
//        joints.add(joint);

        //Connect new plank to astronaut
        //anchor1.x = linksize/2; anchor1.y = 0;
        anchor2.x = 0; anchor2.y = 0;
        jointDef.bodyA = plank.getBody();
        jointDef.bodyB = astroCache.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        joint = world.createJoint(jointDef);
        if (isAvatar2)
            joints.add(joint);
        else
            joints.insert(0, joint);
    }

    /**
     * Shortens the rope by n links, applys a force to shortened side
     *
     * @param isAvatar2 Is avatar2 the side to be shortened
     * @param world The world
     */
    public void shortenRope(boolean isAvatar2, AstronautModel other, World world, int n) {
        if (isAvatar2)
            astroCache = avatar2;
        else
            astroCache = avatar;

        if (nlinks - n < initLinks)
            n = nlinks - initLinks;

        //Remove the last two joints
        for (int i = 0; i <= n; i++) {
            if (isAvatar2)
                world.destroyJoint(joints.removeIndex(joints.size-1));
            else
                world.destroyJoint(joints.removeIndex(0));
        }

        //Remove the last plank
        for (int i = 0; i < n; i++) {
            if (isAvatar2)
                bodies.removeIndex(bodies.size-1).deactivatePhysics(world);
            else
                bodies.removeIndex(0).deactivatePhysics(world);
        }

        //Update nlinks and length
        nlinks = nlinks - n;
        this.length = nlinks * linksize + nlinks * spacing;


        //Create a new joint & reattach astronaut
        Vector2 anchor1 = new Vector2(linksize/2, 0);
        Vector2 anchor2 = new Vector2(0, 0);
        if(!isAvatar2)
            anchor1.x = -linksize/2;
        RevoluteJointDef jointDef = new RevoluteJointDef();
        BoxObstacle lastPlank = (BoxObstacle)bodies.get(bodies.size-1);
        if (!isAvatar2)
            lastPlank = (BoxObstacle)bodies.get(0);
        //astroCache.setPosition(lastPlank.getPosition());

        jointDef.bodyA = lastPlank.getBody();
        jointDef.bodyB = astroCache.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        Joint joint = world.createJoint(jointDef);
        if (isAvatar2)
            joints.add(joint);
        else
            joints.insert(0, joint);

        Vector2 force = other.getPosition().cpy().sub(astroCache.getPosition());
        force.setLength(6);
        if (astroCache.getOnPlanet())
            astroCache.setOnPlanet(false);
        astroCache.getBody().applyForceToCenter(force, true);
    }

    /**
     * Destroys the physics Body(s) of this object if applicable,
     * removing them from the world.
     *
     * @param world Box2D world that stores body
     */
    public void deactivatePhysics(World world) {
        super.deactivatePhysics(world);
        if (start != null) {
            start.deactivatePhysics(world);
        }
        if (finish != null) {
            finish.deactivatePhysics(world);
        }
    }

    /**
     * Sets the texture for the individual planks
     *
     * @param texture the texture for the individual planks
     */
    public void setTexture(TextureRegion texture) {
        for(Obstacle body : bodies) {
            ((SimpleObstacle)body).setTexture(texture);
        }
    }

    /**
     * Returns the texture for the individual planks
     *
     * @return the texture for the individual planks
     */
    public TextureRegion getTexture() {
        if (bodies.size == 0) {
            return null;
        }
        return ((SimpleObstacle)bodies.get(0)).getTexture();
    }

    public Vector2[] getVertices() {
        Vector2[] vertices = new Vector2[bodies.size + 1]; //Number of planks - 1 to only get in between the planks, + 2 get each end
        vertices[0] = avatar.getCurAnchor().getPosition();
        vertices[bodies.size] = avatar2.getCurAnchor().getPosition();
        for (int i = 0; i < bodies.size - 1; i++) {
            int j = i + 1; // position in vertices array
            BoxObstacle plank0 = (BoxObstacle)bodies.get(i);
            BoxObstacle plank1 = (BoxObstacle)bodies.get(i + 1);
            Vector2 dir = plank1.getPosition().cpy().sub(plank0.getPosition());
            dir.scl(0.5f);
            vertices[j] = plank0.getPosition().cpy().add(dir);

        }
        return vertices;
    }

    /**
     * The rope is stretched if a joint in the middle of the rope has a reaction force greater than 10.
     *
     * @return True if rope is completely stretched, false otherwise
     */
    public boolean stretched(float dt) {
        int index = joints.size/2;
        Joint joint = joints.get(index);
        if (joint.getReactionForce(1/dt).len() > 10) return true;
        return false;
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);


        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));


        //Write width

        json.addChild("rope width", new JsonValue(dimension.x));

        //System.out.println(json);

        return json;
    }


    public ObstacleType getType() { return ObstacleType.ROPE;}

    public boolean containsPoint(Vector2 point) {
        return false; //TODO Change later
    }
}