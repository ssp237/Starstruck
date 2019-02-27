/*
 * SoundController.java
 *
 * Sound management in LibGDX is horrible.  It is the absolute worse thing
 * about the engine.  Furthermore, because of OpenAL bugs in OS X, it is
 * even worse than that.  There is a lot of magic vodoo that you have to
 * do to get everything working properly.  This class hides all of that
 * for you and makes it easy to play sound effects.
 * 
 * Note that this class is an instance of a Singleton.  There is only one
 * SoundController at a time.  The constructor is hidden and you cannot
 * make your own sound controller.  Instead, you use the method getInstance()
 * to get the current sound controller.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.util;

import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.utils.*;

/**
 * A singleton class for controlling sound effects in LibGDX
 * 
 * Sound sucks in LibGDX for three reasons.  (1) You have to keep track of
 * a mysterious number every time you play a sound.  (2) You have no idea
 * when a sound has finished playing.  (3) OpenAL bugs on OS X cause popping
 * and distortions if you have no idea what you are doing.  This class 
 * provides a (not so great) solution to all of these.
 * 
 * To get around (1), this sound engine uses a key management system.  
 * Instead of waiting for a number after playing the sound, you give it a
 * key ahead of time.  The key allows you to identify different instances
 * of the same sound.  See our example for collision sounds in the Rocket
 * demo for more.
 * 
 * To get around (2), we have an update() method.  By calling this method
 * you let the SoundController know that time has progressed by one animation
 * frame.  The cooldown prevents you from playing the same instance of a 
 * sound too close together.  In addition, the frame limit prevents you 
 * from playing too many sounds during the same animation frame (which can
 * lead to distortion).  This is not as good as being able to tell when a
 * sound is finished, but it works for most applications.
 * 
 * Finally, for (3), we never actually stop a Sound.  Instead we turn its
 * volume to 0 and allow it to be garbage collected when done.  This is why
 * we never allow you to access a sound object directly.
 */
public class SoundController {

	/**
	 * Inner class to track and active sound instance
	 * 
	 * A sound instance is a Sound object and a number.  That is because
	 * a single Sound object may have multiple instances.  We do not 
	 * know when a sound ends.  Therefore, we simply let the sound go
	 * and we garbage collect when the lifespace is greater than the
	 * sound limit.
	 */
	private class ActiveSound {
		/** Reference to the sound resource */
		public Sound sound;
		/** The id number representing the sound instance */
		public long  id;
		/** Is the sound looping (so no garbage collection) */
		public boolean loop;
		/** How long this sound has been running */
		public long lifespan;
		
		/**
		 * Creates a new active sound with the given values
		 * 
		 * @param s	Reference to the sound resource
		 * @param n The id number representing the sound instance
		 * @param b Is the sound looping (so no garbage collection)
		 */
		public ActiveSound(Sound s, long n, boolean b) {
			sound = s;
			id = n;
			loop = b;
			lifespan = 0;
		}
	}

	/** The default sound cooldown */
	private static final int DEFAULT_COOL = 20;
	/** The default sound length limit */
	private static final int DEFAULT_LIMIT = 120;
	/** The default limit on sounds per frame */
	private static final int DEFAULT_FRAME = 2;
	
	/** The singleton Sound controller instance */
	private static SoundController controller;
	
	/** Keeps track of all of the allocated sound resources */
	private IdentityMap<String,Sound> soundbank;
	/** Keeps track of all of the "active" sounds */
	private IdentityMap<String,ActiveSound> actives;
	/** Support class for garbage collection */
	private Array<String> collection;
	
	
	/** The number of animation frames before a key can be reused */
	private long cooldown;
	/** The maximum amount of animation frames a sound can run */
	private long timeLimit;
	/** The maximum number of sounds we can play each animation frame */
	private int frameLimit;
	/** The number of sounds we have played this animation frame */
	private int current;

	/** 
	 * Creates a new SoundController with the default settings.
	 */
	private SoundController() {
		soundbank = new IdentityMap<String,Sound>();
		actives = new IdentityMap<String,ActiveSound>();
		collection = new Array<String>();
		cooldown = DEFAULT_COOL;
		timeLimit = DEFAULT_LIMIT;
		frameLimit = DEFAULT_FRAME;
		current = 0;
	}

