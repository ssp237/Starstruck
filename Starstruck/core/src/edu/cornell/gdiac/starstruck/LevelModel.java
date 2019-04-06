/*
 * LevelModel.java
 *
 * This stores all of the information to define a level in our simple platform game.
 * We have an avatar, some walls, some platforms, and an exit.  This is a refactoring
 * of WorldController in Lab 4 that separates the level data from the level control.
 *
 * Note that most of the methods are getters and setters, as is common with models.
 * The gameplay behavior is defined by GameController.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 *
 * Edited by Starstruck
 */
package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.starstruck.Obstacles.*;

import java.util.ArrayList;

/**
 * Represents a single level in our game
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.  To reset a level, dispose it and reread the JSON.
 *
 * The level contains its own Box2d World, as the World settings are defined by the
 * JSON file.  However, there is absolutely no controller code in this class, as
 * the majority of the methods are getters and setters.  The getters allow the
 * GameController class to modify the level elements.
 */
public class LevelModel {

    /** The Box2D world */
    protected World world;
    /** The vector world */
    protected VectorWorld vectorWorld;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** The background texture*/
    protected Texture background;

    // Physics objects for the game
    /** Reference to the first character avatar */
    private AstronautModel player1;
    /** Reference to the second character avatar */
    private AstronautModel player2;
    /** Reference to the list of planets */
    private PlanetList planets;
    /** Rope */
    private Rope rope;

    /** Whether or not the level is in debug more (showing off physics) */
    private boolean debug;

    /** AstronautModel cache */
    AstronautModel astroCache;

    /** All the objects in the world. */
    protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();

    /** List of stars in the world */
    protected ArrayList<Star> stars = new ArrayList<Star>();

    /** List of anchors in the world */
    protected ArrayList<Anchor> anchors = new ArrayList<Anchor>();

    /**
     * Returns the bounding rectangle for the physics world
     *
     * The size of the rectangle is in physics, coordinates, not screen coordinates
     *
     * @return the bounding rectangle for the physics world
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns the scaling factor to convert physics coordinates to screen coordinates
     *
     * @return the scaling factor to convert physics coordinates to screen coordinates
     */
    public Vector2 getScale() {
        return scale;
    }

    /**
     * Returns a reference to the Box2D World
     *
     * @return a reference to the Box2D World
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns a reference to the vector World
     *
     * @return a reference to the vector World
     */
    public VectorWorld getVectorWorld() {
        return vectorWorld;
    }

    /**
     * Returns a reference to the planet list
     *
     * @return a reference to the planet list
     */
    public PlanetList getPlanets() {
        return planets;
    }

    /**
     * Returns a reference to the first player avatar
     *
     * @return a reference to the first player avatar
     */
    public AstronautModel getPlayer1() {
        return player1;
    }

    /**
     * Returns a reference to the second player avatar
     *
     * @return a reference to the second player avatar
     */
    public AstronautModel getPlayer2() {
        return player2;
    }

    /**
     * Returns a reference to the rope
     *
     * @return a reference to the rope
     */
    public Rope getRope() {
        return rope;
    }

    /**
     * Returns whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @return whether this level is currently in debug node
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Sets whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @param value	whether this level is currently in debug node
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Creates a new LevelModel
     *
     * The level is empty and there is no active physics world.  You must read
     * the JSON file to initialize the level
     */
    public LevelModel() {
        world  = new World(new Vector2(0,0), false);
        bounds = new Rectangle(0,0,1,1);
        scale = new Vector2(1,1);
        debug  = false;
        planets = new PlanetList(Galaxy.WHIRLPOOL, scale);
    }

