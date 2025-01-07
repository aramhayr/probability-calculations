package com.agoulis.probability;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.agoulis.common.tree.Pair;
import com.agoulis.common.utils.Check;
import com.agoulis.common.utils.CommonConstants;
import com.agoulis.common.utils.Table;

public class ObtuseTriangle {
	private final static String[] columns = { Table.columnsRow, "angleS", "angleM", "angleL", "sideS", "sideM", "sideL", "SdM", "MdL",
			"X1", "Y1", "X2", "Y2", "X3", "Y3", "ρ1", "Θ1", "ρ2", "Θ2", "ρ3", "Θ3"};
	private static int columnsCount = columns.length;

	private final static char cX = 'X';
	private final static char cY = 'Y';
	private final static char pρ = 'ρ';
	private final static char pΘ = 'Θ';

	static Table<Double> table = null;
	
	static Color dotColor = Color.GRAY;
	static Color obtColor = Color.RED;
	
	static Color colorA = dotColor;
	static Color colorB = dotColor;
	static Color colorC = dotColor;
	
	static String diagramSuffix = "";
	private static String metricsFolder;
	
	private static double p = 0.0; // Probability of acute triangle

//	// Triangle
//	static double[] A = { 0, 0 };
//	static double[] C = { 0, 0 };
//	static double[] B = { 0, 0 };
//
//	// Trapezoid
//	static double a = 0;
//	static double b = 1;
//	static double h = 0;
//	static double y0 = 1;

	// Rectangle
	static double minX = 0;
	static double maxX = 1;
	static double minY = 0;
	static double maxY = 1;

	// Ellipse
	static double sja = 1; // major
	static double sna = 1; // manor
	static double centerX = 0;
	static double centerY = 0;

	// Controlled Normal
	static double mean = 0;
	static double σ = 1;

	// Fractal
	static int depth = 4;

	// Infinite plane
	static double dvdN = 0;
	static double dvdX = 1;
	static double dvsN = 0;
	static double dvsX = 0.03;

	// S method
	static double infinity = 137;

	// To add up big angleSteps for -G mode - generating triangles 
	static double angleStep = 0.0;
	static double increment = 0.0;
	static char rotate = 'n';
	static char random  = 'n';


	// Coordinates
	static double[] vertexA = null;
	static double[] vertexB = null;
	static double[] vertexC = null;

	private static void randomInFigure(String figure) {
		switch (figure) {
		case "-r": // Rectangle: (minX, maxX, minY, maxY)
			vertexA = Utils.randomPointInRectangle(minX, maxX, minY, maxY);
			vertexB = Utils.randomPointInRectangle(minX, maxX, minY, maxY);
			vertexC = Utils.randomPointInRectangle(minX, maxX, minY, maxY);
			break;

		case "-e": // Ellipse (same as Bertrand 3-rd (uniform) with 1, 1, 0, 0)
			vertexA = Utils.randomPointInEllipse(sna, sja, centerX, centerY); 
			vertexB = Utils.randomPointInEllipse(sna, sja, centerX, centerY); 
			vertexC = Utils.randomPointInEllipse(sna, sja, centerX, centerY);
			break;

		case "-b": // Bertrand 2-nd
			vertexA = Utils.randomPolarPointBertrand();
			vertexB = Utils.randomPolarPointBertrand();
			vertexC = Utils.randomPolarPointBertrand();
			break;

		case "-n": // Normal
			vertexA = Utils.randomPolarPointNormal();
			vertexB = Utils.randomPolarPointNormal();
			vertexC = Utils.randomPolarPointNormal();
			break;

		case "-f": // Normal
			vertexA = Utils.randomFractalSquarePoint(depth);
			vertexB = Utils.randomFractalSquarePoint(depth);
			vertexC = Utils.randomFractalSquarePoint(depth);
			break;

		case "-c": // Controlled Normal: mean, σ
			vertexA = Utils.randomPolarControledNormal(mean, σ);
			vertexB = Utils.randomPolarControledNormal(mean, σ);
			vertexC = Utils.randomPolarControledNormal(mean, σ);
			break;
			
		case "-s": // S method
			vertexA = new double[] { 1, 0 };
			vertexB = new double[] { 0, 0 };
			vertexC = randomPolarPointSMethod(infinity);
			break;

		case "-m": // M method
			vertexA = randomRejectPointMMethod();
			vertexB = new double[] { 1, 0 };
			vertexC = new double[] { 2, 0 };
			break;

		case "-l": // L method
			vertexA = new double[] { 0, 0 };
			vertexB = randomRejectPointLMethod();
			vertexC = new double[] { 1, 0 };
			break;

		case "-i": // Infinite plane
			vertexA = Utils.randomPolarPointInfinite(dvdN, dvdX, dvsN, dvsX);
			vertexB = Utils.randomPolarPointInfinite(dvdN, dvdX, dvsN, dvsX);
			vertexC = Utils.randomPolarPointInfinite(dvdN, dvdX, dvsN, dvsX);
			break;
			
		case "-G": // Infinite plane
			double [] coordinates = randomGenerateTriangles(increment, sna, sja, rotate, random);

			vertexA = new double [] {coordinates[0], coordinates[1]};
			vertexB = new double [] {coordinates[2], coordinates[3]};
			vertexC = new double [] {coordinates[4], coordinates[5]};

			increment += angleStep; // Increment angleA towards PI
			break;
			

		default:
			throw new IllegalArgumentException("Invalid figure code");
		}
	}

