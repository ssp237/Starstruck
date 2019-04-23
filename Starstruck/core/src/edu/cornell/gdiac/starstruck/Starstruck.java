/*
 * Starstruck.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter.
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.Vector3;
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * Root class for a LibGDX.
 *
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However,
 * those classes are unique to each platform, while this class is the same across all
 * plaforms. In addition, this functions as the root class all intents and purposes,
 * and you would draw it as a root class in an architecture specification.
 */
public class Starstruck extends Game implements ScreenListener {
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	/** Avatar mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Avatar mode for the the game proper (CONTROLLER CLASS) */
	private int current;
	/** List of all WorldControllers */
	private WorldController[] controllers;
	/** Number of special screens */
	private static int SCREENS = 4;

	Vector3 camPos;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public Starstruck() {
		// Start loading with the asset manager
//		manager = new AssetManager();
//
//		// Add font support to the asset manager
//		FileHandleResolver resolver = new InternalFileHandleResolver();
//		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
//		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
	}

	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();

		camPos = canvas.getCamera().position.cpy();

		loading = new LoadingMode(canvas,1);

		// Initialize the three game worlds
		controllers = new WorldController[SCREENS];
		controllers[1] = new LevelSelect(canvas);
		controllers[0] = new MenuMode(canvas);
		controllers[2] = new EditController();
		controllers[3] = new GameController();
		for(int ii = 0; ii < controllers.length; ii++) {
			controllers[ii].preLoadContent(JsonAssetManager.getInstance());
		}
		current = 0;
		loading.setScreenListener(this);
		setScreen(loading);
	}

	/**
	 * Called when the Application is destroyed.
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		for(int ii = 0; ii < controllers.length; ii++) {
			controllers[ii].unloadContent(JsonAssetManager.getInstance());
			controllers[ii].dispose();
		}

		canvas.dispose();
		canvas = null;

		// Unload all of the resources
		JsonAssetManager.getInstance().clear();
		JsonAssetManager.getInstance().dispose();
		super.dispose();
	}

	/**
	 * Called when the Application is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}

	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode, String json) {
		canvas.resetCamera();
		if (exitCode == WorldController.EXIT_PLAY) {
			current = WorldController.EXIT_PLAY;
			controllers[current] = new GameController(json);
			controllers[current].loadContent(JsonAssetManager.getInstance());
			controllers[current].setScreenListener(this);
			controllers[current].setCanvas(canvas);
			exitScreen(screen, WorldController.EXIT_PLAY);
		} else {
			exitScreen(screen, exitCode);
		}
	}

	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		canvas.getCamera().position.set(camPos);
		//System.out.println(camPos);
		canvas.getCamera().update();
		//System.out.println(canvas.getCamera().position);

		canvas.resetCamera();
		//System.out.println(canvas.getCamera().position);
		if (screen == loading) {
			for (int ii = 0; ii < controllers.length; ii++) {
				controllers[ii].loadContent(JsonAssetManager.getInstance());
				controllers[ii].setScreenListener(this);
				controllers[ii].setCanvas(canvas);
			}
			controllers[current].reset();
			setScreen(controllers[current]);

			loading.dispose();
			loading = null;
		}  else if (exitCode == WorldController.EXIT_QUIT) {
			// We quit the main application
			if (current != WorldController.EXIT_QUIT) {
				// We quit to the menu
				current = WorldController.EXIT_QUIT;
				controllers[current].reset();
				setScreen(controllers[current]);
			} else {
				Gdx.app.exit();
			}
		} else if (exitCode == WorldController.EXIT_SELECT) {
			current = WorldController.EXIT_SELECT;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (exitCode == WorldController.EXIT_EDIT) {
			current = WorldController.EXIT_EDIT;
			controllers[current].reset();
			setScreen(controllers[current]);
		} else if (exitCode == WorldController.EXIT_PLAY) {
			current = WorldController.EXIT_PLAY;
			controllers[current].reset();
			setScreen(controllers[current]);
		}
	}

}
