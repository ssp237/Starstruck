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

import edu.cornell.gdiac.starstruck.*;

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
    protected float linksize = 1.0f;
    /** The spacing between each link */
    protected float spacing = 0.0f;
    /** Lengh of the rope */
    protected float length;
    /** Number of links in the rope */
    private int nLinks;
    /** Original number of links in the rope */
    private int initLinks;
    /** List of planks that make up this rope */

    private AstronautModel avatar;
    private AstronautModel avatar2;

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
        nLinks = (int)(length / linksize);
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

        this.length = nLinks * linksize + nLinks * spacing;
        initLinks = nLinks;
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
        ropeJointDef.bodyA = avatar.getBody();
        ropeJointDef.bodyB = bodies.get(0).getBody();
        ropeJointDef.localAnchorA.set(anchor1);
        ropeJointDef.localAnchorB.set(anchor2);
        ropeJointDef.collideConnected = false;
        ropeJointDef.maxLength = linksize/2;
        joint = world.createJoint(ropeJointDef);
        joints.add(joint);
        ropeJointDef.bodyA = avatar2.getBody();
        ropeJointDef.bodyB = bodies.get(0).getBody();
        ropeJointDef.localAnchorA.set(anchor1);
        ropeJointDef.localAnchorB.set(anchor2);
        ropeJointDef.collideConnected = false;
        ropeJointDef.maxLength = linksize*nLinks;
        joint = world.createJoint(ropeJointDef);
        joints.add(joint);


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
            ropeJointDef.bodyA = avatar.getBody();
            ropeJointDef.bodyB = bodies.get(ii+1).getBody();
            ropeJointDef.localAnchorA.set(reset);
            ropeJointDef.localAnchorB.set(anchor2);
            //ropeJointDef.collideConnected = false;
            ropeJointDef.maxLength = linksize*(ii+1) + linksize/2;
            joint = world.createJoint(ropeJointDef);
            joints.add(joint);
            ropeJointDef.bodyA = avatar2.getBody();
            ropeJointDef.bodyB = bodies.get(ii+1).getBody();
            ropeJointDef.localAnchorA.set(reset);
            ropeJointDef.localAnchorB.set(anchor2);
            //ropeJointDef.collideConnected = false;
            ropeJointDef.maxLength = (length - linksize*(ii+1)) + linksize/2;
            joint = world.createJoint(ropeJointDef);
            joints.add(joint);

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

        return true;
    }

    public float getLength() { return length; }

    public int nLinks(){ return nLinks; }

    //public void setLength(int newLength) {length = newLength;}


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
        nLinks++;

        float t = (nLinks - 1) *(linksize+spacing) + linksize/2.0f;
        // pos.set(norm);
        pos.scl(t);
        pos.add(x0,y0);
        BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
        plank.setName(PLANK_NAME+(nLinks - 1));
        plank.setDensity(BASIC_DENSITY);
        bodies.add(plank);

        t = (nLinks) *(linksize+spacing) + linksize/2.0f;
        // pos.set(norm);
        pos.scl(t);
        pos.add(x0,y0);
        BoxObstacle plank2 = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
        plank2.setName(PLANK_NAME+(nLinks));
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

        length = nLinks * linksize + nLinks * spacing;
    }

    public void extendRope(AstronautModel avatar, World world, TextureRegion ropeTexture) {
//        System.out.println(bodies.size);
//        System.out.println(planks.size());

        Joint lastJoint = joints.get(joints.size-1);
        //Joint lastRopeJoint = joints.get(joints.size-1);
        BoxObstacle lastPlank = (BoxObstacle)bodies.get(bodies.size-1);

        // Destroy the last joint
        if (!joints.removeValue(lastJoint, true)) System.out.println("lastJoint wasn't removed from joints");
        //if (!joints.removeValue(lastRopeJoint, true)) System.out.println("lastJoint wasn't removed from joints");
        world.destroyJoint(lastJoint);
        //world.destroyJoint(lastRopeJoint);

        //Calculate position of end of lastPlank
        float angle = lastPlank.getAngle();
        Vector2 endPoint = new Vector2(1, (float)Math.tan(angle));
        endPoint.setLength(linksize/2);

        //Make the new plank
        Vector2 pos = new Vector2(lastPlank.getPosition().x + endPoint.x + linksize/2,
                lastPlank.getPosition().y + endPoint.y);
        BoxObstacle plank = new BoxObstacle(pos.x, pos.y, planksize.x, planksize.y);
        plank.setName(PLANK_NAME+bodies.size);
        plank.setDensity(BASIC_DENSITY);
        bodies.add(plank);
        plank.activatePhysics(world);
        plank.setTexture(ropeTexture);
        Vector2 plankPos = plank.getPosition();
        System.out.println("Plank Pos = " + plankPos);
        //Update nlinks and length
        nLinks++;
        this.length = nLinks * linksize + nLinks * spacing;

        Vector2 anchor1 = new Vector2(endPoint);
        Vector2 anchor2 = new Vector2(-linksize/2, 0);
        RevoluteJointDef jointDef = new RevoluteJointDef();
        RopeJointDef ropeJointDef = new RopeJointDef();

        //Connect new plank to last plank
        jointDef.bodyA = lastPlank.getBody();
        jointDef.bodyB = plank.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
        joints.add(joint);
        ropeJointDef.bodyA = this.avatar.getBody();
        ropeJointDef.bodyB = plank.getBody();
        ropeJointDef.localAnchorA.set(anchor1);
        ropeJointDef.localAnchorB.set(anchor2);
        ropeJointDef.collideConnected = false;
        ropeJointDef.maxLength = nLinks*linksize + linksize/2;
        joint = world.createJoint(ropeJointDef);
        joints.add(joint);
        ropeJointDef.bodyA = this.avatar2.getBody();
        ropeJointDef.bodyB = plank.getBody();
        ropeJointDef.localAnchorA.set(anchor1);
        ropeJointDef.localAnchorB.set(anchor2);
        ropeJointDef.collideConnected = false;
        ropeJointDef.maxLength = (length - nLinks*linksize) + linksize/2;
        joint = world.createJoint(ropeJointDef);
        joints.add(joint);

        //Connect new plank to astronaut
        anchor1.x = linksize/2; anchor1.y = 0;
        anchor2.x = 0; anchor2.y = 0;
        jointDef.bodyA = plank.getBody();
        jointDef.bodyB = avatar2.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        joint = world.createJoint(jointDef);
        joints.add(joint);
//        ropeJointDef.bodyA = plank.getBody();
//        ropeJointDef.bodyB = avatar2.getBody();
//        ropeJointDef.localAnchorA.set(anchor1);
//        ropeJointDef.localAnchorB.set(anchor2);
//        ropeJointDef.collideConnected = false;
//        ropeJointDef.maxLength = MAX_LENGTH;
//        joint = world.createJoint(ropeJointDef);
//        joints.add(joint);

//        Joint ropeJoint = joints.get(0);
//        ((RopeJoint)ropeJoint).setMaxLength(length);
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
}