    public AstronautModel createAstro(JsonValue json, boolean active) {
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
     * Lays out the game geography from the given JSON file. Requires planets is not null.
     *
     * @param levelFormat	the JSON file defining the level
     */
    public void populate(JsonValue levelFormat) {
        float[] pSize = levelFormat.get("physicsSize").asFloatArray();
        int[] gSize = levelFormat.get("graphicSize").asIntArray();

        String key = levelFormat.get("background").get("texture").asString();
        background = JsonAssetManager.getInstance().getEntry(key, Texture.class);

        bounds = new Rectangle(0,0,pSize[0],pSize[1]);
        scale.x = gSize[0]/pSize[0];
        scale.y = gSize[1]/pSize[1];

        // Create players
//        player1 = new AstronautModel(true);
//        player1.initialize(levelFormat.get("astronaut 1"));
//        player1.setDrawScale(scale);
//        player1.setName("avatar1");
//        activate(player1);
//
//        player2 = new AstronautModel(false);
//        player2.initialize(levelFormat.get("astronaut 2"));
//        player2.setDrawScale(scale);
//        player1.setName("avatar2");
//        activate(player2);

        player1 = createAstro(levelFormat.get("astronaut 1"), true);
        player1.setName("avatar");
        player1.activatePhysics(world);
        //addObject(player1);

        player2 = createAstro(levelFormat.get("astronaut 2"), false);
        player2.setName("avatar2");
        player2.activatePhysics(world);

        //objects.remove(player1); objects.remove(player2);

        JsonValue ropeVal = levelFormat.get("rope");

        key = ropeVal.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        float dwidth  = texture.getRegionWidth()/scale.x;
        float dheight = texture.getRegionHeight()/scale.y;
        rope = new Rope(player1.getX() + 0.5f, player1.getY() + 0.5f,
                ropeVal.get("rope width").asFloat(), dwidth, dheight, player1, player2);
        rope.setTexture(texture);
        rope.setDrawScale(scale);
        rope.setName("rope");
        activate(rope);

        objects.add(player1); objects.add(player2);

        //Create planets
        float[][] planetSpecs = null;
        JsonValue planet = levelFormat.get("planets").child();
        while(planet != null) {
            planetSpecs = addRow(planetSpecs, planetSpec(planet));
            planet = planet.next;
        }

        planets = new PlanetList(Galaxy.WHIRLPOOL, scale);
        planets.addPlanets(planetSpecs, world, vectorWorld);

        //add stars
        int i = 0;
        JsonValue starVals = levelFormat.get("stars").child();
        while(starVals != null) {
            Star star = Star.fromJSON(starVals, scale);
            star.setName("star" + i);
            activate(star);
            stars.add(star);
            starVals = starVals.next;
        }

        //add anchors
        i = 0;
        JsonValue anchorVals = levelFormat.get("anchors").child();
        while(anchorVals != null) {
            Anchor anchor = Anchor.fromJSON(anchorVals, scale);
            anchor.setName("anchor" + i);
            activate(anchor);
            anchors.add(anchor);
            anchorVals = anchorVals.next;
        }
    }

    /**
     * Return a new array that is the result of adding newRow as the last row of old.
     * @param old The 2D array to be added to
     * @param newRow The new row to add to old
     * @return The result of adding newRow to old.
     */
    private float[][] addRow(float[][] old, float[] newRow) {
        if (old == null) {
            float[][] out = new float[1][newRow.length];
            out[0] = newRow;
            return out;
        }
        int n = old[0].length; int m = old.length;
        float[][] out = new float[m+1][n];

        for (int i = 0; i < m; i++) {
            out[i] = old[i];
        }

        out[m] = newRow;
        return out;
    }

    /**
     * Parse a JSON pertaining to 1 planet into the relevant data entries.
     * @param json A JsonValue containing data relating to one planet.
     * @return A vector describing a planet where 1st col is x, 2nd is y, 3rd is radius, 4th is mass, 6th is sprite to
     *          use and 5th is gravitational pull range.
     */
    private float[] planetSpec(JsonValue json) {
        float[] out = new float[6];

        out[0] = json.get("x").asFloat();
        out[1] = json.get("y").asFloat();
        out[2] = json.get("radius").asFloat();
        out[3] = json.get("mass").asFloat();
        out[4] = json.get("grange").asFloat();
        out[5] = json.get("sprite").asFloat();

        return out;
    }


    public void dispose() {
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        for(Planet p : planets.getPlanets()){
            p.deactivatePhysics(world);
        }
        objects.clear();
        if (world != null) {
            world.dispose();
            world = new World(new Vector2(0,0), false);
        }
        objects.clear();
        planets.clear();
        stars.clear();
        anchors.clear();
        vectorWorld = new VectorWorld();
    }

    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    protected void activate(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    private boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    /**
     * Draws the level to the given game canvas
     *
     * If debug mode is true, it will outline all physics bodies as wireframes. Otherwise
     * it will only draw the sprite representations.
     *
     * @param canvas	the drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.clear();

        canvas.begin();

        float x = (float) Math.floor((canvas.getCamera().position.x - canvas.getWidth()/2)/canvas.getWidth()) * canvas.getWidth();

        canvas.draw(background, Color.WHITE, x, 0,canvas.getWidth(),canvas.getHeight());
        canvas.draw(background, Color.WHITE, x + canvas.getWidth(), 0,canvas.getWidth(),canvas.getHeight());

        for(Planet p : planets.getPlanets()){
            p.draw(canvas);
        }
        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }
        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for(Planet p : planets.getPlanets()){
                p.drawDebug(canvas);
            }
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }
}
