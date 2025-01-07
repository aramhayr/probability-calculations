package com.agoulis.probability;

import java.util.List;
import java.util.stream.Collectors;

import com.agoulis.common.utils.Check;

public class StatsUtils {
	final static double statsAccuracy = 1000.0;
	final static double probabilityAccuracy = 10000.0;


	final static String names = "μ,Md,Min,Max,σ²,σ,Σ,Skew,Kurt";

	// Columns (variables) metrics calculation
	public static String statistics(List<Double> distribution) {
		Check.isNotNull(Check.pre, "Statistics: distribution is null", distribution);
		Check.assertFalse(Check.pre, "Statistics: distribution is empty", distribution.isEmpty());

		// Calculate metrics
		double min = distribution.stream().mapToDouble(d -> d).min().orElse(0);
		double max = distribution.stream().mapToDouble(d -> d).max().orElse(0);
		double sum = distribution.stream().mapToDouble(d -> d).sum();
		int n = distribution.size();
		double mean = sum / n;
		double variance = distribution.stream().mapToDouble(d -> Math.pow(d - mean, 2)).sum() / n;
		double stdDev = Math.sqrt(variance);
		double median = calculateMedian(distribution);
		double skewness = calculateSkewness(distribution, mean, stdDev);
		double kurtosis = calculateKurtosis(distribution, mean, stdDev);

		// Format results
		return String.format("%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f", 
				round(mean, statsAccuracy), 
				round(median, statsAccuracy), 
				round(min, statsAccuracy), 
				round(max, statsAccuracy), 
				round(variance, statsAccuracy), 
				round(stdDev, statsAccuracy),
				round(sum, statsAccuracy), 
				round(skewness, statsAccuracy), 
				round(kurtosis, statsAccuracy));
	}

	public static double round(double value, double accuracy) {
		return Math.round(value * accuracy) / accuracy;
	}

	private static double calculateMedian(List<Double> distribution) {
		List<Double> sorted = distribution.stream().sorted().collect(Collectors.toList());
		int n = sorted.size();
		if (n % 2 == 0) {
			return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
		} else {
			return sorted.get(n / 2);
		}
	}

	private static double calculateSkewness(List<Double> distribution, double mean, double stdDev) {
		int n = distribution.size();
		double sum = distribution.stream().mapToDouble(d -> Math.pow((d - mean) / stdDev, 3)).sum();
		return n * sum / ((n - 1) * (n - 2));
	}

	private static double calculateKurtosis(List<Double> distribution, double mean, double stdDev) {
		int n = distribution.size();
		double sum = distribution.stream().mapToDouble(d -> Math.pow((d - mean) / stdDev, 4)).sum();
		return n * (n + 1) * sum / ((n - 1) * (n - 2) * (n - 3)) - 3 * Math.pow(n - 1, 2) / ((n - 2) * (n - 3));
	}
}
