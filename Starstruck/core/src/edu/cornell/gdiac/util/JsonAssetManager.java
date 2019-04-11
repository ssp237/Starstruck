/*
 * JsonAssetManager.java
 *
 * This is an extension of AssetManager that uses a JSON to define the assets
 * to be used.  The JSON file is called the asset directory.  This class allows
 * you to load and unload the directory all at once.  It also allows you to 
 * refer to assets by their directory keys instead of the file names.  This
 * provides a more extensible way of adding assets.
 *
 * Right now, this asset manager only supports textures, fonts, and sounds. If
 * you want an asset directory that provides support for other assets, you will
 * need to extend this class.
 *
 * Author: Walker M. White
 * Version: 3/2/2016
 */
 package edu.cornell.gdiac.util;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.assets.loaders.*;
import com.badlogic.gdx.assets.loaders.resolvers.*;

/**
 * An asset manager that uses a JSON file to define its assets.
 *
 * The manager allows assets to be loaded normally, but it is best 
 * to use the asset directory methods instead.  This means using
 * getEntry() instead of get() to access an asset.
 *
 * This class is implemented as a singleton in order to allow easy 
 * access from anywhere in the application. This makes sense when
 * there is only one directory to load.  If you want to load a new
 * directory in the middle of the game, you may need to rethink
 * this design.
 */
public class JsonAssetManager extends AssetManager {

	/** The asset directory of this asset manager */
	private JsonValue directory;
	/** The allocated texture regions (for easy clean-up) */
	private ObjectMap<String,TextureRegion> regions;
	/** The allocated filmstrips (for easy clean-up) */
	private ObjectMap<String,FilmStrip> filmstrips;
	/** The allocated textures (for easy clean-up) */
	private ObjectMap<String,Texture> textures;
	/** The allocated fonts (for easy clean-up) */
	private ObjectMap<String,BitmapFont> fonts;
	/** The allocated sounds (for easy clean-up) */
	private ObjectMap<String,Sound> sounds;
	/** The allocated music (for easy clean-up) */
	private ObjectMap<String,Music> music;
	
	/** The singleton asset manager (for easy access) */
	private static JsonAssetManager manager;
	
	/**
	 * Returns a reference to the singleton asset manager
	 * 
	 * If there is no active asset manager, this method will initialize
	 * one immediately.
	 *
	 * @return a reference to the singleton asset manager
	 */
	public static JsonAssetManager getInstance() {
		if (manager == null) {
			manager = new JsonAssetManager();
		}
		return manager;
	}

	/**
	 * Clears and disposes the singleton asset manager
	 * 
	 * Calling the method will flush out all assets immediately. This
	 * method is preferred to disposing of the manager manually.
	 */
	public static void clearInstance() {
		if (manager != null) {
			manager.clear();
			manager.dispose();
			manager = null;
		}
	}

	/**
	 * Creates a new asset manager
	 *
	 * The asset manager will have font support.  It will also have
	 * sound support through the SoundController.
	 */
	private JsonAssetManager() {
		super();
		
		// Add font support to the asset manager
		FileHandleResolver resolver = new InternalFileHandleResolver();
		setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		// To keep track of the directory
		directory = null;
		regions = new ObjectMap<String,TextureRegion>();
		filmstrips = new ObjectMap<String,FilmStrip>();
		textures = new ObjectMap<String,Texture>();
		fonts = new ObjectMap<String,BitmapFont>();
		sounds = new ObjectMap<String,Sound>();
		music = new ObjectMap<String,Music>();
	}
	
	/**
	 * Loads assets defined by the given directory
	 *
	 * The assets are all loaded asynchronously. The directory will not
	 * be loaded if their is a current active directory.
	 *
	 * @param json	the parsed asset directory
	 */
	public void loadDirectory(JsonValue json) {
		assert directory == null : "Directory has already been loaded; must unload first";
		directory = json;
		loadTextures();
		loadTextureRegions();
		loadFilmStrips();
		loadSounds();
		loadFonts();
		loadMusic();
	}
	
