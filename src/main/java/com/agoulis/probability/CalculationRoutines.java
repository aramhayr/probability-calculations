package com.agoulis.probability;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.agoulis.common.tree.Pair;
import com.agoulis.common.utils.CommonConstants;
import com.agoulis.common.utils.FileSystemException;
import com.agoulis.common.utils.LocaleUtils;
import com.agoulis.common.utils.PersistentString;

public class CalculationRoutines {
	static List<String> results = null;
	static boolean verbose = false;
	static boolean saveRaw = false;
	static boolean saveDistributionImage = false;
	static String mode = "-x";
	static int repetitions = 0;
	static PointsDistribution pd = null; /* new PointsDistribution(3.0, 3.0, 0.5, Color.BLACK, Color.WHITE); */

	static List<Pair<String, Double>> parameters = null;

	static int skipPercent = 20; // 100 means that 1% of points (every hundredth) of points will be displayed

	private final static String outputFolder = "data";
	final static String metricsFolder = outputFolder + CommonConstants.fsep + "distribution";
	final static String statsFolder = outputFolder + CommonConstants.fsep + "statistics";
	final static String histograms = outputFolder + CommonConstants.fsep + "histograms";
// Debug 
	static boolean debug = false;
	static int ghalfPi = 0;
	static int lhalfPi = 0;

	static int gx0 = 0;
	static int lx0 = 0;

	public static void main(String... args) {
		if (args.length < 2) {
			printUsage();
			System.exit(1);
		}

		mode = args[0];
		String arg = args[0];
		if (arg.equals("-h")) {
			printUsage();
			System.exit(0);
		}
		repetitions = PersistentString.toInteger(args[1]);
		initialize();
		mode = arg.replace("v", "");

		if (mode.length() < arg.length()) {
			verbose = true;
		}

		mode = mode.replace("t", "");

		if (mode.length() < arg.length()) {
			saveRaw = true;
		}
		arg = mode;
		mode = mode.replace("o", "");

		if (mode.length() < arg.length()) {
			saveDistributionImage = true;
		}

		try {
			if (verbose) {
				System.out.println("mode: " + mode);
				System.out.println("2π  =\t" + Utils.twoPi);
				System.out.println("π   =\t" + Math.PI);
				System.out.println("π/2 =\t" + Utils.halfPi);
				System.out.println("π/3 =\t" + Utils.thirdPi);
				System.out.println("π/4 =\t" + Utils.quoterPi);
				System.out.println("π/6 =\t" + Utils.sixthPi);
			}

			runCalculation(mode, args);
		} catch (IllegalArgumentException iae) {
			System.err.println(iae.getMessage() + ". Exiting with -2");
			System.exit(-2);
		}
	}

	private static void initialize() {
		results = new ArrayList<String>();
		parameters = new ArrayList<Pair<String, Double>>();
		verbose = false;
		saveRaw = false;
		saveDistributionImage = false;
		// Debug
		ghalfPi = 0;
		lhalfPi = 0;
	}

	/**
	 * The formula for the n-th step side length: c(n) = √(2+ √(4 - c(n-1)²))
	 * 
	 * Note1: the most accurate value is at step 14 - 3.141592654807589; MacOS
	 * calculator gives 3.14159265. Britannica gives this:
	 * 3.141592653589793238462643383279502884197 step - 13; l =
	 * 3.8349519451455667E-4; s = 3.1415926334632482; step - 14; l =
	 * 1.9174759856003352E-4; s = 3.141592654807589; step - 15; l =
	 * 9.58737989905156E-5; s = 3.1415926453212153; step - 16; l =
	 * 4.793689891625549E-5; s = 3.1415926073757197; step - 17; l =
	 * 2.3968451774136908E-5; s = 3.1415929109396727; step - 18; l =
	 * 1.198423051908566E-5; s = 3.141594125195191;
	 * 
	 * Note2: Looks like double float precision is hitting its limit around step 14.
	 * It collapses at step 29: step - 28; l = 1.4901161193847656E-8; s = 4.0; step
	 * - 29; l = 0.0; s = 0.0;
	 *
	 * @param steps
	 * @param result
	 * @return
	 */
	public static void circlePerimeter(int steps) {
		int current = 0;
		double c = 2; // initial value of the side of polygon
		double s = 0; // perimeter
		double l = c;

		String tmp = "step; polygon side length (l=c=2); s is permeter";
		System.out.println(tmp);

		for (current = 0; current < steps; current++) {
			double coefficient = Math.pow(2, current /* + 1 */); // Adding 1 makes it 2𝝿
			double stepLog = Math.log(coefficient);
			double edgeLog = Math.log(1 / l);

			s = coefficient * l;

			String str = "step - " + current + ";\tl = " + l + ";\ts = " + s + ";\tstepLog = " + stepLog
					+ ";\tedgeLog = " + edgeLog + ";\tdimension = " + stepLog / edgeLog;
			results.add(str);
			l = 4;
			l = Math.sqrt(2 - Math.sqrt(l - Math.pow(c, 2)));

			c = l;
		}
	}

