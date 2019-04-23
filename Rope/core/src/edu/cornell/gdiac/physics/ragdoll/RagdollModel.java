/*
 * RagdollModel.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.ragdoll;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.graphics.g2d.*;

import edu.cornell.gdiac.physics.obstacle.*;

/**
 * A ragdoll whose body parts are boxes connected by joints
 *
 * This class has several bodies connected by joints.  For information on how
 * the joints fit together, see the ragdoll diagram at the start of the class.
 * 
 * 
 */
public class RagdollModel extends ComplexObstacle {

	/** Indices for the body parts in the bodies array */
	private static final int PART_NONE = -1;
	private static final int PART_BODY = 0;
	private static final int PART_HEAD = 1;
	private static final int PART_LEFT_ARM  = 2;
	private static final int PART_RIGHT_ARM = 3;
	private static final int PART_LEFT_FOREARM  = 4;
	private static final int PART_RIGHT_FOREARM = 5;
	private static final int PART_LEFT_THIGH  = 6;
	private static final int PART_RIGHT_THIGH = 7;
	private static final int PART_LEFT_SHIN  = 8;
	private static final int PART_RIGHT_SHIN = 9;
	
	/** The number of DISTINCT body parts */
	private static final int BODY_TEXTURE_COUNT = 6;

	/**
	 * Returns the texture index for the given body part 
	 *
	 * As some body parts are symmetrical, we reuse textures.
	 *
	 * @returns the texture index for the given body part 
	 */
	private static int partToAsset(int part) {
		switch (part) {
		case PART_BODY:
			return 0;
		case PART_HEAD:
			return 1;
		case PART_LEFT_ARM:
		case PART_RIGHT_ARM:
			return 2;
		case PART_LEFT_FOREARM:
		case PART_RIGHT_FOREARM:
			return 3;
		case PART_LEFT_THIGH:
		case PART_RIGHT_THIGH:
			return 4;
		case PART_LEFT_SHIN:
		case PART_RIGHT_SHIN:
			return 5;
		default:
			return -1;
		}
	}
		
	// Layout of ragdoll
	//
	// o = joint
	//                   ___
	//                  |   |
	//                  |_ _|
	//   ______ ______ ___o___ ______ ______
	//  |______o______o       o______o______|
	//                |       |
	//                |       |
	//                |_______|
	//                | o | o |
	//                |   |   |
	//                |___|___|
	//                | o | o |
	//                |   |   |
	//                |   |   |
	//                |___|___|
	//
	/** Distance between torso center and face center */
	private static final float TORSO_OFFSET   = 3.8f;
	/** Y-distance between torso center and arm center */
	private static final float ARM_YOFFSET    = 1.75f;  
	/** X-distance between torso center and arm center */
	private static final float ARM_XOFFSET    = 3.15f;  
	/** Distance between center of arm and center of forearm */
	private static final float FOREARM_OFFSET = 2.75f; 
	/** X-distance from center of torso to center of leg */
	private static final float THIGH_XOFFSET  = 0.75f;  
	/** Y-distance from center of torso to center of thigh */
	private static final float THIGH_YOFFSET  = 3.5f;  
	/** Distance between center of thigh and center of shin */
	private static final float SHIN_OFFSET    = 2.75f;
	/** The offset of the bubbler from the head center */
	private Vector2 BUBB_OFF = new Vector2(0.55f,  1.9f);

	
	/** The density for each body part */
	private static final float DENSITY = 1.0f;

    /** Bubble generator to glue to snorkler. */
    private BubbleGenerator bubbler;
    
	/** Texture assets for the body parts */
	private TextureRegion[] partTextures;	

	/** Cache vector for organizing body parts */
	private Vector2 partCache = new Vector2();

	/**
	 * Creates a new ragdoll with its root at the origin.
	 * 
	 * The root is NOT a body part.  It is simply a body that 
	 * is unconnected to the rest of the object.  
	 */
	public RagdollModel() {
		this(0,0);
	}

	/**
	 * Creates a new ragdoll with its head at the given position.
	 *
	 * @param x  Initial x position of the ragdoll head
	 * @param y  Initial y position of the ragdoll head
	 */
	public RagdollModel(float x, float y) {
		super(x,y);
		
		float ox = BUBB_OFF.x+x;
		float oy = BUBB_OFF.y+y;
	    bubbler = new BubbleGenerator(ox, oy);
	}
	
