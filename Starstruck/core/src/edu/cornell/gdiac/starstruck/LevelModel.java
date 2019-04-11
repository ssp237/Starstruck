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
import edu.cornell.gdiac.starstruck.Models.Enemy;
import edu.cornell.gdiac.starstruck.Models.Worm;
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
    private VectorWorld vectorWorld;
    /** The boundary of the world */
    private Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** The background texture*/
    private Texture background;

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
    /** Rope texture for extension method */
    //protected TextureRegion ropeTexture;
    /** List of enemies in the world */
    protected PooledList<Enemy> enemies = new PooledList<Enemy>();

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
     * Returns a reference to the enemy list
     *
     * @return a reference to the enemy list
     */
    public PooledList<Enemy> getEnemies() {
        return enemies;
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
        this(new Rectangle(0,0,1,1), new Vector2(1,1));
    }

    /**
     * Creates a new LevelModel with the specified bounds and scale.
     *
     * @param bounds The bounds for this level.
     * @param scale The scale for this level.
     */
    public LevelModel(Rectangle bounds, Vector2 scale) {
        world  = new World(new Vector2(0,0), false);
        this.bounds = bounds;
        this.scale = scale;
        debug  = false;
        planets = new PlanetList(Galaxy.WHIRLPOOL, scale);
    }



    /**
     * Set the background to the specified texture.
     * @param background The texture to use as the background.
     */
    public void setBackground(Texture background) {
        this.background = background;
    }

    /**
     * Lays out the game geography from the given JSON file. Requires planets is not null.
     *
     * @param levelFormat	the JSON file defining the level
     */
    public void populate(JsonValue levelFormat) {
        float[] pSize = levelFormat.get("physicsSize").asFloatArray();
        int[] gSize = levelFormat.get("graphicSize").asIntArray();

        String key = levelFormat.get("background").asString();
        background = JsonAssetManager.getInstance().getEntry(key, Texture.class);

        bounds = new Rectangle(0,0,pSize[0],pSize[1]);
        scale.x = gSize[0]/pSize[0];
        scale.y = gSize[1]/pSize[1];

        player1 = AstronautModel.fromJson(levelFormat.get("astronaut 1"), scale,true);
        player1.setName("avatar");
        player1.activatePhysics(world);
        //addObject(player1);

        player2 = AstronautModel.fromJson(levelFormat.get("astronaut 2"), scale, false);
        player2.setName("avatar2");
        player2.activatePhysics(world);

        //objects.remove(player1); objects.remove(player2);

        JsonValue ropeVal = levelFormat.get("rope");

        key = ropeVal.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        //ropeTexture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        float dwidth  = texture.getRegionWidth()/scale.x;
        float dheight = texture.getRegionHeight()/scale.y;
        rope = new Rope(player1.getX() + 0.5f, player1.getY() + 0.5f,
                ropeVal.get("rope width").asFloat(), dwidth, dheight, player1, player2);
        rope.setTexture(texture);
        rope.setDrawScale(scale);
        rope.setName("rope");
        activate(rope);

        objects.add(player1); objects.add(player2);

        Planet.setPresets(levelFormat.get("planet specs"));
        planets = new PlanetList(Galaxy.WHIRLPOOL, scale);

        JsonValue planet = levelFormat.get("planets").child();
        while(planet != null) {
            planets.addPlanet(planet, world, vectorWorld);
            planet = planet.next();
        }

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


        //add worms
       JsonValue wormVals = levelFormat.get("worms").child();
        while (wormVals != null) {
            Worm wormie = Worm.fromJSON(wormVals, scale);
            activate(wormie);
            enemies.add(wormie);
            wormVals = wormVals.next;
        }
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
        enemies.clear();
        vectorWorld = new VectorWorld();
    }

    /**
     * Add the specified obstacle to this LevelModel
     * @param obj The obstacle to be added to this LevelModel
     */
    public void add(Obstacle obj) {
        switch (obj.getType()) {
            case PLANET: planets.addPlanet((Planet) obj, vectorWorld); break;
            case ANCHOR: activate(obj); break;
            case STAR: activate(obj); break;
            case PLAYER: addPlayer((AstronautModel) obj); break;
            case ROPE: objects.add(0, obj); obj.activatePhysics(world); rope = (Rope) obj; break;
            case WORM: activate(obj); enemies.add((Worm) obj); break;
        }
    }

    /**
     * Uh remove idk
     * Player and rope can NOT be removed, so this method will do nothing.
     *
     * @param obj Object to be removed.
     */
    public void remove(Obstacle obj) {
        switch (obj.getType()) {
            case PLANET: obj.deactivatePhysics(world); planets.remove((Planet) obj); break;
            case ANCHOR: deactivate(obj); break;
            case STAR: deactivate(obj); break;
            case WORM: deactivate(obj); enemies.remove((Worm) obj); break;
        }
    }

    /**
     * Handles case of adding a player
     * @param player Player to be added
     */
    private void addPlayer(AstronautModel player) {
        if (player.getName().contains("2")) {
            player2 = player;
        } else {
            player1 = player;
        }
        activate(player);
    }

    /**
     * Immediately adds the object to the physics world
     *
     * @param obj The object to add
     */
    protected void activate(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Remove the object from the physics world
     *
     * @param obj The object to remove
     */
    private void deactivate(Obstacle obj) {
        objects.remove(obj);
        obj.deactivatePhysics(world);
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

    public PooledList<Obstacle> getAllObjects() {
        PooledList<Obstacle> out = new PooledList<Obstacle>();
        out.addAll(objects);
        out.addAll(planets.getPlanets());
        return out;
    }

    /**
     * Write this level to a JsonValue that, when parsed, would return the same level.
     * @return A JsonValue representing this level.
     */
    public JsonValue toJSON(){
        JsonValue out = new JsonValue(JsonValue.ValueType.object);

        //Add size fields
        JsonValue physicsSize = new JsonValue(JsonValue.ValueType.array);
        physicsSize.addChild(new JsonValue(bounds.width));
        physicsSize.addChild(new JsonValue(bounds.height));

        JsonValue graphicsSize = new JsonValue(JsonValue.ValueType.array);
        graphicsSize.addChild(new JsonValue((int) (bounds.width * scale.x)));
        graphicsSize.addChild(new JsonValue((int) (bounds.height * scale.y)));

        out.addChild("physicsSize", physicsSize);
        out.addChild("graphicSize", graphicsSize);

        //Add background
        out.addChild("background", new JsonValue(JsonAssetManager.getInstance().getKey(background)));

        //Add astronauts
        out.addChild("astronaut 1", player1.toJson());
        out.addChild("astronaut 2", player2.toJson());

        //Add rope
        out.addChild("rope", rope.toJson());

        //Add planet presets
        out.addChild("planet specs", Planet.presetJson());

        //Add planets
        out.addChild("planets", planets.toJson());

        //Add obstacles
        JsonValue anchors = new JsonValue(JsonValue.ValueType.array);
        JsonValue stars = new JsonValue(JsonValue.ValueType.array);
        JsonValue worms = new JsonValue(JsonValue.ValueType.array);

        for (Obstacle obj : objects) {
            switch (obj.getType()) {
                case STAR: stars.addChild(((Star) obj).toJson()); break;
                case ANCHOR: anchors.addChild(((Anchor) obj).toJson()); break;
            }
        }


        out.addChild("anchors", anchors);
        out.addChild("stars", stars);

        //Add enemies

        out.addChild("worm", worms);


        return out;
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
        float y = (float) Math.floor((canvas.getCamera().position.y - canvas.getHeight()/2)/canvas.getHeight()) * canvas.getHeight();

        canvas.draw(background, Color.WHITE, x, y,canvas.getWidth(),canvas.getHeight());
        canvas.draw(background, Color.WHITE, x + canvas.getWidth(), y,canvas.getWidth(),canvas.getHeight());
        canvas.draw(background, Color.WHITE, x, y + canvas.getHeight(),canvas.getWidth(),canvas.getHeight());
        canvas.draw(background, Color.WHITE, x + canvas.getWidth(), y + canvas.getHeight(),canvas.getWidth(),canvas.getHeight());

        for(Planet p : planets.getPlanets()){
            p.draw(canvas);
        }
        for(Obstacle obj : objects) {
            if (obj.getType() != ObstacleType.PLAYER) obj.draw(canvas);
        }
        if (player1.isActive()) { player2.draw(canvas); player1.draw(canvas); }
        else { player1.draw(canvas); player2.draw(canvas); }
        for (Enemy e: enemies) {
            e.draw(canvas);
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
            for (Enemy e: enemies) {
             e.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }
}