	/**
	 * Rolling dice
	 * 
	 */
	public static void probability(double tries, int value) {
		double requested = 0;
		for (int i = 0; i < tries; i++) {
			Pair<Integer, Integer> dice = Utils.roll();
			if ((int) dice.getKey() + (int) dice.getValue() < value) {
				requested++;
			}
//			String s = "[" + dice.getKey() + ":" + dice.getValue() + "]" + ", requested=" + requested ;
//			System.out.println(s);
		}
		System.out.println("tries=" + tries + ", value=" + value + ", probability=" + requested / tries);
	}

	/**
	 * The formula probability distribution for chord > a (side of equilateral
	 * triangle) distribution = -2/3Δr2 + 1/2 [-2Δr↑2 + 1/3Δr + 1/3] -> looks wrong]
	 * 
	 * @param ring radiusIncrement
	 */
	public static void onRing(double radiusIncrementStep) {
		double distribution = 0.0; // probability distribution at r + Δr
		double radiusIncrement = 0.0; // r + Δr
		int step = 0;

		while (radiusIncrement <= 0.501) {
			distribution = -2.0 / 3.0 * Math.pow(radiusIncrement, 2.0) + 0.5;
			System.out.println(++step + ", \t" + Math.round(radiusIncrement * 1000.0) / 1000.0 + ", \t"
					+ Math.round(distribution * 1000000.0) / 1000000.0);
			radiusIncrement += radiusIncrementStep;
		}
	}

	/**
	 * break A Stick: Parallel
	 * 
	 * @param tries
	 */
	public static void breakAStickParallel(int tries) {
		int s = 0; // success
		for (int i = 0; i < tries; i++) {
			double cut1 = Utils.random.nextDouble(); //
			double cut2 = Utils.random.nextDouble(); //
			if ((cut1 < 0.5 && cut2 > 0.5 && (cut2 - cut1) < 0.5)
					|| (cut2 < 0.5 && cut1 > 0.5 && (cut1 - cut2) < 0.5)) {
				++s;
//				System.out.println(
//						i + ", \t" + Math.round(cut1 * 1000.0) / 1000.0 + ", \t" + Math.round(cut2 * 1000.0) / 1000.0);
			}
		}
		System.out.println("\ntries =\t" + tries + ", success par. = \t" + s + ", p="
				+ Math.round((((double) s) / ((double) tries)) * 1000.0) / 1000.0);
	}

	/**
	 * break A Stick: Sequentially
	 * 
	 * @param tries
	 */
	public static void breakAStickSequentially(int tries) {
		int s = 0; // success
		for (int i = 0; i < tries; i++) {
			double cut1 = Utils.random.nextDouble() * 0.5; //
			double cut2 = Utils.random.nextDouble() * (1.0 - cut1); //
			double cut3 = 1.0 - cut1 - cut2;
			if ((cut1 + cut2) > cut3 && (cut1 + cut3) > cut2 && (cut2 + cut3) > cut1) {
				++s;
			}
//			cut1 = Math.round(cut1 * 1000.0) / 1000.0;
//			cut2 = Math.round(cut2 * 1000.0) / 1000.0;
//			cut3 = Math.round(cut3 * 1000.0) / 1000.0;
//			System.out.println(
//					i + ", \ts" + + s + ",   cut1=" + cut1 + ", cut2=" + cut2 + ", cut3=" + cut3 + ", sum=" + (cut1 + cut2 + cut3));
		}
		System.out.println("\ntries =\t" + tries + ", success seq. = \t" + s + ", p="
				+ Math.round((((double) s) / ((double) tries)) * 1000.0) / 1000.0);
	}