	/**
	 * Returns the single instance for the SoundController
	 * 
	 * The first time this is called, it will construct the SoundController.
	 * 
	 * @return the single instance for the SoundController
	 */
	public static SoundController getInstance() {
		if (controller == null) {
			controller = new SoundController();
		}
		return controller;
	}
	
	/// Properties
	/**
	 * Returns the number of frames before a key can be reused
	 * 
	 * If a key was used very recently, then an attempt to use the key
	 * again means that the sound will be stopped and restarted. This
	 * can cause undesirable artifacts.  So we limit how fast a key
	 * can be reused.
	 * 
	 * @return the number of frames before a key can be reused
	 */
	public long getCoolDown() {
		return cooldown;
	}

	/**
	 * Sets the number of frames before a key can be reused
	 * 
	 * If a key was used very recently, then an attempt to use the key
	 * again means that the sound will be stopped and restarted. This
	 * can cause undesirable artifacts.  So we limit how fast a key
	 * can be reused.
	 * 
	 * param value	the number of frames before a key can be reused
	 */
	public void setCoolDown(long value) {
		cooldown = value;
	}
	
	/**
	 * Returns the maximum amount of animation frames a sound can run
	 * 
	 * Eventually we want to garbage collect sound instances.  Since we cannot
	 * do this, we set an upper bound on all sound effects (default is 2
	 * seconds) and garbage collect when time is up.
	 * 
	 * Sounds on a loop with NEVER be garbage collected.  They must be stopped
	 * manually via stop().
	 * 
	 * @return the maximum amount of animation frames a sound can run
	 */
	public long getTimeLimit() {
		return timeLimit;
	}

	/**
	 * Sets the maximum amount of animation frames a sound can run
	 * 
	 * Eventually we want to garbage collect sound instances.  Since we cannot
	 * do this, we set an upper bound on all sound effects (default is 2
	 * seconds) and garbage collect when time is up.
	 * 
	 * Sounds on a loop with NEVER be garbage collected.  They must be stopped
	 * manually via stop().
	 * 
	 * @param value the maximum amount of animation frames a sound can run
	 */
	public void setTimeLimit(long value) {
		timeLimit = value;
	}
	
	/**
	 * Returns the maximum amount of sounds per animation frame
	 * 
	 * Because of Box2d limitations in LibGDX, you might get a lot of simultaneous
	 * sounds if you try to play sounds on collision.  This in turn can cause
	 * distortions.  We fix that by putting an upper bound on the number of 
	 * simultaneous sounds per animation frame.  If you exceed the number, then
	 * you should wait another frame before playing a sound.
	 * 
	 * @return the maximum amount of sounds per animation frame
	 */
	public int getFrameLimit() {
		return frameLimit;
	}
	
	/**
	 * Sets the maximum amount of sounds per animation frame
	 * 
	 * Because of Box2d limitations in LibGDX, you might get a lot of simultaneous
	 * sounds if you try to play sounds on collision.  This in turn can cause
	 * distortions.  We fix that by putting an upper bound on the number of 
	 * simultaneous sounds per animation frame.  If you exceed the number, then
	 * you should wait another frame before playing a sound.
	 * 
	 * @param value the maximum amount of sounds per animation frame
	 */
	public void setFrameLimit(int value) {
		frameLimit = value;
	}

	/// Sound Management
	/**
	 * Uses the asset manager to allocate a sound
	 * 
	 * All sound assets are managed internally by the controller.  Do not try 
	 * to access the sound directly.  Use the play and stop methods instead.
	 * 
	 * @param manager  A reference to the asset manager loading the sound
	 * @param filename The filename for the sound asset
	 */
	public void allocate(AssetManager manager, String filename) {
		Sound sound = manager.get(filename,Sound.class);
		soundbank.put(filename,sound);
	}

