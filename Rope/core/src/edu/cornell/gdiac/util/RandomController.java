/*
 * RandomController.java
 *
 * Static controller class for producing random numbers.
 *
 * Just about every part of the game needs random numbers.  If we were to
 * make this in instance class, we would have references littered throughout
 * our code.  Forunately, we usually only need one random number generator,
 * so it can be a singleton.  Once again, we have implemented the
 * singleton as a static class.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.util;

import java.util.Random;

/**
 * Static class for producing random numbers.
 */
public class RandomController {
	/** Pseudo-random number generator */
	private static Random generator = new Random(0); // Make it deterministic

	/**
	 * Returns a random int between min and max (inclusive).
	 *
	 * @param min Minimum value in random range
	 * @param max Maximum value in random range
	 *
	 * @return a random int between min and max (inclusive).
	 */
	public static int rollInt(int min, int max) {
		return generator.nextInt(max-min+1)+min;
	}

	/**
	 * Returns a random float between min and max (inclusive).
	 *
	 * @param min Minimum value in random range
	 * @param max Maximum value in random range
	 *
	 * @return a random float between min and max (inclusive).
	 */
	public static float rollFloat(float min, float max) {
		return generator.nextFloat() * (max - min) + min;
	}
}
