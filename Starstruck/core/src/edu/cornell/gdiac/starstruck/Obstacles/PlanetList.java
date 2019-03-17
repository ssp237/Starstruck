package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.starstruck.Galaxy;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;

import javax.xml.soap.Text;
import java.util.ArrayList;

/**
 * An instance represents a list of the planets in one level.
 * Implements iterable.
 * Stores texture files, so that different galaxy themes can be selected.
 */
public class PlanetList {

    /** Track all loaded assets (for unloading purposes) */
    protected Array<String> assets;

    /** Files for default galaxy planets*/
    private static String GALAXY1_PLANET1_FILE = "planets/planet1.png";
    private static String GALAXY1_PLANET2_FILE = "planets/planet2.png";
    private static String GALAXY1_PLANET3_FILE = "planets/planet3.png";
    private static String GALAXY1_PLANET4_FILE = "planets/planet4.png";

    /** Files for planets to be used when drawing. MUST BE SET IN CONSTRUCTOR BEFORE ANY TEXTURES ARE CREATED*/
    private String PLANET1_FILE;
    private String PLANET2_FILE;
    private String PLANET3_FILE;
    private String PLANET4_FILE;

    /** Textures to be used when drawing planets*/
    TextureRegion planet1_texture;
    TextureRegion planet2_texture;
    TextureRegion planet3_texture;
    TextureRegion planet4_texture;

    /** The planets in this PlanetList*/
    private ArrayList<Planet> planets;

    /**
     *  Constructs a new PlanetList from the given array of planet specifications, using
     *  the given assetManager to load the assets and in the specified galaxy.
     * @param planetSpecs A 2D array of specifications for the planets to be constructed. Each row specifies
     *                    a planet, and must contain in order: x coord, y coord, radius, mass, draw scale,
     *                    planet sprite to use (1-4).
     *                    Will probably need to edit eventually.
     * @param manager The asset manager idk
     * @param galaxy The galaxy for the planets in this level.
     * @param scale The drawing scale
     * @param vectorWorld The vectorWorld controlling the gravity for this level
     * @param world The world idk why we need this but sure
     */
    public PlanetList(float[][] planetSpecs, AssetManager manager, Galaxy galaxy, Vector2 scale, VectorWorld vectorWorld,
                      World world) {
        setPlanetFiles(galaxy);

        manager.load(PLANET1_FILE, Texture.class);
        assets.add(PLANET1_FILE);
        manager.load(PLANET2_FILE, Texture.class);
        assets.add(PLANET2_FILE);
        manager.load(PLANET3_FILE, Texture.class);
        assets.add(PLANET3_FILE);
        manager.load(PLANET4_FILE, Texture.class);
        assets.add(PLANET4_FILE);

        planet1_texture = createTexture(manager, PLANET1_FILE, true);
        planet2_texture = createTexture(manager, PLANET2_FILE, true);
        planet3_texture = createTexture(manager, PLANET3_FILE, true);
        planet4_texture = createTexture(manager, PLANET4_FILE, true);

        String ptname = "planet";
        for (int i = 0; i < planetSpecs.length; i++) {
            TextureRegion texture = getPlanetTexture((int) planetSpecs[i][5]);

            Planet planet = new Planet(planetSpecs[i][0], planetSpecs[i][1], planetSpecs[i][2], planetSpecs[i][3],
                    planetSpecs[i][4], texture, world, scale);

            planet.setName(ptname+i);

            //Vector2 pos = new Vector2(obj.getBody().getPosition().x, obj.getBody().getPosition().y - obj.getRadius());
            //vectorWorld.addPlanet(obj, PLANETS[i][3], obj.getCenter()); //Radius parameter is temporary fix for why center is off

            vectorWorld.addPlanet(planet, planetSpecs[i][3]);
        }


    }

    /**
     * Returns a newly loaded texture region for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     *
     * @param manager 	Reference to global asset manager.
     * @param file		The texture (region) file
     * @param repeat	Whether the texture should be repeated
     *
     * @return a newly loaded texture region for the given file.
     */
    protected TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
        if (manager.isLoaded(file)) {
            TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (repeat) {
                region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            }
            return region;
        }
        return null;
    }

    /**
     * Sets the planet sprites based on the current galaxy.
     * @param galaxy The galaxy who's planet sprites should be used.
     */
    private void setPlanetFiles(Galaxy galaxy) {
        switch(galaxy){
            case DEFAULT :
            default : setDefaultSprites();

        }
    }

    /**
     * Sets the planet files to the default sprite options.
     */
    private void setDefaultSprites(){
        PLANET1_FILE = GALAXY1_PLANET1_FILE;
        PLANET2_FILE = GALAXY1_PLANET2_FILE;
        PLANET3_FILE = GALAXY1_PLANET3_FILE;
        PLANET4_FILE = GALAXY1_PLANET4_FILE;

    }

    /**
     * Returns the texture for the 0-indexed i'th planet sprite in the current galaxy.
     * @param i The planet sprite to return in the current galaxy (indexed by 0)
     * @return The 0-indexed i'th planet sprite in the current galaxy.
     */
    private TextureRegion getPlanetTexture(int i) {
        switch(i) {
            case 0:
                return planet1_texture;
            case 1:
                return planet2_texture;
            case 2:
                return planet3_texture;
            case 3:
                return planet4_texture;
            default:
                return planet1_texture;
        }
    }
}