	private static String commaSeparatedColumnNames(int start, int end) {
		int stop = end < columns.length ? end : columns.length;
		String comma = "";
		String out = comma;
		for (int i = start; i < stop; i++) {
			out += comma + columns[i];
			comma = ",";
		}
		return out;
	}

	/**
	 * Calculates metrics for each column of random variables of each column and
	 * creates a row of these metrics as a comma separated values (csv)
	 * 
	 * Returns a String of lines for each original column of random variables
	 * 
	 */
	public static String tableStatistics() throws IOException {
		String statistics = Table.columnsRow + "," + StatsUtils.names + "\n";
		PointsDistribution pd = new PointsDistribution(100, 10, 1, Color.BLACK, Color.WHITE);

		List<Double> coordinateX = new ArrayList<Double>();
		List<Double> coordinateY = new ArrayList<Double>();
		List<Double> polarR = new ArrayList<Double>(); // polar radius
		List<Double> polarA = new ArrayList<Double>(); // polar angle
		String column = "";
		try {
			for (int i = 1; i < columns.length; i++) {
				column = columns[i];
				if (!column.equals(Table.columnsRow)) {
					List<Double> cols = table.getColumn(column);
					statistics += column + "," + StatsUtils.statistics(cols) + "\n";
					// For adding variable coordinates only
					boolean skip = setDoNotSkip(column);
					if (column.charAt(0) == cX) {
						if (skip) {
							coordinateX.addAll(cols);
						}
						continue;
					}
					if (column.charAt(0) == cY) {
						if (skip) {
							coordinateY.addAll(cols);
						}
						continue;
					}
					if (column.charAt(0) == pρ) {
						if (skip) {
							polarR.addAll(cols);
						}
						continue;
					}
					if (column.charAt(0) == pΘ) {
						if (skip) {
							polarA.addAll(cols);
						}
						continue;
					}
					pd.saveDiagram(cols, metricsFolder, column + "-" + diagramSuffix);
				}
			}
			column = "" + cX;
			pd.saveDiagram(coordinateX, metricsFolder, column + "-" + diagramSuffix);
			column = "" + cY;
			pd.saveDiagram(coordinateY, metricsFolder, column + "-" + diagramSuffix);
			column = "" + pρ;
			pd.saveDiagram(polarR, metricsFolder, column + "-" + diagramSuffix);
			column = "" + pΘ;
			pd.saveDiagram(polarA, metricsFolder, column + "-" + diagramSuffix);

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Error writing " + column + "-" + diagramSuffix + " to " + metricsFolder);
		}
		return statistics;
	}

	private static boolean setDoNotSkip(String column) {
		boolean Smethod = CalculationRoutines.mode.equals("-s");
		boolean Mmethod = CalculationRoutines.mode.equals("-m");
		boolean Lmethod = CalculationRoutines.mode.equals("-l");
		
		boolean vertexSmall = column.charAt(1) == '3';
		boolean vertexMedium = column.charAt(1) == '1';
		boolean vertexLarge = column.charAt(1) == '2';
		
		return !(Smethod || Mmethod || Lmethod) || (Smethod && vertexSmall) || (Mmethod && vertexMedium) || (Lmethod && vertexLarge); 
	}