	/**
	 * Returns the JSON key for a given asset type
	 *
	 * @param type the asset type
	 *
	 * @return the JSON key for a given asset type
	 */
	private <T> String getClassIdentifier(Class<T> type) {
		if (type.equals(Texture.class)) {
			return "textures";
		} else if (type.equals(TextureRegion.class)) {
			return "texture regions";
		} else if (type.equals(FilmStrip.class)) {
			return "filmstrips";
		} else if (type.equals(BitmapFont.class)) {
			return "fonts";
		} else if (type.equals(Sound.class)) {
			return "sounds";
		} else if (type.equals(Music.class)) {
			return "music";
		}
		// Should never reach here
		assert false : "JSON directory does not support this assets class";
		return null;
	}

	/**
	 * Loads all textures in the asset directory
	 */
	private void loadTextures() {
		JsonValue json = directory.getChild(getClassIdentifier(Texture.class));
		while (json != null) {
			String file= json.getString("file");
			load(file,Texture.class);
			json = json.next;
		}
	}

	/**
	 * Loads all texture regions in the asset directory
	 */
	private void loadTextureRegions() {
		JsonValue json = directory.getChild(getClassIdentifier(TextureRegion.class));
		while (json != null) {
			String file= json.getString("file");
			load(file,Texture.class);
			json = json.next;
		}
	}

	/**
	 * Loads all filmstrips in the asset directory
	 */
	private void loadFilmStrips() {
		JsonValue json = directory.getChild(getClassIdentifier(FilmStrip.class));
		while (json != null) {
			String file= json.getString("file");
			load(file,Texture.class);
			json = json.next;
		}
	}
	
	/**
	 * Loads all fonts in the asset directory
	 */
	private void loadFonts() {
		JsonValue json = directory.getChild(getClassIdentifier(BitmapFont.class));
		while (json != null) {
			FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
			size2Params.fontFileName = json.getString("file");
			size2Params.fontParameters.size = json.getInt("size");
		
			load(size2Params.fontFileName, BitmapFont.class, size2Params);
			json = json.next;
		}
	}
	
	/**
	 * Loads all sounds in the asset directory
	 */
	private void loadSounds() {
		JsonValue json = directory.getChild(getClassIdentifier(Sound.class));
		while (json != null) {
			String file= json.getString("file");
			System.out.println("Loading "+json.name()+" at "+file);
			load(file,Sound.class);
			json = json.next;
		}
	}

	/**
	 * Loads all music in the asset directory
	 */
	private void loadMusic() {
		JsonValue json = directory.getChild(getClassIdentifier(Music.class));
		while (json != null) {
			String file= json.getString("file");
			System.out.println("Loading "+json.name()+" at "+file);
			load(file,Music.class);
			json = json.next;
		}
	}
	
	/**
	 * Unloads assets defined in the current directory
	 *
	 * The asset loader is now free to load another directory.
	 */
	public void unloadDirectory() {
		unloadTextures();
		unloadTextureRegions();
		unloadFilmStrips();
		unloadSounds();
		unloadFonts();
		unloadMusic();
		directory = null;
	}
	
	/**
	 * Unloads all textures in the asset directory
	 */
	private void unloadTextures() {
		JsonValue json = directory.getChild(getClassIdentifier(Texture.class));
		while (json != null) {
			String file = json.getString("file");
			if (isLoaded(file)) {
				unload(file);
				if (textures.containsKey(file)) {
					textures.remove(file);
				}
			}
			json = json.next;
		}
	}

	/**
	 * Unloads all textures regions in the asset directory
	 */
	private void unloadTextureRegions() {
		JsonValue json = directory.getChild(getClassIdentifier(TextureRegion.class));
		while (json != null) {
			String file = json.getString("file");
			if (isLoaded(file)) {
				unload(file);
				if (regions.containsKey(file)) {
					regions.remove(file);
				}
			}
			json = json.next;
		}
	}

