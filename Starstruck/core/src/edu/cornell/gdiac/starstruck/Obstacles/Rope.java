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
    /** Reel force, taken from game controller */
    private float reel_force;

    private AstronautModel avatar;
    private AstronautModel avatar2;

    /** AstonautModel cache for extending rope */
    private AstronautModel astroCache;
    /** Vector2 cache */
    private Vector2 dirCache = new Vector2();

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
        initPlankSize = bodies.size();

        setReelForce(1.8f);
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
        assert bodies.size() > 0;

        Vector2 anchor1 = new Vector2();
        Vector2 anchor2 = new Vector2(-linksize / 2, 0);

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

        // Link the planks together
        anchor1.x = linksize / 2;
        for (int ii = 0; ii < bodies.size()-1; ii++) {
            // Look at what we did above and join the planks
            jointDef.bodyA = bodies.get(ii).getBody();
            jointDef.bodyB = bodies.get(ii + 1).getBody();
            jointDef.localAnchorA.set(anchor1);
            jointDef.localAnchorB.set(anchor2);
            joint = world.createJoint(jointDef);
            joints.add(joint);

            initJointSize = joints.size();
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
        jointDef.bodyA = bodies.get(bodies.size() - 1).getBody();
        jointDef.bodyB = avatar2.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        joint = world.createJoint(jointDef);
        joints.add(joint);

        return true;
    }

    public float getLength() { return length; }

    public int nLinks(){ return nlinks; }

    public ArrayList<Joint> getJointList() { return joints; }

    public ArrayList<Obstacle> getPlanks() { return bodies; }

    public void setReelForce(float value) { reel_force = value; }

    /**
     * Extends the rope
     *
     * @param isAvatar2 If avatar2 is the one to extend. True for avatar2, false for avatar
     * @param world this world
     */
    public void extendRope(boolean isAvatar2, World world, TextureRegion ropeTexture) {
        this.isAvatar2 = isAvatar2;

        Joint lastJoint;
        BoxObstacle lastPlank;
        if (isAvatar2) {
            lastJoint = joints.get(joints.size() - 1);
            lastPlank = (BoxObstacle) bodies.get(bodies.size() - 1);
            astroCache = avatar2;
        }
        else {
            lastJoint = joints.get(0);
            lastPlank = (BoxObstacle) bodies.get(0);
            astroCache = avatar;
        }

        // Destroy the last joint
        if (!joints.remove(lastJoint)) { System.out.println("lastJoint wasn't removed from joints in extend rope"); }
        world.destroyJoint(lastJoint);

        //Calculate position of end of lastPlank
        float angle = lastPlank.getAngle();
        Vector2 endPoint = new Vector2(1, (float)Math.tan(angle));
        endPoint.setLength(linksize/2);

        //Make the new plank
        Vector2 pos = new Vector2(lastPlank.getPosition().x + endPoint.x + linksize/2,
                lastPlank.getPosition().y + endPoint.y);
        BoxObstacle plank = new BoxObstacle(lastPlank.getPosition().x, lastPlank.getPosition().y, planksize.x, planksize.y);
        if (isAvatar2)
            plank.setName(PLANK_NAME+bodies.size());
        else
            plank.setName(PLANK_NAME + (-bodies.size()));
        plank.setDensity(BASIC_DENSITY);
        if (isAvatar2)
            bodies.add(plank);
        else
            bodies.add(0, plank);
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
            joints.add(0, joint);

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
            joints.add(0, joint);

        if (!astroCache.getOnPlanet() && astroCache.getLinearVelocity().len() != 0)
            astroCache.getBody().applyForceToCenter(astroCache.lastVel, true);
    }

    /**
     * Shortens the rope by n links, applys a force to shortened side
     *
     * @param astro The side to be shortened
     * @param world The world
     */
    public void shortenRope(AstronautModel astro, Vector2 otherPos, World world, int n) {
        boolean isAvatar2 = astro == avatar2;
        if (isAvatar2)
            astroCache = avatar2;
        else
            astroCache = avatar;

        if (nlinks - n < initLinks)
            n = nlinks - initLinks;

        //Remove the last two joints
        for (int i = 0; i <= n; i++) {
            if (isAvatar2)
                world.destroyJoint(joints.remove(joints.size()-1));
            else
                world.destroyJoint(joints.remove(0));
        }

        //Remove the last plank
        for (int i = 0; i < n; i++) {
            if (isAvatar2)
                bodies.remove(bodies.size()-1).deactivatePhysics(world);
            else
                bodies.remove(0).deactivatePhysics(world);
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
        BoxObstacle lastPlank = (BoxObstacle)bodies.get(bodies.size()-1);
        if (!isAvatar2)
            lastPlank = (BoxObstacle)bodies.get(0);

        jointDef.bodyA = lastPlank.getBody();
        jointDef.bodyB = astroCache.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        Joint joint = world.createJoint(jointDef);
        if (isAvatar2)
            joints.add(joint);
        else
            joints.add(0, joint);

        Vector2 force = otherPos.cpy().sub(astroCache.getPosition());
        force.setLength(astroCache.getJumpPulse());
//        if (astroCache.getOnPlanet()) {
//            astroCache.setPosition(lastPlank.getPosition());
//            astroCache.setOnPlanet(false);
//        }
        astroCache.setPosition(lastPlank.getPosition());
        astroCache.setOnPlanet(false);
        astroCache.getBody().applyForceToCenter(force, true);
    }

    /**
     *
     * @param isAvatar2 Is avatar2 the one going through the portal?
     */
    public Array<Joint> split( World world, boolean isAvatar2, Portal portal1, Portal portal2) {
        Joint lastJoint;
        BoxObstacle plank1;
        BoxObstacle plank2;
//        if (isAvatar2) {
//            lastJoint = joints.get(joints.size()-5);
//            plank1 = (BoxObstacle)bodies.get(bodies.size()-5);
//            plank2 = (BoxObstacle)bodies.get(bodies.size()-4);
//        }
//        else {
//            lastJoint = joints.get(4);
//            plank1 = (BoxObstacle)bodies.get(4);
//            plank2 = (BoxObstacle)bodies.get(3);
//        }
        int index = joints.size()/2;
        lastJoint = joints.get(index);
        if (isAvatar2) {
            plank1 = (BoxObstacle)bodies.get(index-1);
            plank2 = (BoxObstacle)bodies.get(index);
        }
        else {
            plank1 = (BoxObstacle)bodies.get(index);
            plank2 = (BoxObstacle)bodies.get(index-1);
        }

        Array<Joint> result = new Array<Joint>(2);
//        result.add(lastJoint);
//        result.add(lastJoint);

        //Destroy joint
        if (!joints.remove(lastJoint)) { System.out.println("lastJoint wasn't removed from joints in split"); }
        world.destroyJoint(lastJoint);

        if (isAvatar2) {
            for (int i = index; i < bodies.size(); i++)
                bodies.get(i).setPosition(portal2.getPosition());
        }
        else {
            for (int i = 0; i < index; i++)
                bodies.get(i).setPosition(portal2.getPosition());
        }

        //Make two new joints
        RevoluteJointDef jointDef = new RevoluteJointDef();
        Vector2 anchor1;
        if (isAvatar2)
            anchor1 = new Vector2(linksize/2, 0);
        else
            anchor1 = new Vector2(-linksize/2, 0);
        Vector2 anchor2 = new Vector2();

        //Connect to portal 1 (trailing end)
        jointDef.bodyA = plank1.getBody();
        jointDef.bodyB = portal1.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
//        if (isAvatar2)
//            joints.add(joints.size()-4, joint);
//        else
//            joints.add(4, joint);
        if (isAvatar2) {
            joints.add(index, joint);
            //result.insert(0, joint);
        }
        else {
            joints.add(index, joint);
            //result.insert(1, joint);
        }

        //Connect to portal 2 (leading end)
        anchor1.x = -anchor1.x;
        jointDef.bodyA = portal2.getBody();
        jointDef.bodyB = plank2.getBody();
        jointDef.localAnchorA.set(anchor2);
        jointDef.localAnchorB.set(anchor1);
        joint = world.createJoint(jointDef);
//        if (isAvatar2)
//            joints.add(joints.size()-4, joint);
//        else
//            joints.add(4, joint);
        if (isAvatar2) {
            joints.add(index + 1, joint);
            //result.insert(1, joint);

        }
        else {
            joints.add(index, joint);
            //result.insert(0, joint);
        }

//        if (isAvatar2) {
//            result.add(joints.get(joints.size()-6));
//            result.add(joints.get(joints.size()-5));
//        }
//        else {
//            result.add(joints.get(4));
//            result.add(joints.get(5));
//        }
        result.add(joints.get(index));
        result.add(joints.get(index+1));
        return result;

    }

    public void reconnect(World world, boolean isAvatar2, Joint joint1, Joint joint2, Vector2 portalPos) {
        int connect = joints.indexOf(joint1);
        if (connect < 0) System.out.println("reconnect method, joint could not be found");

        BoxObstacle plank1 = (BoxObstacle)bodies.get(connect-1);
        BoxObstacle plank2 = (BoxObstacle)bodies.get(connect);

        //Destroy portal joints
        if (!joints.remove(joint1)) { System.out.println("joint1 wasn't removed from joints in reconnect"); }
        world.destroyJoint(joint1);
        if (!joints.remove(joint2)) { System.out.println("joint2 wasn't removed from joints in reconnect"); }
        world.destroyJoint(joint2);

        if (isAvatar2) {
            for (int i = connect; i < bodies.size(); i++)
                bodies.get(i).setPosition(portalPos);
        }
        else {
            for (int i = 0; i < connect; i++) {
                bodies.get(i).setPosition(portalPos);
            }

        }

        //Make new joint & reconnect rope
        RevoluteJointDef jointDef = new RevoluteJointDef();
        Vector2 anchor1 = new Vector2(linksize/2, 0);
        Vector2 anchor2 = new Vector2(-linksize/2, 0);

        jointDef.bodyA = plank1.getBody();
        jointDef.bodyB = plank2.getBody();
        jointDef.localAnchorA.set(anchor1);
        jointDef.localAnchorB.set(anchor2);
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
        joints.add(connect, joint);
    }

    /**
     * Pull the avatar in the direction of the rope
     *
     * @param isAvatar2
     */
    public void reel(boolean isAvatar2) {
        BoxObstacle plank;
        BoxObstacle plank0;
        if (isAvatar2) {
            for (int i = bodies.size()-2; i >= 0; i--) { //i = bodies.size()-2;
                plank = (BoxObstacle)bodies.get(i);
                plank0 = (BoxObstacle)bodies.get(i+1);
                dirCache = plank.getPosition().cpy().sub(plank0.getPosition());
                dirCache.setLength(reel_force);
                plank.getBody().applyForceToCenter(dirCache, true);
                if (i >= bodies.size()/2) i--;
            }
        }
        else {
            for (int i = 1; i < bodies.size()/2; i++) { //i < bodies.size()
                plank = (BoxObstacle) bodies.get(i);
                plank0 = (BoxObstacle) bodies.get(i-1);
                dirCache = plank.getPosition().cpy().sub(plank0.getPosition());
                dirCache.setLength(reel_force);
                plank.getBody().applyForceToCenter(dirCache, true);
                if (i <= bodies.size()/2) i++;
            }
        }
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
        if (bodies.size() == 0) {
            return null;
        }
        return ((SimpleObstacle)bodies.get(0)).getTexture();
    }

    public Vector2[] getVertices() {
        Vector2[] vertices = new Vector2[bodies.size() + 1]; //Number of planks - 1 to only get in between the planks, + 2 get each end
        vertices[0] = avatar.getCurAnchor().getPosition();
        vertices[bodies.size()] = avatar2.getCurAnchor().getPosition();
        for (int i = 0; i < bodies.size() - 1; i++) {
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
     * The rope is stretched if a joint of the rope has a reaction force greater than 10.
     * 0 = avatar1's side
     * 2 = middle of rope
     * 1 = avatar 2's side
     * 3 = all three
     *
     * @return True if rope is completely stretched, false otherwise
     */
    public boolean stretched(float dt, int c) {
        int index = -1;
        if (c == 0)
            index = 1;
        else if (c == 2)
            index = joints.size() - 2;
        else if (c == 3) {
            Joint joint1 = joints.get(1);
            Joint joint2 = joints.get(joints.size()/2);
            Joint joint3 = joints.get(joints.size()-2);
            return joint1.getReactionForce(1/dt).len() > 10 && joint2.getReactionForce(1/dt).len() > 10
                    && joint3.getReactionForce(1/dt).len() > 10;
        }
        else
            index = joints.size()/2;
        if (index < 0) System.out.println("problem with Rope.stretched");
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