	/**
	 * Calculate probability of obtuse triangle from the probability of cute
	 * 
	 * @return probability of obtuse triangle
	 */
	public static double getProbability() {
		return 1 - p;
	}

	public static double calculateProbabilitiy(String figure, int tries) {
		List<String> values = new ArrayList<String>();
		String out = commaSeparatedColumnNames(0, columnsCount);
		int i = 0;

		values.add(out);
		int row = tries;

		while (row-- > 0) {
			randomInFigure(figure); // populate all three: vertexA, vertexB, and vertexC coordinates

			colorA = dotColor;
			colorB = dotColor;
			colorC = dotColor;
			
			out = row + ", " + tableRow(vertexA, vertexB, vertexC);
			if (CalculationRoutines.saveDistributionImage) {
				CalculationRoutines.pd.pixel(vertexA, colorA);
				CalculationRoutines.pd.pixel(vertexB, colorB);
				CalculationRoutines.pd.pixel(vertexC, colorC);
			}
			values.add(out);
		}

		table = new Table<Double>(values, Double.class);
//		double sum = 0.0;

		int acute = 0;
		List<String> acuteList = new ArrayList<String>();

		if (CalculationRoutines.verbose) {
			System.out.println(columns[0] + " [" + commaSeparatedColumnNames(1, 4) + "]");
		}

		for (i = 0; i < tries; i++) {
			String r = "" + i;
			if (CalculationRoutines.verbose) {
				System.out.println("Row=" + r + " Values [" + table.getValue(r, columns[1]) + ", "
						+ table.getValue(r, columns[2]) + ", " + table.getValue(r, columns[3]) + "]");
			}
			// Because of sorting columns[3] in the large angle
			if (table.getValue(r, columns[3]) < Utils.halfPi) {
				++acute;
				acuteList.add(r);
			}
		}
		p = ((double) acute) / ((double) tries);
		p = StatsUtils.round(p, StatsUtils.probabilityAccuracy);
		if (CalculationRoutines.verbose) {
			System.out.println(table.toString());
			System.out.println("Rows acute " + acuteList.toString());
			System.out.println("Probability acute " + p);
		}
		return p;
	}

// Perplexity
	public static String tableRow(double[] pa, double[] pb, double[] pc) {
		double[] sides = sides(pa, pb, pc);
		double[] angles = angles(sides[0], sides[1], sides[2]);

		double angleA = angles[0];
		double angleB = angles[1];
		double angleC = angles[2];
		
		if (CalculationRoutines.verbose) {
			System.out.println("X coordinates - A, B, C = " + pa[0] + ","+ pb[0] + ","+ pc[0]);
			System.out.println("Y coordinates - A, B, C = " + pa[1] + ","+ pb[1] + ","+ pc[1]);
			System.out.println("Angles - A, B, C = " + angleA + ","+ angleB + ","+ angleC);
			if (angleA > angleB || angleC > angleB ) {
				System.out.println("===============>>>>>>>>>>>>>>>>>");				
			}
		}
/*		
		if (angleA > Utils.halfPi) {
			colorA = obtColor;
		} else if (angleB > Utils.halfPi) {
			if (CalculationRoutines.mode.equals("-s")) {
				colorC = obtColor;
//				if (angleA > Utils.halfPi) { // When m > l
//					colorC = obtColor;					
//				}
			} else if (CalculationRoutines.mode.equals("-m")) {
				colorA = obtColor;
			} else {
				colorB = obtColor;
			}
		} else if (angleC > Utils.halfPi) {
			colorC = obtColor;
		}
*/
		boolean obtuse = false;
		if (angleA > Utils.halfPi) {
			obtuse = true;
			colorA = obtColor;	
		}

		if (angleB > Utils.halfPi) {
			obtuse = true;
			colorB = obtColor;	
		}

		if (angleC > Utils.halfPi) {
			obtuse = true;
			colorC = obtColor;	
		}

		if (obtuse == true && CalculationRoutines.mode.equals("-s")) {
			colorC = obtColor;
		}

		if (obtuse == true && CalculationRoutines.mode.equals("-m")) {
			colorA = obtColor;
		}


//		double[] sides = { a, b, c };
		double[] sidesSorted = sortAscending(sides);

//		double[] angles = { angleA, angleB, angleC };
		double[] anglesSorted = sortAscending(angles);
		
		double[] p1 = Utils.cartesianToPolar(pa);
		double[] p2 = Utils.cartesianToPolar(pb);
		double[] p3 = Utils.cartesianToPolar(pc);

		return elementsToString(anglesSorted) + "," + elementsToString(sidesSorted) + ","
				+ sidesSorted[0] / sidesSorted[1] + "," + sidesSorted[1] / sidesSorted[2] + ","
				+ coordinatesToString(pa, pb, pc) + "," + coordinatesToString(p1, p2, p3);
	}