	/**
	 * Unloads all filmstrips in the asset directory
	 */
	private void unloadFilmStrips() {
		JsonValue json = directory.getChild(getClassIdentifier(FilmStrip.class));
		while (json != null) {
			String file = json.getString("file");
			if (isLoaded(file)) {
				unload(file);
				if (filmstrips.containsKey(file)) {
					filmstrips.remove(file);
				}
			}
			json = json.next;
		}
	}
	
	/**
	 * Unloads all fonts in the asset directory
	 */
	private void unloadFonts() {
		JsonValue json = directory.getChild(getClassIdentifier(BitmapFont.class));
		while (json != null) {
			String file = json.getString("file");
			if (isLoaded(file)) {
				unload(file);
				if (fonts.containsKey(file)) {
					fonts.remove(file);
				}
			}
			json = json.next;
		}
	}

	/**
	 * Unloads all sounds in the asset directory
	 */	
	private void unloadSounds() {
		JsonValue json = directory.getChild(getClassIdentifier(Sound.class));
		while (json != null) {
			String file = json.getString("file");
			if (isLoaded(file)) {
				SoundController controller = SoundController.getInstance();
				controller.deallocate(this, file);
				if (sounds.containsKey(file)) {
					sounds.remove(file);
				}
			}
			json = json.next;
		}
	}

	/**
	 * Unloads all music in the asset directory
	 */
	private void unloadMusic() {
		JsonValue json = directory.getChild(getClassIdentifier(Music.class));
		while (json != null) {
			String file = json.getString("file");
			if (isLoaded(file)) {
				unload(file);
				if (music.containsKey(file)) {
					music.remove(file);
				}
			}
			json = json.next;
		}
	}
	
	/**
	 * Allocates assets defined in the current directory
	 *
	 * Assets are allocate after loading.  This binds the asset to
	 * the directory key to allow key look-up.
	 */
	public void allocateDirectory() {
		JsonValue json = directory.getChild(getClassIdentifier(TextureRegion.class));
		while (json != null) {
			allocateTextureRegion(json);
			json = json.next;
		}
		json = directory.getChild(getClassIdentifier(Texture.class));
		while (json != null) {
			allocateTexture(json);
			json = json.next;
		}
		json = directory.getChild(getClassIdentifier(FilmStrip.class));
		while (json != null) {
			allocateFilmStrip(json);
			json = json.next;
		}
		json = directory.getChild(getClassIdentifier(BitmapFont.class));
		while (json != null) {
			allocateFont(json);
			json = json.next;
		}
		json = directory.getChild(getClassIdentifier(Sound.class));
		while (json != null) {
			allocateSound(json);
			json = json.next;
		}
		json = directory.getChild(getClassIdentifier(Music.class));
		while (json != null) {
			allocateMusic(json);
			json = json.next;
		}
	}

	/**
	 * Allocates a texture region and binds it to the directory key
	 *
	 * @param json 	the directory entry for the asset
	 */	
	private TextureRegion allocateTextureRegion(JsonValue json) {
		String filename = json.getString("file");
		TextureRegion region = new TextureRegion(get(filename, Texture.class));
		region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		if (json.getBoolean("wrap")) {
			region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		}
		regions.put(json.name(),region);
		return region;
	}
	
	/**
	 * Allocates a texture and binds it to the directory key
	 *
	 * @param json 	the directory entry for the asset
	 */	
	private Texture allocateTexture(JsonValue json) {
		String filename = json.getString("file");
		Texture texture = get(filename, Texture.class);
		texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		if (json.getBoolean("wrap")) {
			texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		}
		textures.put(json.name(),texture);
		return texture;
	}

	/**
	 * Allocates a filmstrip and binds it to the directory key
	 *
	 * @param json 	the directory entry for the asset
	 */
	private FilmStrip allocateFilmStrip(JsonValue json) {
		String filename = json.getString("file");
		int rows = json.getInt("rows");
		int cols = json.getInt("cols");
		int size = json.getInt("size");
		int delay = json.getInt("delay");
		FilmStrip strip = new FilmStrip(get(filename, Texture.class), rows, cols, size, delay);
		strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		if (json.getBoolean("wrap")) {
			strip.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		}
		filmstrips.put(json.name(),strip);
		return strip;
	}
	
