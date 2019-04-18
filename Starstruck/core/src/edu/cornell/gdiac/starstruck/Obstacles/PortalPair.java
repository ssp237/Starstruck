package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.starstruck.Models.*;

public class PortalPair {

    /** The connected portals */
    private Portal portal1;
    private Portal portal2;
    /** The joints connecting each end of rope to portal */
    private Joint joint1;
    private Joint joint2;
    Array<Joint> joints;
    /** Whether this portal is "active" -- astronaut is in the process of going through portals */
    private boolean active;
    /** Name of this portal pair */
    private String portalName;
    private TextureRegion textureRegion;

    private PortalPair(float width, float height, float p1x, float p1y, float p2x, float p2y) {
        portal1 = new Portal(p1x, p1y, width, height, 1);
        portal2 = new Portal(p2x, p2y, width, height, 2);
        portal1.setBodyType(BodyDef.BodyType.StaticBody);
        portal2.setBodyType(BodyDef.BodyType.StaticBody);
        active = false;
    }

    public PortalPair(float width, float height, float p1x, float p1y, float p2x, float p2y, String name, Vector2 scale) {
        this(width, height, p1x, p1y, p2x, p2y);
        portal1.setPortName(name);
        portal1.setName(name + "1");
        portal2.setPortName(name);
        portal2.setName(name + "2");
        portal1.setDrawScale(scale);
        portal2.setDrawScale(scale);
        portalName = name;
    }

//    public PortalPair(float width, float height, float p1x, float p1y, float p2x, float p2y, String name, Vector2 scale, TextureRegion texture) {
//        this(width, height, p1x, p1y, p2x, p2y, name, scale);
//        portal1.setTexture(texture);
//        portal2.setTexture(texture);
//    }

    public PortalPair(float p1x, float p1y, float p2x, float p2y, String name, Vector2 scale, TextureRegion texture) {
        this(texture.getRegionWidth()/scale.x, texture.getRegionHeight()/scale.y, p1x, p1y, p2x, p2y, name, scale);
        portal1.setTexture(texture);
        portal2.setTexture(texture);
        setTexture(texture);
    }

    public String getPortalName() { return portalName; }

    public Portal getPortal1() { return portal1; }

    public Portal getPortal2() { return portal2; }


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
     *
     * @param world
     * @param avatar The avatar that collided with the portal
     * @param rope
     */
    public void teleport(World world, AstronautModel avatar, Rope rope) {
        active = !active;

        if (dist(portal1.getPosition(), avatar.getPosition()) < dist(portal2.getPosition(), avatar.getPosition()))
            teleportHelper(world, avatar, rope, portal1, portal2);
        else
            teleportHelper(world, avatar, rope, portal2, portal1);


    }

    public void teleportHelper (World world, AstronautModel avatar, Rope rope, Portal thisPortal, Portal otherPortal) {
        avatar.setPosition(otherPortal.getPosition().cpy().add(new Vector2(3, 0)));
        avatar.setLinearVelocity(avatar.lastVel);
        if (active) { //This case should always happen before !active
            //Split rope
            joints = rope.split(world, avatar.getName().equals("avatar2"), thisPortal, otherPortal);
            joint1 = joints.get(0);
            joint2 = joints.get(1);

        }
        if (!active) { //Joint should be set by now
            //reconnect rope
            rope.reconnect(world, avatar.getName().contains("avatar2"), joint1, joint2);
        }
    }

    private TextureRegion getTexture() {
        return textureRegion;
    }

    private void setTexture(TextureRegion texture) {
        textureRegion = texture;
    }

    /**
     * Return a new anchor with parameters specified by the JSON
     * @param json A JSON containing data for one anchor
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static PortalPair fromJSON(JsonValue json, Vector2 scale) {
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        String name = json.get("name").asString();
        PortalPair out =  new PortalPair(json.get("x1").asFloat(), json.get("y1").asFloat(), json.get("x2").asFloat(), json.get("y2").asFloat(), name, scale, texture);
        return out;
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write position
        Vector2 p1 = portal1.getPosition();
        Vector2 p2 = portal2.getPosition();
        json.addChild("x1", new JsonValue(p1.x));
        json.addChild("y1", new JsonValue(p1.y));
        json.addChild("x2", new JsonValue(p2.x));
        json.addChild("y2", new JsonValue(p2.y));

        //Write name
        json.addChild("name", new JsonValue(getPortalName()));

        //Write color TODO
        json.addChild("color", new JsonValue("blue"));

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));

        //System.out.println(json);

        return json;
    }
}