	public static double[] sortAscending(double[] array) {
		// Create a copy of the input array to avoid modifying the original
		double[] sortedArray = Arrays.copyOf(array, array.length);

		// Sort the array in ascending order
		Arrays.sort(sortedArray);

		return sortedArray;
	}

	// ---------- Random 3rd vertex for S-method, Uniform: ρ - [0, 1], θ - [0, 2π]
	/**
	 * S method with reject
	 * 
	 * @return
	 */
	public static double[] randomPolarPointSMethod(double infinity) {
		double ρ = Math.sqrt(Utils.randomInRange(1, infinity));;
		double θ = Utils.randomInRange(Utils.thirdPi, Math.PI);

//		if (θ < halfPi) {
//			while (ρ * Math.cos(θ) > 0.5) {
//				ρ = Math.sqrt(randomInRange(1, infinity));;
//				θ = randomInRange(thirdPi, Math.PI);				
//			}
//		}
//		
		double[] polar = { ρ, θ };
		double[] cartesian = Utils.polarToCartesian(polar);

		return cartesian;
	}

	// ---------- Random 3rd vertex for S-method, Uniform: ρ - [0, 1], θ - [0, 2π]
	/**
	 * S method with reject
	 * 
	 * @return
	 */
	public static double[] randomPolarPointSMethodFixedML(double infinity) {
		double ρ = Math.sqrt(Utils.randomInRange(1, infinity));;
		double θ = Utils.randomInRange(Utils.thirdPi, Math.PI);

		if (θ < Utils.halfPi) {
			while (ρ * Math.cos(θ) > 0.5) {
				ρ = Math.sqrt(Utils.randomInRange(1, infinity));;
				θ = Utils.randomInRange(Utils.thirdPi, Math.PI);				
			}
		}
		
		double[] polar = { ρ, θ };
		double[] cartesian = Utils.polarToCartesian(polar);

		return cartesian;
	}

	// ---------- Random 3rd vertex for M-method, Uniform: ρ - [1, 2], θ - [0, 2/3π]
	/**
	 * M method
	 * 
	 * @return
	 */
	public static double[] randomPolarPointMMethod() {
		double θ = Utils.randomInRange(0, Utils.thirdPi);
		double ρ = Utils.randomInRange(1.0, (1.0 + Math.cos(2 * θ)) / Math.cos(θ)); // Inside BDF figure on Diagram 3

		double[] polar = { ρ, θ };
		double[] cartesian = Utils.polarToCartesian(polar);

		return cartesian;
	}

	// ---------- Random triangle 3rd vertex for L-method, Uniform: ρ - [0.5 ⋆
	// cos(θ), 1], θ - [0, π/3]
	/**
	 * L method
	 * 
	 * Generates a random point on a 0.5, 1, 0, sin(π/3) then rejects the points that are
	 * beyond the r=1 circle.
	 * 
	 * @return
	 */
	public static double[] randomRejectPointLMethod() {
		double[] polar = Utils.cartesianToPolar(Utils.randomPointInRectangle(0.5, 1, 0, Math.sin(Utils.thirdPi)));
		while (polar[0] > 1) {
			polar = Utils.cartesianToPolar(Utils.randomPointInRectangle(0.5, 1, 0, Math.sin(Utils.thirdPi)));
		}

		double[] cartesian = Utils.polarToCartesian(polar);
		return cartesian;
	}

	// ---------- Random 3rd vertex for M-method REJECT, Uniform: ρ - [1, 2], θ -
	// [0, 2/3π]
	/**
	 * M method
	 * 
	 * @return
	 */
	public static double[] randomRejectPointMMethod() {
		double[] cartesian = Utils.randomPointInEllipse(1, 1, 1, 0);
		double[] shiftedPolar = Utils.cartesianToPolar(new double[] { cartesian[0] - 2, cartesian[1] });

		while (shiftedPolar[1] > Utils.halfPi || shiftedPolar[1] < (2 * Utils.thirdPi) && shiftedPolar[0] < 1) {
			cartesian = Utils.randomPointInEllipse(1, 1, 1, 0);
			shiftedPolar = Utils.cartesianToPolar(new double[] { cartesian[0] - 2, cartesian[1] });
		}

		return new double[] { Math.abs(cartesian[0]), Math.abs(cartesian[1]) };
	}