	/**
	 * Allocates a font and binds it to the directory key
	 *
	 * @param json 	the directory entry for the asset
	 */	
	private BitmapFont allocateFont(JsonValue json) {
		String filename = json.getString("file");
		BitmapFont font = get(filename, BitmapFont.class);
		fonts.put(json.name(),font);
		return font;
	}

	/**
	 * Allocates a sound and binds it to the directory key
	 *
	 * @param json 	the directory entry for the asset
	 */	
	private Sound allocateSound(JsonValue json) {
		String filename = json.getString("file");
		Sound sound = get(filename, Sound.class);
		SoundController controller = SoundController.getInstance();
		controller.allocate(this, filename);
		sounds.put(json.name(), sound);
		return sound;
	}


	/**
	 * Allocates a sound and binds it to the directory key
	 *
	 * @param json 	the directory entry for the asset
	 */
	private Music allocateMusic(JsonValue json) {
		String filename = json.getString("file");
		Music sound = get(filename, Music.class);
		music.put(json.name(), sound);
		return sound;
	}
	
	/**
	 * Returns the asset associate with the given directory key
	 *
	 * The assets must be allocated for this method to return a value.
	 *
	 * @param key	the asset directory key
	 * @param type	the asset type
	 */
	public <T> T getEntry(String key, Class<T> type) {
		try {
			if (type.equals(TextureRegion.class)) {
				return (T)regions.get(key);
			} else if (type.equals(Texture.class)) {
				return (T)textures.get(key);
			} else if (type.equals(FilmStrip.class)) {
				return (T)filmstrips.get(key).copy();
			} else if (type.equals(BitmapFont.class)) {
				return (T)fonts.get(key);
			} else if (type.equals(Sound.class)) {
				return (T)sounds.get(key);
			} else if (type.equals(Music.class)) {
				return (T)music.get(key);
			}
		} catch (Exception e) {
			return null;
		}

		// Should never reach here
		assert false : "JSON directory does not support this assets class";
		return null;
		
	}

	/**
	 * Reverse lookup of a key given a Texture. Null if not found.
	 * @param texture Texture to be searched for.
	 * @return Key associated with texture; null if not found.
	 */
	public String getKey(Texture texture) {
		for (ObjectMap.Entry<String, Texture> entry : textures) {
			if (texture.equals(entry.value)) return entry.key;
		}
		return null; //Not found
	}

	/**
	 * Reverse lookup of a key given a TextureRegion. Null if not found.
	 * @param texture TextureRegion to be searched for.
	 * @return Key associated with texture; null if not found.
	 */
	public String getKey(TextureRegion texture) {
		for (ObjectMap.Entry<String, TextureRegion> entry : regions) {
			if (texture.equals(entry.value)) return entry.key;
		}
		return null; //Not found
	}

	/**
	 * Reverse lookup of a key given a Filmstrip. Null if not found.
	 * @param strip Filmstrip to be searched for.
	 * @return Key associated with strip; null if not found.
	 */
	public String getKey(FilmStrip strip) {
		for (ObjectMap.Entry<String, FilmStrip> entry : filmstrips) {
			if (strip.equals(entry.value)) return entry.key;
		}
		return null; //Not found
	}

	/**
	 * Reverse lookup of a key given a font. Null if not found.
	 * @param font font to be searched for.
	 * @return Key associated with font; null if not found.
	 */
	public String getKey(BitmapFont font) {
		for (ObjectMap.Entry<String, BitmapFont> entry : fonts) {
			if (font.equals(entry.value)) return entry.key;
		}
		return null; //Not found
	}

	/**
	 * Reverse lookup of a key given a Sound. Null if not found.
	 * @param sound Sound to be searched for.
	 * @return Key associated with sound; null if not found.
	 */
	public String getKey(Sound sound) {
		for (ObjectMap.Entry<String, Sound> entry : sounds) {
			if (sound.equals(entry.value)) return entry.key;
		}
		return null; //Not found
	}

}
