package com.agoulis.probability;

import java.awt.Color;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCalculations {
	static String repetitions = "12000"; // p1 - tries,
	String mode = "-x";

	String p2 = "-";
	String p3 = "-";
	String p4 = "-";
	String p5 = "-";

	@BeforeClass
	public static void setUpStreams() throws Exception {
	}

	@AfterClass
	public static void close() {
	}

//	@Test
	public void testSingleDistributions() {
		System.out.println("--------------- Testing Obtuse triangle problem - testSingleDistributions via main");
		CalculationRoutines.printUsage();

		// ------------------------ Normal controlled
		mode = "-cot";
		p2 = "0"; // mean; 
		p3 = "0.003"; // SD (σ) = 0.73,  actual = 0.50

		try {
			CalculationRoutines.main(mode, repetitions, p2, p3, p4, p5);
		} catch (RuntimeException re) {
			System.out.println("Error at testDistributions");
			re.printStackTrace();
		}
	}

	@Test
	public void testObtuseTriangleBuilds() {
		System.out.println("--------------- Testing testObtuseTriangleBuilds: S, M, and l methods via main");

		// ------------------------ S method
		mode = "-sot"; //
		p2 = "137"; // infinity max: works for > 17: for low values the process loops (?), does not end
		try {
			CalculationRoutines.main(mode, repetitions, p2);
		} catch (RuntimeException re) {
			System.out.println("Error at testObtuseTriangleBuilds");
			re.printStackTrace();
		}

		// ------------------------ M method
		mode = "-mot";
		try {
			CalculationRoutines.main(mode, repetitions);
		} catch (RuntimeException re) {
			System.out.println("Error at testObtuseTriangleBuilds");
			re.printStackTrace();
		}

		// ------------------------ L method
		mode = "-lot";
		try {
			CalculationRoutines.main(mode, repetitions);
		} catch (RuntimeException re) {
			System.out.println("Error at testObtuseTriangleBuilds");
			re.printStackTrace();
		}
	}

	@Test
	public void testDistributions() {
		System.out.println("--------------- Testing Obtuse triangle problem - testDistributions via main");
		CalculationRoutines.printUsage();

		// ------------------------ Rectangle
		mode = "-rot"; //
		p2 = "-0.5"; // min of horizontal side
		p3 = "0.5"; // max of horizontal side
		p4 = "-0.5"; // min of vertical side
		p5 = "0.5"; // max of vertical side

		try {
			CalculationRoutines.main(mode, repetitions, p2, p3, p4, p5);
		} catch (RuntimeException re) {
			System.out.println("Error at testDistributions");
			re.printStackTrace();
		}

		// ------------------------ Ellipse
		mode = "-eot";
		p2 = "1"; // minor (a);
		p3 = "1"; // major (b)
		p4 = "0"; // center X
		p5 = "0"; // center Y

		try {
			CalculationRoutines.main(mode, repetitions, p2, p3, p4, p5);
		} catch (RuntimeException re) {
			System.out.println("Error at testDistributions");
			re.printStackTrace();
		}

		// ------------------------ Normal (standard)
		mode = "-not";
		try {
			CalculationRoutines.main(mode, repetitions, p2, p3, p4, p5);
		} catch (RuntimeException re) {
			System.out.println("Error at testDistributions");
			re.printStackTrace();
		}

		// ------------------------ Normal controlled
		mode = "-cot";
		p2 = "0"; // mean; 
		p3 = "0.93"; // SD (σ) = 0.73,  actual = 0.50

		try {
			CalculationRoutines.main(mode, repetitions, p2, p3, p4, p5);
		} catch (RuntimeException re) {
			System.out.println("Error at testDistributions");
			re.printStackTrace();
		}

		// ------------------------ Infinite plane
		mode = "-iot";
		p2 = "0"; // dvd min;
		p3 = "1"; // dvd max;
		p4 = "0.09"; // dvs min;
		p5 = "1.4"; // dvs max;

		try {
			CalculationRoutines.main(mode, repetitions, p2, p3, p4, p5);
		} catch (RuntimeException re) {
			System.out.println("Error at testDistributions");
			re.printStackTrace();
		}

		// ------------------------ Fractal
		mode = "-fot";
		p2 = "8"; // depth
		p3 = "2"; // multiple modulo

		try {
			CalculationRoutines.main(mode, repetitions, p2, p3);
		} catch (RuntimeException re) {
			System.out.println("Error at testDistributions");
			re.printStackTrace();
		}

		// ------------------------ Bertrand 2nd
		mode = "-bot";
		try {
			CalculationRoutines.main(mode, repetitions, p2, p3, p4, p5);
		} catch (RuntimeException re) {
			System.out.println("Error at testDistributions");
			re.printStackTrace();
		}

		// ------------------------ G method
		mode = "-Got";
		p2 = "1"; // minor (a);
		p3 = "1"; // major (b)
		p4 = "n"; // rotate? y, n
		p5 = "n"; // random long side? y, n

		try {
			CalculationRoutines.main(mode, repetitions, p2, p3, p4, p5);
		} catch (RuntimeException re) {
			System.out.println("Error at testObtuseTriangleBuilds");
			re.printStackTrace();
		}
	}

//	@Test
	public void testDistributionImage() {
		// Ellipse
		PointsDistribution pd = new PointsDistribution(3.0, 3.0, 0.5, Color.BLACK, Color.WHITE);
		int tries = 4500;

		int j = 2;
		int n = 1;

		int centerX = 1;
		int centerY = 0;

		int i = tries;
		while (--i > 0) {
			pd.pixel(Utils.randomPointInEllipse(j, n, centerX, centerY), Color.RED);
		}

		if (pd.errorCount() > 0 && pd.errorCount() < (tries / 100)) {
			System.out.println("Errors:\n" + pd.errors());
		}
		try {
			String fileName = "Ellipse-center[" + centerX + "," + centerY + "] j" + j + " n" + n;
			pd.save("target", fileName);
			System.out.println("Saving: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Rectangle
		pd = new PointsDistribution(3.0, 3.0, 0.5, Color.BLACK, Color.WHITE);
		i = tries;
		int h = 1;
		int w = 1;

		while (--i > 0) {
			pd.pixel(Utils.randomPointInRectangle(0, h, 0, w), Color.BLUE);
		}

		if (pd.errorCount() > 0 && pd.errorCount() < (tries / 100)) {
			System.out.println("Errors: " + pd.errors());
		}
		try {
			String fileName = "Rectangle-center[" + centerX + "," + centerY + "] h" + h + " w" + w;
			pd.save("target", fileName);
			System.out.println("Saving: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// L -Method
		pd = new PointsDistribution(1.5, 1.5, 0.5, Color.BLACK, Color.WHITE);
		i = tries;

		while (--i > 0) {
			pd.pixel(ObtuseTriangle.randomRejectPointLMethod(), Color.GREEN);
		}

		if (pd.errorCount() > 0 && pd.errorCount() < (tries / 100)) {
			System.out.println("Errors: " + pd.errors());
		}
		try {
			String fileName = "L-method";
			pd.save("target", fileName);
			System.out.println("Saving: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// M -Method
		pd = new PointsDistribution(3.5, 3.5, 0.5, Color.BLACK, Color.WHITE);
		i = tries;

		while (--i > 0) {
			pd.pixel(ObtuseTriangle.randomPolarPointMMethod(), Color.GREEN);
		}

		if (pd.errorCount() > 0 && pd.errorCount() < (tries / 100)) {
			System.out.println("Errors: " + pd.errors());
		}
		try {
			String fileName = "M-method";
			pd.save("target", fileName);
			System.out.println("Saving: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// S -Method
		pd = new PointsDistribution(138, 138, 20, Color.BLACK, Color.WHITE);
		i = tries;
		int infinity = 737;

		while (--i > 0) {
			pd.pixel(ObtuseTriangle.randomPolarPointSMethod(infinity), Color.GREEN);
		}

		if (pd.errorCount() > 0 && pd.errorCount() < (tries / 100)) {
			System.out.println("Errors: " + pd.errors());
		}
		try {
			String fileName = "S-method";
			pd.save("target", fileName);
			System.out.println("Saving: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Normal
		pd = new PointsDistribution(2.5, 2.5, 0.5, Color.BLACK, Color.WHITE);
		i = tries;

		while (--i > 0) {
			pd.pixel(Utils.randomPolarPointNormal(), Color.RED);
		}

		if (pd.errorCount() > 0 && pd.errorCount() < (tries / 100)) {
			System.out.println("Errors: " + pd.errors());
		}
		try {
			String fileName = "Normal";
			pd.save("target", fileName);
			System.out.println("Saving: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Normal-Bertrand-2
		pd = new PointsDistribution(1.5, 1.5, 0.3, Color.BLACK, Color.WHITE);
		i = tries;

		while (--i > 0) {
			pd.pixel(Utils.randomPolarPointBertrand(), Color.BLUE);
		}

		if (pd.errorCount() > 0 && pd.errorCount() < (tries / 100)) {
			System.out.println("Errors: " + pd.errors());
		}
		try {
			String fileName = "Bertrand-2";
			pd.save("target", fileName);
			System.out.println("Saving: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Infinite plane
		pd = new PointsDistribution(10, 10, 1, Color.BLACK, Color.WHITE);
		i = tries;

		double dvdN = 0;
		double dvdX = 2;
		double dvsN = 0;
		double dvsX = 0.3;

		while (--i > 0) {
			pd.pixel(Utils.randomPolarPointInfinite(dvdN, dvdX, dvsN, dvsX), Color.GREEN);
		}

		if (pd.errorCount() > 0 && pd.errorCount() < (tries / 100)) {
			System.out.println("Errors: " + pd.errors());
		}
		try {
			String fileName = "Infinite-plane";
			pd.save("target", fileName);
			System.out.println("Saving: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