	protected void init() {
		// We do not do anything yet.
		BoxObstacle part;
		
		// TORSO
	    part = makePart(PART_BODY, PART_NONE, getX(), getY());
	    part.setFixedRotation(true);
		
		// HEAD
		makePart(PART_HEAD, PART_BODY, 0, TORSO_OFFSET);
		
		// ARMS
		makePart(PART_LEFT_ARM, PART_BODY, -ARM_XOFFSET, ARM_YOFFSET);
		part = makePart(PART_RIGHT_ARM, PART_BODY, ARM_XOFFSET, ARM_YOFFSET);
		part.setAngle((float)Math.PI);
		
		// FOREARMS
		makePart(PART_LEFT_FOREARM, PART_LEFT_ARM, -FOREARM_OFFSET, 0);
		part = makePart(PART_RIGHT_FOREARM, PART_RIGHT_ARM, FOREARM_OFFSET, 0);
		part.setAngle((float)Math.PI);
		
		// THIGHS
		makePart(PART_LEFT_THIGH, PART_BODY, -THIGH_XOFFSET, -THIGH_YOFFSET);
		makePart(PART_RIGHT_THIGH, PART_BODY, THIGH_XOFFSET, -THIGH_YOFFSET);
		
		// SHINS
		makePart(PART_LEFT_SHIN,  PART_LEFT_THIGH, 0, -SHIN_OFFSET);
		makePart(PART_RIGHT_SHIN, PART_RIGHT_THIGH, 0, -SHIN_OFFSET);
		
		bubbler.setDrawScale(drawScale);
		bodies.add(bubbler);
	}
	
    /**
     * Sets the drawing scale for this physics object
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param x  the x-axis scale for this physics object
     * @param y  the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y) {
    	super.setDrawScale(x,y);
    	
    	if (partTextures != null && bodies.size == 0) {
    		init();
    	}
    }
    
    /**
     * Sets the array of textures for the individual body parts.
     *
     * The array should be BODY_TEXTURE_COUNT in size.
     *
     * @param textures the array of textures for the individual body parts.
     */
    public void setPartTextures(TextureRegion[] textures) {
    	assert textures != null && textures.length > BODY_TEXTURE_COUNT : "Texture array is not large enough";
    	
    	partTextures = new TextureRegion[BODY_TEXTURE_COUNT];
    	System.arraycopy(textures, 0, partTextures, 0, BODY_TEXTURE_COUNT);
    	if (bodies.size == 0) {
    		init();
    	} else {
    		for(int ii = 0; ii <= PART_RIGHT_SHIN; ii++) {
    			((SimpleObstacle)bodies.get(ii)).setTexture(partTextures[partToAsset(ii)]);
    		}
    	}
    }
    
	/**
     * Returns the array of textures for the individual body parts.
     *
     * Modifying this array will have no affect on the physics objects.
     *
     * @return the array of textures for the individual body parts.
     */
    public TextureRegion[] getPartTextures() {
    	return partTextures;
    }
    
    /**
     * Returns the bubble generator welded to the mask
     *
     * @return the bubble generator welded to the mask
     */
    public BubbleGenerator getBubbleGenerator() {
    	return bubbler;
    }

	/**
	 * Helper method to make a single body part
	 * 
	 * While it looks like this method "connects" the pieces, it does not really.  It 
	 * puts them in position to be connected by joints, but they will fall apart unless 
	 * you make the joints.
	 * 
	 * @param part		The part to make
	 * @param connect	The part to connect to
	 * @param x 		The x-offset RELATIVE to the connecting part
	 * @param y			The y-offset RELATIVE to the connecting part
	 * 
	 * @return the newly created part
	 */
	private BoxObstacle makePart(int part, int connect, float x, float y) {
		TextureRegion texture = partTextures[partToAsset(part)];
		
		partCache.set(x,y);
		if (connect != PART_NONE) {
			partCache.add(bodies.get(connect).getPosition());
		}
		
		float dwidth  = texture.getRegionWidth()/drawScale.x;
		float dheight = texture.getRegionHeight()/drawScale.y;

		BoxObstacle body = new BoxObstacle(partCache.x, partCache.y, dwidth, dheight);
		body.setDrawScale(drawScale);
		body.setTexture(texture);
		body.setDensity(DENSITY);
		bodies.add(body);
		return body;
	}

