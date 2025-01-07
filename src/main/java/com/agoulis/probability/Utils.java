package com.agoulis.probability;

import java.util.Random;

import com.agoulis.common.tree.Pair;
import com.agoulis.common.utils.Check;

import java.util.*;
import java.util.stream.*;

public class Utils {
	// https://docs.oracle.com/javase/8/docs/api/java/util/Random.html
	static Random random = new Random();

	final static double twoPi = 2 * Math.PI;
	final static double halfPi = Math.PI / 2;
	final static double thirdPi = Math.PI / 3;
	final static double quoterPi = Math.PI / 4;
	final static double sixthPi = Math.PI / 6;
	
	// 2 dice roll
	/**
	 * Returns a Pair of Integers, which contain 2 random numbers from 1 to 6.
	 * Imitates 2 dice roll.
	 * 
	 * Pair.getKey() and Pair.getValue() return first and second dice values.
	 * 
	 * @return Pair<Integer, Integer>
	 */
	public static Pair<Integer, Integer> roll() {
		return new Pair<Integer, Integer>(random.nextInt(6) + 1, random.nextInt(6) + 1);
	}

	// Randomizations for Obtuse triangle problem
	// ----- Figures
	// ---------- Rectangle
	/**
	 * Random point inside the (maxX - minX) by (maxY - minY) rectangle
	 * 
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @return pair of double coordinates
	 */
	public static double[] randomPointInRectangle(double minX, double maxX, double minY, double maxY) {
		double x = minX + Math.random() * (maxX - minX);
		double y = minY + Math.random() * (maxY - minY);
		return new double[] { x, y };
	}

	// ---------- Ellipse
	/**
	 * Random point inside the a and b radius ellipse
	 * 
	 * @param a
	 * @param b
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	public static double[] randomPointInEllipse(double a, double b, double centerX, double centerY) {
		double angle = Math.random() * twoPi;
		double r = Math.sqrt(random.nextDouble());
		double x = centerX + a * r * Math.cos(angle);
		double y = centerY + b * r * Math.sin(angle);

		return new double[] { x, y };
	}

	// ----- Polar coordinates
	// ---------- Normal distribution: ρ - Gaussian, θ - Uniform
	public static double[] randomPolarPointNormal() {
		double ρ = Math.abs(random.nextGaussian());
		double θ = random.nextDouble() * twoPi;

		double[] polar = { ρ, θ };
		double[] cartesian = polarToCartesian(polar);

		return cartesian;
	}

	// ---------- Controlled Normal distribution: ρ - Gaussian(mean, σ), θ - Uniform
	public static double[] randomPolarControledNormal(double mean, double variance) {
		double ρ = Math.abs(nextGaussian(mean, variance));
		double θ = random.nextDouble() * twoPi;

		double[] polar = { ρ, θ };
		double[] cartesian = polarToCartesian(polar);

		return cartesian;
	}

	// ---------- Infinite plane: ρ - Uniform infinite, θ - Uniform [0, 2π]
	public static double[] randomPolarPointInfinite(double dvdN, double dvdX, double dvsN, double dvsX) {
		double dvd = randomInRange(dvdN, dvdX);
		double dvs = randomInRange(dvsN, dvsX);

		double ρ = Math.sqrt(dvd / dvs);
		double θ = random.nextDouble() * twoPi;

		double[] polar = { ρ, θ };
		double[] cartesian = polarToCartesian(polar);

		return cartesian;
	}

	// ---------- Fractal plane: depth - recursion depth, base - the squares at each
	// depth
	public static double[] randomFractalSquarePoint(int depth) {
		double x = nextFractal(depth);
		double y = nextFractal(depth);

		double[] descartes = { x, y };
		return descartes;
	}

	private static double nextFractal(int depth) {
		double r = 0.0; // randomInRange(-depth, depth); // was -1, 1

		while (depth-- > 0) {
			double l = randomInRange(-1, 1);
			r += l * depth;
		}
		return r;
	}

	// ---------- Bertrand 2nd method (normal(?)): ρ - Normal (?) [0, 1], θ -
	// Uniform [0, 2π]
	public static double[] randomPolarPointBertrand() {
		double ρ = random.nextDouble();
		double θ = random.nextDouble() * twoPi;

		double[] polar = { ρ, θ };
		double[] cartesian = polarToCartesian(polar);

		return cartesian;
	}

	/**
	 * Calculates distance between p1 and p2 points on Cartesian coordinate system
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double distance(double[] p1, double[] p2) {
		double dx = p1[0] - p2[0];
		double dy = p1[1] - p2[1];
		return Math.sqrt(dx * dx + dy * dy);
	}

	// Conversion
	/**
	 * Convert polar coordinates to Crtesian
	 * 
	 * @param p
	 * @return
	 */
	public static double[] cartesianToPolar(double[] p) {
		double x = p[0];
		double y = p[1];

		// Calculate r (radius)
		double ρ = Math.sqrt(x * x + y * y);

		// Calculate θ (angle in radians)
		double θ = Math.atan2(y, x);

		// Convert θ to degrees if needed
		// double thetaDegrees = Math.toDegrees(θ);

		double[] d = { ρ, θ };
		return d;
	}

	/**
	 * Convert Cartesian coordinates to polar
	 * 
	 * @param p
	 * @return
	 */
	public static double[] polarToCartesian(double[] p) {
		double ρ = p[0];
		double θ = p[1];
		double x = ρ * Math.cos(θ);
		double y = ρ * Math.sin(θ);
		double[] d = { x, y };

		return d;
	}

	/**
	 * Return random in [min, max] range based on Random.nextDouble(): returns the
	 * next pseudo-random, uniformly distributed double value between 0.0 and 1.0
	 * 
	 * @param min
	 * @param max
	 * @return min + (max - min) * (random)
	 */
	public static double randomInRange(double min, double max) {
		return min + (max - min) * random.nextDouble();
	}

	/**
	 * Uniform in radius [min, max] range. Similar to randomInRange, but takes square root
	 * from the returned double.
	 * 
	 * @param min
	 * @param max
	 * @return value = min + (max - min) * √(random)
	 */
	public static double uniformInRange(double min, double max) {
		return min + (max - min) * Math.sqrt(random.nextDouble());
	}

	public static double normalInRange(double min, double max) {
		return min + (max - min) * Math.abs(random.nextGaussian());
	}

	/**
	 * The random.nextGaussian() returns vales per mean = 0, σ = 1 - standard normal. This 
	 * functions transforms it into mean + √(variance) * standardNormal;
	 * 
	 * @param mean
	 * @param variance
	 * @return
	 */
	public static double nextGaussian(double mean, double variance) {
		// Generate a standard normal random variable
		double standardNormal = random.nextGaussian();

		// Transform the standard normal to the desired distribution
		double transformed = mean + Math.sqrt(variance) * standardNormal;

		// Clamp the value to [0, 1] range
//	        return Math.max(0, Math.min(1, transformed));

		// No Clamp
		return transformed;
	}
}