	// ---------- Generate triangles
	/**
	 * Generate triangles per simplest method
	 * 
	 * @return
	 */
	public static double[] randomGenerateTriangles(double angleAugment, double a, double b, char rotate, char random) {
		double maxSide = random == 'y' ? Utils.randomInRange(0, 1) : 1;
		double angleA = Utils.thirdPi + angleAugment;

		double[] cartesian = Utils.randomPointInEllipse(a, b, 0, 0);
		double xA = cartesian[0];
		double yA = cartesian[1];

		double anglesBCsum = Math.PI - angleA;
		double lowerBound = (anglesBCsum > angleA) ? (anglesBCsum - angleA) : 0;
		double upperBound = (anglesBCsum > angleA) ? angleA : anglesBCsum;
		double angleB = Utils.randomInRange(lowerBound, upperBound); // see expression [3] derivation on u-line
		double angleC = Math.PI - angleA - angleB;

		if (equal(angleB, angleA)) {
			angleB = angleA;
		}

		if (equal(angleB, angleC)) {
			angleC = angleB;
		}
		// Per the Law of Sines, sin(angleA) / a = sin(angleB) / b = sin(angleC) / c
//		double sideBC = Math.abs(maxSide * Math.sin(angleA));

		double sideBC = maxSide;
		double sideAC = 0;
		double sideAB = 0;
		if (angleA != Math.PI) {
			sideAC = Math.abs(maxSide * (Math.sin(angleC) / Math.sin(angleA)));
			sideAB = Math.abs(maxSide * (Math.sin(angleB) / Math.sin(angleA)));
		} else {
			sideAC = Utils.randomInRange(0, 1);
			sideAB = 1 - sideAC;
			angleB = 0;
			angleC = 0;
		}
		
//		double lengthAB = randomInRange(sideLower, sideUpper);
//		double lengthAC = calculateThirdSide(angleA, lengthAB, maxSide);

		double[] coordinates = calculateTriangleCoordinates(angleA, xA, yA, sideAC, sideAB);

		double[] triangle = { xA, yA, coordinates[0], coordinates[1], coordinates[2], coordinates[3] };
		if (rotate == 'y') {
			double radians = Utils.randomInRange(0, Utils.twoPi);
			triangle = rotate(triangle, radians);
		}

		if (CalculationRoutines.verbose) {
			double[] vertexA = new double[] { triangle[0], triangle[1] };
			double[] vertexB = new double[] { triangle[2], triangle[3] };
			double[] vertexC = new double[] { triangle[4], triangle[5] };

			double[] sides = sides(vertexA, vertexB, vertexC);

			double[] angles = angles(sides[0], sides[1], sides[2]);

			System.out.printf(">>>>>>>>>>>>> Designed vs Calculated <<<<<<<<<<<<<<<<<<<<\n");

			System.out.printf("++++Sides designed (AB, AC, BC) \t[%.5f, %.5f, %.5f]%n", sideAB, sideAC, sideBC);
			System.out.printf("++++Sides calculated (a, b, c) \t[%.5f, %.5f, %.5f]%n", sides[0], sides[1], sides[2]);
			System.out.printf("------Angles designed (A, B, C) \t[%.5f, %.5f, %.5f], sin(A)=%.5f, sin(B)=%.5f, sin(C)=%.5f%n", angleA, angleB, angleC, Math.sin(angleA), Math.sin(angleB), Math.sin(angleC));
			System.out.printf("------Angles calculated (a, b, c) \t[%.5f, %.5f, %.5f], sin(a1)=%.5f, sin(a2)=%.5f, sin(a3)=%.5f%n", angles[0], angles[1],
					angles[2], Math.sin(angles[0]), Math.sin(angles[1]), Math.sin(angles[2]));
		}
		Check.assertTrue(Check.ast, "sideBC = " + sideBC + " is equal to " + "maxSide = " + maxSide,
				sideBC == maxSide);

		Check.assertFalse(Check.ast, "sideAC = " + sideAC + " is bigger than " + "maxSide = " + maxSide,
				sideAC > maxSide);

		Check.assertFalse(Check.ast, "sideAB = " + sideAB + " is bigger than " + "maxSide = " + maxSide,
				sideAB > maxSide);

		return triangle;
	}