	/**
	 * Creates the joints for this object.
	 * 
	 * We implement our custom logic here.
	 *
	 * @param world Box2D world to store joints
	 *
	 * @return true if object allocation succeeded
	 */
	protected boolean createJoints(World world) {
		assert bodies.size > 0;


		//#region INSERT CODE HERE
		// Implement all of the Ragdoll Joints here
		RevoluteJointDef jointDefLeftElbow = new RevoluteJointDef();


		// Initial joint
		jointDefLeftElbow.bodyA = bodies.get(PART_LEFT_ARM).getBody();
		jointDefLeftElbow.bodyB = bodies.get(PART_LEFT_FOREARM).getBody();
		jointDefLeftElbow.localAnchorA.set(- FOREARM_OFFSET/2, 0);
		jointDefLeftElbow.localAnchorB.set(FOREARM_OFFSET/2, 0);
		jointDefLeftElbow.collideConnected = false;
		Joint joint = world.createJoint(jointDefLeftElbow);
		joints.add(joint);

		RevoluteJointDef jointDefRightElbow = new RevoluteJointDef();


		jointDefRightElbow.bodyA = bodies.get(PART_RIGHT_ARM).getBody();
		jointDefRightElbow.bodyB = bodies.get(PART_RIGHT_FOREARM).getBody();
		jointDefRightElbow.localAnchorA.set(-FOREARM_OFFSET/2, 0);
		jointDefRightElbow.localAnchorB.set(FOREARM_OFFSET/2, 0);
		jointDefRightElbow.collideConnected = false;
		joint = world.createJoint(jointDefRightElbow);
		joints.add(joint);


		RevoluteJointDef jointDefRightKnee = new RevoluteJointDef();

		jointDefRightKnee.bodyA = bodies.get(PART_RIGHT_THIGH).getBody();
		jointDefRightKnee.bodyB = bodies.get(PART_RIGHT_SHIN).getBody();
		jointDefRightKnee.localAnchorA.set(0, -SHIN_OFFSET/2);
		jointDefRightKnee.localAnchorB.set(0, SHIN_OFFSET/2);
		jointDefRightKnee.collideConnected = false;
		joint = world.createJoint(jointDefRightKnee);
		joints.add(joint);


		RevoluteJointDef jointDefLeftKnee = new RevoluteJointDef();

		jointDefLeftKnee.bodyA = bodies.get(PART_LEFT_THIGH).getBody();
		jointDefLeftKnee.bodyB = bodies.get(PART_LEFT_SHIN).getBody();
		jointDefLeftKnee.localAnchorA.set(0, -SHIN_OFFSET/2);
		jointDefLeftKnee.localAnchorB.set(0, SHIN_OFFSET/2);
		jointDefLeftKnee.collideConnected = false;
		joint = world.createJoint(jointDefLeftKnee);
		joints.add(joint);

		RevoluteJointDef jointDefRightHip = new RevoluteJointDef();

		jointDefRightHip.bodyA = bodies.get(PART_RIGHT_THIGH).getBody();
		jointDefRightHip.bodyB = bodies.get(PART_BODY).getBody();
		jointDefRightHip.localAnchorA.set(0, THIGH_YOFFSET/2);
		jointDefRightHip.localAnchorB.set(THIGH_XOFFSET, -THIGH_YOFFSET/2);
		jointDefRightHip.collideConnected = false;
		joint = world.createJoint(jointDefRightHip);
		joints.add(joint);

		RevoluteJointDef jointDefLeftHip = new RevoluteJointDef();

		jointDefLeftHip.bodyA = bodies.get(PART_LEFT_THIGH).getBody();
		jointDefLeftHip.bodyB = bodies.get(PART_BODY).getBody();
		jointDefLeftHip.localAnchorA.set(0, THIGH_YOFFSET/2);
		jointDefLeftHip.localAnchorB.set(-THIGH_XOFFSET, -THIGH_YOFFSET/2);
		jointDefLeftHip.collideConnected = false;
		joint = world.createJoint(jointDefLeftHip);
		joints.add(joint);


		RevoluteJointDef jointDefRightShoulder = new RevoluteJointDef();

		jointDefRightShoulder.bodyA = bodies.get(PART_RIGHT_ARM).getBody();
		jointDefRightShoulder.bodyB = bodies.get(PART_BODY).getBody();
		jointDefRightShoulder.localAnchorA.set(ARM_XOFFSET/2, 0);
		jointDefRightShoulder.localAnchorB.set(ARM_XOFFSET/2, ARM_YOFFSET);
		jointDefRightShoulder.collideConnected = false;
		joint = world.createJoint(jointDefRightShoulder);
		joints.add(joint);

		RevoluteJointDef jointDefLeftShoulder = new RevoluteJointDef();

		jointDefLeftShoulder.bodyA = bodies.get(PART_LEFT_ARM).getBody();
		jointDefLeftShoulder.bodyB = bodies.get(PART_BODY).getBody();
		jointDefLeftShoulder.localAnchorA.set(ARM_XOFFSET/2, 0);
		jointDefLeftShoulder.localAnchorB.set(-ARM_XOFFSET /2, ARM_YOFFSET);
		jointDefLeftShoulder.collideConnected = false;
		joint = world.createJoint(jointDefLeftShoulder);
		joints.add(joint);

		RevoluteJointDef jointDefNeck = new RevoluteJointDef();

		jointDefNeck.bodyA = bodies.get(PART_HEAD).getBody();
		jointDefNeck.bodyB = bodies.get(PART_BODY).getBody();
		jointDefNeck.localAnchorA.set(0, -TORSO_OFFSET/2);
		jointDefNeck.localAnchorB.set(0, TORSO_OFFSET/2);
		jointDefNeck.collideConnected = false;
		joint = world.createJoint(jointDefNeck);
		joints.add(joint);

		// You may add additional methods if you find them useful

		//#endregion
		
		
		// Weld the bubbler to this mask
		WeldJointDef weldDef = new WeldJointDef();
		weldDef.bodyA = bodies.get(PART_HEAD).getBody();
		weldDef.bodyB = bubbler.getBody();
		weldDef.localAnchorA.set(BUBB_OFF);
		weldDef.localAnchorB.set(0,0);
		Joint wjoint = world.createJoint(weldDef);
		joints.add(wjoint);

		return true;
	}
}