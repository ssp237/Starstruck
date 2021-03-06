package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.starstruck.Models.*;
import com.badlogic.gdx.graphics.Color;

public class PortalPair {

    private static final float PORTAL_SPEED = 10f;
    private static final int NUM_COLORS = 2;

    /** The connected portals, should not change */
    private Portal portal1;
    private Portal portal2;
    /** The current leading and trailing portals, should change */
    public Portal leadPortal;
    public Portal trailPortal;
    /** The joints connecting each end of rope to portal */
    private Joint joint1;
    private Joint joint2;
    Array<Joint> joints;
    /** Whether this portal is "active" -- astronaut is in the process of going through portals */
    private boolean active;
    /** Name of this portal pair */
    private String portalName;
    private FilmStrip texture;
    /** Draw scale */
    private Vector2 scale;
    /** integer code for this color */
    private int color;
    /** Is this the goal */
    private boolean goal;

    private PortalPair(float width, float height, float p1x, float p1y, float p2x, float p2y, boolean goal) {
        portal1 = new Portal(p1x, p1y, width, height, 1, goal);
        if (goal)
            portal2 = new Portal(200, 200, width, height, 2, goal);
        else
            portal2 = new Portal(p2x, p2y, width, height, 2, goal);
        portal1.setBodyType(BodyDef.BodyType.StaticBody);
        portal2.setBodyType(BodyDef.BodyType.StaticBody);
        active = false;
    }

    public PortalPair(float width, float height, float p1x, float p1y, float p2x, float p2y, String name, Vector2 scale, boolean goal) {
        this(width, height, p1x, p1y, p2x, p2y, goal);
        portal1.setPortName(name);
        portal1.setName(name + "1");
        portal2.setPortName(name);
        portal2.setName(name + "2");
        portal1.setDrawScale(scale);
        portal2.setDrawScale(scale);
        portalName = name;
        this.scale = scale;
    }

//    public PortalPair(float width, float height, float p1x, float p1y, float p2x, float p2y, String name, Vector2 scale, TextureRegion texture) {
//        this(width, height, p1x, p1y, p2x, p2y, name, scale);
//        portal1.setTexture(texture);
//        portal2.setTexture(texture);
//    }

    public PortalPair(float p1x, float p1y, float p2x, float p2y, String name, Vector2 scale, FilmStrip texture, int color, boolean goal) {
        this(texture.getRegionWidth()/scale.x, texture.getRegionHeight()/scale.y, p1x, p1y, p2x, p2y, name, scale, goal);
        portal1.setTexture(texture);
        portal2.setTexture(texture);
        this.color = color;
        portal1.setColor(portalColor(color));
        portal2.setColor(portalColor(color));
        setTexture(texture);
        this.goal = goal;
    }

    public String getPortalName() { return portalName; }

    public Portal getPortal1() { return portal1; }

    public Portal getPortal2() { return portal2; }

    public boolean isActive() { return active; }

    public boolean isGoal() { return goal; }


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
        leadPortal = otherPortal;
        trailPortal = thisPortal;
        Vector2 dir = new Vector2(avatar.lastVel);
        dir.setLength(getTexture().getRegionWidth()/2/scale.x);
        avatar.setPosition(otherPortal.getPosition().cpy().add(dir));
        avatar.portalVel.set(avatar.lastVel);
        //avatar.portalVel.setLength(PORTAL_SPEED);
        if (active) { //This case should always happen before !active
            //Split rope
            joints = rope.split(world, avatar.getName().equals("avatar2"), thisPortal, otherPortal);
            joint1 = joints.get(0);
            joint2 = joints.get(1);
            avatar.setPosition(otherPortal.getPosition().cpy().add(dir));
            avatar.setLinearVelocity(avatar.portalVel);
        }
        if (!active) { //Joint should be set by now
            //reconnect rope
            rope.reconnect(world, avatar.getName().contains("avatar2"), joint1, joint2, otherPortal.getPosition());
            avatar.setPosition(otherPortal.getPosition().cpy().add(dir));
            avatar.setLinearVelocity(avatar.portalVel.scl(0.5f));
        }
    }

    private FilmStrip getTexture() {
        return texture;
    }

    private void setTexture(FilmStrip texture) {
        this.texture = texture;
    }

    /**
     * 0: White, default
     * 1: Light blue
     * 2: Violet
     * 3: Salmon
     *
     * @param color Number code for color
     * @return The color
     */
    private Color portalColor(int color) {
        if (color == 0) return Color.WHITE;
        if (color == 1) return Color.SKY;
        if (color == 2) return Color.VIOLET;
        if (color == 3) return Color.SALMON;
        return Color.WHITE;
    }

    public int nextColor() {
        int result = color + 1;
        if (result > NUM_COLORS) result = 0;
        return result;
    }

    /**
     * Return a new anchor with parameters specified by the JSON
     * @param json A JSON containing data for one portal pair
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static PortalPair fromJSON(JsonValue json, Vector2 scale) {
        String key = json.get("texture").asString();
        FilmStrip texture = JsonAssetManager.getInstance().getEntry(key, FilmStrip.class);
        String name = json.get("name").asString();
        int color = json.get("color").asInt();
        String goal = json.get("goal").asString();
        boolean isGoal = false;
        if (goal.equals("yes")) isGoal = true;
        PortalPair out =  new PortalPair(json.get("x1").asFloat(), json.get("y1").asFloat(), json.get("x2").asFloat(), json.get("y2").asFloat(), name, scale, texture, color, isGoal);
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

        //Write color
        json.addChild("color", new JsonValue(color + ""));

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));

        //Is portal?
        String goal = "no";
        if (isGoal())
            goal = "yes";
        json.addChild("goal", new JsonValue(goal));

        //System.out.println(json);

        return json;
    }
}