	public static boolean equal(double a, double b) {
		// 15 digits after decimal point
		return Math.abs(a - b) < 0.000000000000001 ? true : false;
	}

	public static double[] rotate(double[] triangle, double radians) {
		Check.assertTrue(Check.pre, "There are " + triangle.length + " coordinates for a triangle. " + "Should be 6",
				triangle.length == 6);

		double x1 = triangle[0], y1 = triangle[1];
		double x2 = triangle[2], y2 = triangle[3];
		double x3 = triangle[4], y3 = triangle[5];

		// Compute sine and cosine of the angle
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);

		// Rotate point 2
		double x2New = x1 + (x2 - x1) * cos + (y2 - y1) * sin;
		double y2New = y1 - (x2 - x1) * sin + (y2 - y1) * cos;

		// Rotate point 3
		double x3New = x1 + (x3 - x1) * cos + (y3 - y1) * sin;
		double y3New = y1 - (x3 - x1) * sin + (y3 - y1) * cos;

		// Return the new coordinates
		return new double[] { x1, y1, x2New, y2New, x3New, y3New };
	}

	public static double[] calculateTriangleCoordinates(double angleA, double xA, double yA, double lengthAB,
			double lengthAC) {
		// Calculate coordinates of point B
		double xB = xA + lengthAB * Math.cos(angleA / 2);
		double yB = yA + lengthAB * Math.sin(angleA / 2);

		// Calculate coordinates of point C
		double xC = xA + lengthAC * Math.cos(-angleA / 2);
		double yC = yA + lengthAC * Math.sin(-angleA / 2);

		// Store results in arrays
		double[] coordinates = { xB, yB, xC, yC };

		// Print results
		if (CalculationRoutines.verbose) {
			System.out.printf("Coordinates of B: (%.2f, %.2f)%n", coordinates[0], coordinates[1]);
			System.out.printf("Coordinates of C: (%.2f, %.2f)%n", coordinates[2], coordinates[3]);
		}

		return coordinates;
	}

	public static double calculateThirdSide(double angleA, double lengthAB, double lengthBC) {
		// Convert angle to radians if it's in degrees
		// double angleInRadians = Math.toRadians(angleA);
		// If angleA is already in radians, use it directly

		// Apply the law of cosines
		double lengthAC = Math
				.sqrt(lengthAB * lengthAB + lengthBC * lengthBC - 2 * lengthAB * lengthBC * Math.cos(angleA));

		return lengthAC;
	}

	// Takes triangle vertices coordinates and calculates sides
	public static double[] sides(double[] pa, double[] pb, double[] pc) {
		double a = Utils.distance(pb, pc);
		double b = Utils.distance(pa, pc);
		double c = Utils.distance(pa, pb);

		return new double[] { a, b, c };
	}

	// Takes triangle side lengths and calculates angles
	public static double[] angles(double a, double b, double c) {
		double angleA = Math.acos((b * b + c * c - a * a) / (2 * b * c));
		double angleB = Math.acos((a * a + c * c - b * b) / (2 * a * c));
		double angleC = Math.PI - angleA - angleB;

		return new double[] { angleA, angleB, angleC };
	}

	public static void setFileNamePrefix(String mF, String fN) {
		metricsFolder = mF;
		diagramSuffix = "diagram-" + fN;
	}
	
	// Helper method to reverse the array
	private static void reverseArray(double[] array) {
		int left = 0;
		int right = array.length - 1;

		while (left < right) {
			// Swap elements
			double temp = array[left];
			array[left] = array[right];
			array[right] = temp;

			// Move towards the center
			left++;
			right--;
		}
	}

	private static String coordinatesToString(double[] pa, double[] pb, double[] pc) {
		return String.format("%.6f,%.6f,%.6f,%.6f,%.6f,%.6f", pa[0], pa[1], pb[0], pb[1], pc[0], pc[1]);
	}

	private static String elementsToString(double[] elements) {
		double[] sorted = sortAscending(elements);

		return String.format("%.6f,%.6f,%.6f", sorted[0], sorted[1], sorted[2]);
	}
}