	/**
	 * graphs ratio of a line cut length to the whole and the square of the cut to
	 * the square of the whole
	 * 
	 * @param ring radiusIncrement
	 */
	public static void cutRatioGrowth(double step) {
		double cut = 0; // success
		int s = 0;
		while (cut <= 0.5) {
//			System.out.println("\nstep =\t" + s++ + ", cut = \t" + Math.round(cut*100.0)/100.0 + ", ratio=" + Math.round((cut / (1.0 - cut))*100.0)/100.0);
//			double square = Math.pow(cut, 2.0);
			cut = cut + step;
			double doub = 1 * cut;
			System.out.println(s++ + ", " + Math.round(cut * 100.0) / 100.0 + ", "
					+ Math.round((doub / (1.0 - cut)) * 100.0) / 100.0);
		}
	}

	public static void print() {
		for (String s : results) {
			System.out.println(s);
		}
	}

	private static void runCalculation(String mode, String... properties) {
		System.out.println("=== Calculations started at " + LocaleUtils.daytime());
		switch (mode) {
		case "-π":
			circlePerimeter(PersistentString.toInteger(properties[1])); // takes property = max=25 steps for doubling the number of
																// sides
			break;

		case "-g": // on ring probability
			onRing(PersistentString.toDouble(properties[1])); // takes radiusIncrementStep: double dd = 1.0 divided by double ds =
													// 200.0;
			break;

		case "-d": // die Utils.roll?
			probability(PersistentString.toInteger(properties[1]), PersistentString.toInteger(properties[2])); // properties[1] = tries (e.g.
																							// 5552),
			break; // properties[1] = sum? (e.g. 8)

		case "-p": // brake stick: parallel properties[1] = tries
			breakAStickParallel(PersistentString.toInteger(properties[1])); //
			break;

		case "-q": // brake stick: sequential properties[1] = tries
			breakAStickSequentially(PersistentString.toInteger(properties[1])); //
			break;

		case "-w": //
			cutRatioGrowth(PersistentString.toInteger(properties[1])); // double s = 0.01
			break;

		case "-n": // Normal
		{
			pd = new PointsDistribution(5.0, 5.0, 0.5, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-c": // Controlled Normal: mean, σ
		{
			ObtuseTriangle.mean = PersistentString.toDouble(properties[2]);
			ObtuseTriangle.σ = PersistentString.toDouble(properties[3]);

			parameters.add(new Pair<String, Double>("μ", ObtuseTriangle.mean));
			parameters.add(new Pair<String, Double>("σ", ObtuseTriangle.σ));
			double max = ObtuseTriangle.mean + 2.5*ObtuseTriangle.σ;
			pd = new PointsDistribution(max, max, max/5, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-f": // Controlled Normal: mean, σ
		{
			ObtuseTriangle.depth = PersistentString.toInteger(properties[2]);

			parameters.add(new Pair<String, Double>("dp", (double) ObtuseTriangle.depth /* ObtuseTriangle.mean */));
			
			double max = (ObtuseTriangle.depth + 1)*ObtuseTriangle.depth/2;

			pd = new PointsDistribution(max, max, max/5, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-r": // // Rectangle: (minX, maxX, minY, maxY)
		{
			ObtuseTriangle.minX = PersistentString.toDouble(properties[2]);
			ObtuseTriangle.maxX = PersistentString.toDouble(properties[3]);
			ObtuseTriangle.minY = PersistentString.toDouble(properties[4]);
			ObtuseTriangle.maxY = PersistentString.toDouble(properties[5]);

			parameters.add(new Pair<String, Double>("nX", ObtuseTriangle.minX));
			parameters.add(new Pair<String, Double>("xX", ObtuseTriangle.maxX));
			parameters.add(new Pair<String, Double>("nY", ObtuseTriangle.minY));
			parameters.add(new Pair<String, Double>("xY", ObtuseTriangle.maxY));

			pd = new PointsDistribution(ObtuseTriangle.maxX + 0.2, ObtuseTriangle.maxY + 0.2, 0.4, Color.BLACK,
					Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-e": // Ellipse: (a, b, centerX, centerY)
		{
			ObtuseTriangle.sna = PersistentString.toDouble(properties[2]);
			ObtuseTriangle.sja = PersistentString.toDouble(properties[3]);
			ObtuseTriangle.centerX = PersistentString.toDouble(properties[4]);
			ObtuseTriangle.centerY = PersistentString.toDouble(properties[5]);

			parameters.add(new Pair<String, Double>("n", ObtuseTriangle.sna));
			parameters.add(new Pair<String, Double>("j", ObtuseTriangle.sja));
			parameters.add(new Pair<String, Double>("cX", ObtuseTriangle.centerX));
			parameters.add(new Pair<String, Double>("cY", ObtuseTriangle.centerY));

			double step = ObtuseTriangle.sja / ObtuseTriangle.sna;

			pd = new PointsDistribution(ObtuseTriangle.centerX + ObtuseTriangle.sna + step,
					ObtuseTriangle.centerY + ObtuseTriangle.sja + step, step, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-b": // Bertrand 2-nd
		{
			pd = new PointsDistribution(1.5, 1.5, 0.5, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-s": // S-method: (infinity)
		{
			ObtuseTriangle.infinity = PersistentString.toDouble(properties[2]);
			parameters.add(new Pair<String, Double>("inF", ObtuseTriangle.infinity));
			int infSqrt = (int) Math.sqrt(ObtuseTriangle.infinity + 7);
			pd = new PointsDistribution(infSqrt, infSqrt, infSqrt / 5, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-m": //
		{
			pd = new PointsDistribution(2.5, 2.5, 0.5, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-l": // L method: range θ = [0,thirdPi], ρ =[0.5 / cos(θ), 1 ]
		{
			pd = new PointsDistribution(1.5, 1.5, 0.5, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		case "-G": {
			ObtuseTriangle.sna = PersistentString.toDouble(properties[2]);
			ObtuseTriangle.sja = PersistentString.toDouble(properties[3]);
			ObtuseTriangle.rotate = properties[4].charAt(0);
			ObtuseTriangle.random = properties[5].charAt(0);

			parameters.add(new Pair<String, Double>("n", ObtuseTriangle.sna));
			parameters.add(new Pair<String, Double>("j", ObtuseTriangle.sja));
			parameters.add(new Pair<String, Double>("rot", (ObtuseTriangle.rotate == 'y' ? 1.0:0)));
			parameters.add(new Pair<String, Double>("ran", (ObtuseTriangle.random == 'y' ? 1.0:0)));

			ObtuseTriangle.angleStep = (Math.PI - Utils.thirdPi) / (repetitions-1);

			if (verbose) {
				System.out.println("angleStep: " + ObtuseTriangle.angleStep);
				System.out.println("repetitions: " + repetitions);
			}

			double step = ObtuseTriangle.sja / ObtuseTriangle.sna;

			pd = new PointsDistribution(ObtuseTriangle.centerX + ObtuseTriangle.sna + step + 1,
					ObtuseTriangle.centerY + ObtuseTriangle.sja + step + 1, step, Color.BLACK, Color.WHITE);		}
			obtuse(mode, properties);
			break;

		case "-i": // Infinite plane
		{
			ObtuseTriangle.dvdN = PersistentString.toDouble(properties[2]);
			ObtuseTriangle.dvdX = PersistentString.toDouble(properties[3]);
			ObtuseTriangle.dvsN = PersistentString.toDouble(properties[4]);
			ObtuseTriangle.dvsX = PersistentString.toDouble(properties[5]);

			parameters.add(new Pair<String, Double>("nD", ObtuseTriangle.dvdN));
			parameters.add(new Pair<String, Double>("xD", ObtuseTriangle.dvdX));
			parameters.add(new Pair<String, Double>("nS", ObtuseTriangle.dvsN));
			parameters.add(new Pair<String, Double>("xS", ObtuseTriangle.dvsX));

			double scale = ObtuseTriangle.dvdX / ObtuseTriangle.dvsN;
			pd = new PointsDistribution(scale / 2, scale / 2, scale / 5, Color.BLACK, Color.WHITE);
		}
			obtuse(mode, properties);
			break;

		default:
			printUsage();
			throw new IllegalArgumentException("Invalid mode: " + mode);
		}
		print();
		System.out.println("=== Calculations ended at " + LocaleUtils.daytime());
	}

	private static void obtuse(String mode, String... properties) {
		// main(mode, repetitions, limitX, limitX);
		String ext = ".csv";
//		ObtuseTriangle.figure = mode;
		String version = "Version, " + "Mode " + mode + ", Reps " + repetitions;
		String fileName = "run" + mode + "-rep-" + repetitions;
		if (parameters.size() > 0) {
			for (Pair<String, Double> p : parameters) {
				fileName += "-" + p.getKey() + ":" + p.getValue();
			}
		}
		ObtuseTriangle.setFileNamePrefix(histograms, fileName);
		double p = ObtuseTriangle.calculateProbabilitiy(mode, repetitions); // properties[2] = tries
		version += ", Act. p=" + p + ", Obt. p=" + StatsUtils.round((1 - p), StatsUtils.probabilityAccuracy);

		String run = fileName; // This is for exception handling: system might fail before initialization
		try {
			run = ObtuseTriangle.tableStatistics() + "\n" + version;
		} catch (IOException e1) {
			System.out.println("I/O Error at " + run);
			e1.printStackTrace();
		}

		System.out.println(version);
		if (debug) {
			double tot = ghalfPi + lhalfPi;
			System.out.println("Bigger=" + ghalfPi + ". Samller=" + lhalfPi + ". Total=" + tot + ". P(obt)=="
					+ (((double) ghalfPi) / tot));

			tot = gx0 + lx0;
			System.out.println(
					"BiggerX=" + gx0 + ". SamllerX=" + lx0 + ". Total=" + tot + ". P(obt)==" + (((double) lx0) / tot));
		}
		if (verbose) {
			System.out.println(fileName);
			System.out.println(run);
		}
		if (saveRaw) {
			ObtuseTriangle.table.setVersion(version);
			System.out.println("Saving " + version);
			ObtuseTriangle.table.save(statsFolder, "raw-" + fileName + ext);
		}
		try {
			PersistentString.write(metricsFolder, fileName + ext, run);
			if (saveDistributionImage) {
				if (pd.errorCount() > 0 && pd.errorCount() < (repetitions / 100)) {
					System.out.println("Errors: " + pd.errors());
				}
				try {
					pd.save(metricsFolder, fileName);
					System.out.println("Saving points distribution JPEG image: " + fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileSystemException fse) {
			// TODO Auto-generated catch block
			fse.printStackTrace();
		}
	}

//	private static void writeString(String folder, String file, String result) {
//		Path targetFile = null;
//		targetFile = Paths.get(folder + CommonConstants.fsep + file);
//
//		if (Files.exists(targetFile)) {
//			System.out.println("Skipping existing file: " + targetFile);
//		} else {
//			try {
//				FileNames.ensureDirectory(folder);
//				Files.write(targetFile, result.getBytes());
//			} catch (IOException e) {
//				System.out.println("Error writing " + file + " into the " + folder + " folder");
//				e.printStackTrace();
//				System.exit(-3);
//			}
//			System.out.println("Saved: " + targetFile);
//		}
//	}
//
	static void printUsage() {
		String s = "Usage: java com.agoulis.probability.Caclculations -<mode> <argument1>, <argument2>, ..., <argumentN>\n";
		s += "The utility performs these calculations per modes and arguments \n";
//		s += "\t -π - calculate π via doubling the number of polygon sides. Properties\n";
		s += "\t -h - print this help\n";
		s += "\t -d - dice roll: probability of sum of 2 dice. After -d provide <tries>,\n";
		s += "\t <sum> in [2,12] range, e.g. -d 1250 7\n";
		s += "\t -g - probability distribution at r + Δr ring, where r is in [0, 0.5] and Δr [0, 0.5]\n";
		s += "range.  After -g provide Δr, e.g. -g 0.01\n";

		s += "  Broken stick:\n";
		s += "\t -p - parallel. After -p provide <tries>\n";
		s += "\t -q - sequential. After -q provide <tries>\n";
		s += "\t -w - cut ratio growth. After -w provide <increment>, e.g. -w 0.05\n";

		s += "  Obtuse triangle generation:\n";
		s += "\t -n - Normal [standard] distribution for Ro and uniform for theta in polar\n";
		s += "coordinates. After -n provide <tries>\n";
		s += "\t -c - Normal [controlled] distribution for Ro and uniform for theta in polar\n";
		s += "coordinates. After -c provide <tries> <mean> and <variance>\n";
		s += "\t -r - Uniform distribution in a (maxX-minX) by (maxY-minY) rectangle. After -r provide\n";
		s += "<tries>, <minX>, <maxX>, <minY>, <maxY>, e,g, -r 12500 -0.5 0, 0.5, 0\n";
		s += "\t -e - Uniform distribution in an ellipse with J (major) and N (minor)\n";
		s += "radii. After -e provide <tries>, <J>, <N>, <centerX>, <centerY>, to get a circle\n";
		s += "-r 125000 1, 1, 0, 0 will put random points into the circle of unit radius at origin\n";
		s += "\t -b - Betrand 2-nd method randomization. Normal (?) distribution in a circle\n";
		s += "of unit radius. After -b provide <tries>\n";
		s += "\t -f - Fractal plain coordinate for each level from d to 1 is calculated as Σ(rand[-1,1]*d),\n";
		s += "where d is a double. After -f provide <tries>, <depth>. e.g. -G 1500 8 models plane as 8 by 8\n";
		s += "embedded squares with side 2 (the coordinates range is [-1,1])\n";
		s += "\t -i - Infinite plain modeled via dividing two random numbers. After -i\n";
		s += "provide <tries>, dividend range [minD,maxD] and divisor range [minS,maxS]\n";
		s += "\t -s - S-method. After -s provide <tries> and the \n";
		s += "<value of the 'infinity'> in [2, MAX_DOUBLE] range\n";
		s += "\t -m - M-method. After -m provide <tries>\n";
		s += "\t -l - L-method. After -l provide <tries>\n";
		s += "\t -G - Generate triangles by incrementing the big angle A value in [π/3, π] range. Step of\n";
		s += "increment is (2π/3)/<tries>. The angle A coordinates are taken uniformly as a point in circle\n";
		s += "(or ellipse). The side BC in front of vertex A (long side - sideL)is selected to be 1 (or random\n";
		s += "in [0,1] range), while the next side is selected randomly in the [0, sideL] range. Using the angle A\n";
		s += "and two sides the coordinates of other vertices are calculated. You can also set random rotation of\n";
		s += "the triangle around the vertex A in range [0, 2π]. After -l provide <tries>, J (major),  N (minor),\n";
		s += "<rotate>[y,n], <random> [y,n], e.g. -G 1500 1, 1, n, y\n";
		s += "Note 1: The results go into the data folder:\n";
		s += "\tdata\n";
		s += "\t├── distribution\n";
		s += "\t│   ├── histograms\n";
		s += "\t│   │   ├── X-diagram-run-b-rep-225000.jpg\n";
		s += "\t│   │   ├──  ... \n";
		s += "\t│   │   └── X-diagram-run-b-rep-225000.jpg\n";
		s += "\t│   ├── run-b-rep-225000.csv\n";
		s += "\t│   ├──  ... \n";
		s += "\t│   └── run-s-rep-225000-inF:137.0.jpg\n";
		s += "\t├── histograms\n";
		s += "\t│   ├── MdL-diagram-run-b-rep-225000.jpg\n";
		s += "\t│   ├──  ... \n";
		s += "\t│   ├── MdL-diagram-run-e-rep-225000-n:1.0-j:1.0-cX:0.0-cY:0.0.jpg\n";
		s += "\t└── statistics\n";
		s += "\t    ├── raw-run-b-rep-225000.csv\n";
		s += "\t    ├──  ... \n";
		s += "\t    ├── raw-run-e-rep-225000-n:1.0-j:1.0-cX:0.0-cY:0.0.csv\n";
		s += "On the distribution diagrams obtuse angle vertices position marked with RED dots. For S and M\n";
		s += "methods the opposite to the S and M sides vertices marked RED, which are the small and medium angle\n";
		s += "coordinates respectively\n";
		s += "Note 2: The system writes over the existing files. To keep previous results for the same parameters\n";
		s += "you may slightly change number of tries (+1) or rename existing files\n";
		s += "  Use: \t   'v' for verbose mode (not recommended if tries are more than 100), e.g. -rv\n";
		s += "\t   't' for storing original (raw) data in csv format, e.g. -rt\n";
		s += "\t   'o' for saving a JPEG image of points distribution, , e.g. -rto\n";

		s += "*** Copyright © 2024 - Aram Hayrapetyan ***\n";

		System.out.println(s);
	}
}