	/**
	 * Plays the an instance of the given sound
	 * 
	 * A sound is identified by its filename.  You can have multiple instances of the
	 * same sound playing.  You use the key to identify a sound instance.  You can only
	 * have one key playing at a time.  If a key is in use, the existing sound may
	 * be garbage collected to allow you to reuse it, depending on the settings.
	 * 
	 * However, it is also possible that the key use may fail.  In the latter case,
	 * this method returns false.  In addition, if the sound is currently looping,
	 * then this method will return true but will not stop and restart the sound.
	 * 
	 * 
	 * @param key		The identifier for this sound instance
	 * @param filename	The filename of the sound asset
	 * @param loop		Whether to loop the sound
	 * 
	 * @return True if the sound was successfully played
	 */
	public boolean play(String key, String filename, boolean loop) {
		return play(key,filename,loop,1.0f);
	}

	/**
	 * Plays the an instance of the given sound
	 * 
	 * A sound is identified by its filename.  You can have multiple instances of the
	 * same sound playing.  You use the key to identify a sound instance.  You can only
	 * have one key playing at a time.  If a key is in use, the existing sound may
	 * be garbage collected to allow you to reuse it, depending on the settings.
	 * 
	 * However, it is also possible that the key use may fail.  In the latter case,
	 * this method returns false.  In addition, if the sound is currently looping,
	 * then this method will return true but will not stop and restart the sound.
	 * 
	 * 
	 * @param key		The identifier for this sound instance
	 * @param filename	The filename of the sound asset
	 * @param loop		Whether to loop the sound
	 * @param volume	The sound volume in the range [0,1]
	 * 
	 * @return True if the sound was successfully played
	 */
	public boolean play(String key, String filename, boolean loop, float volume) {
		// Get the sound for the file
		if (!soundbank.containsKey(filename) || current >= frameLimit) {
			return false;
		}

		// If there is a sound for this key, stop it
		Sound sound = soundbank.get(filename);
		if (actives.containsKey(key)) {
			ActiveSound snd = actives.get(key);
			if (!snd.loop && snd.lifespan > cooldown) {
				// This is a workaround for the OS X sound bug
				//snd.sound.stop(snd.id);
				snd.sound.setVolume(snd.id, 0.0f); 
			} else {
				return true;
			}
		}
		
		// Play the new sound and add it
		long id = sound.play(volume);
		if (id == -1) {
			return false;
		} else if (loop) {
			sound.setLooping(id, true);
		}
		
		actives.put(key,new ActiveSound(sound,id,loop));
		current++;
		return true;
	}
	
	/**
	 * Stops the sound, allowing its key to be reused.
	 * 
	 * This is the only way to stop a sound on a loop.  Otherwise it will
	 * play forever.
	 * 
	 * If there is no sound instance for the key, this method does nothing.
	 * 
	 * @param key	The sound instance to stop.
	 */
	public void stop(String key) {
		// Get the active sound for the key
		if (!actives.containsKey(key)) {
			return;
		}
		
		ActiveSound snd = actives.get(key);
		
		// This is a workaround for the OS X sound bug
		//snd.sound.stop(snd.id);
		snd.sound.setLooping(snd.id,false); // Will eventually garbage collect
		snd.sound.setVolume(snd.id, 0.0f); 
		actives.remove(key);
	}
	
	/**
	 * Returns true if the sound instance is currently active
	 * 
	 * @param key	The sound instance identifier
	 * 
	 * @return true if the sound instance is currently active
	 */
	public boolean isActive(String key) {
		return actives.containsKey(key);
	}
	
	/**
	 * Updates the current frame of the sound controller.
	 * 
	 * This method serves two purposes.  First, it allows us to limit the number
	 * of sounds per animation frame.  In addition it allows us some primitive
	 * garbage collection.
	 */
	public void update() {
		for(String key : actives.keys()) {
			ActiveSound snd = actives.get(key);
			snd.lifespan++;
			if (snd.lifespan > timeLimit) {
				collection.add(key);
				snd.sound.setLooping(snd.id,false); // Will eventually garbage collect
				snd.sound.setVolume(snd.id, 0.0f); 
			}
		}
		for(String key : collection) {
			actives.remove(key);
		}
		collection.clear();
		current = 0;
	}

}
