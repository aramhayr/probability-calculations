package com.agoulis.probability;

import javax.imageio.ImageIO;

import com.agoulis.common.utils.FileNames;
import com.agoulis.common.utils.FileSystemException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PointsDistribution {
	private final double maxX;
	private final double maxY;
	private final double step;
	private final Color drawColor;
	private final Color backgroundColor;
	private final int width = 600;
	private final int height = 600;
	private final BufferedImage image;
	private final Graphics2D g2d;
	private final List<double[]> errorList;

	private Color textColor;

	static int i = 0; // To calculate the percentage of points to display

	public PointsDistribution(double maxX, double maxY, double step, Color draw, Color background) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.step = step;
		this.drawColor = draw;
		this.backgroundColor = background;
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		this.g2d = image.createGraphics();
		this.errorList = new ArrayList<>();

		textColor = drawColor;
		
		initializeImage();
	}

	private void initializeImage() {
		// Set background color
		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, width, height);

		// Set drawing color
		g2d.setColor(drawColor);

		// Draw X and Y axes
		g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0));
		g2d.drawLine(0, height / 2, width, height / 2); // X-axis
		g2d.drawLine(width / 2, 0, width / 2, height); // Y-axis

		// Mark X-axis values
		g2d.setFont(new Font("Onyx", Font.PLAIN, 10));
		for (double x = -maxX; x <= maxX; x += step) {
			int xPos = (int) ((x + maxX) / (2 * maxX) * width);
			g2d.drawString(String.format("%.1f", x), xPos, height / 2 + 15);
		}

		// Mark Y-axis values
		for (double y = -maxY; y <= maxY; y += step) {
			int yPos = (int) (height - (y + maxY) / (2 * maxY) * height);
			g2d.drawString(String.format("%.1f", y), width / 2 - 30, yPos);
		}
	}

	public void pixel(double[] coordinates, Color color) {
//		if (i++ == CalculationRoutines.skipPercent) {
			i = 0;
			if (coordinates[0] >= -maxX && coordinates[0] <= maxX && coordinates[1] >= -maxY
					&& coordinates[1] <= maxY) {
				int x = (int) ((coordinates[0] + maxX) / (2 * maxX) * width);
				int y = (int) (height - (coordinates[1] + maxY) / (2 * maxY) * height);
				g2d.setColor(color);
				g2d.fillRect(x, y, 1, 1);
			} else {
				errorList.add(coordinates);
			}
//		}
	}

	public int errorCount() {
		return errorList.size();
	}

	public String errors() {
		StringBuilder sb = new StringBuilder();
		for (double[] coord : errorList) {
			sb.append(String.format("(%.2f, %.2f)", coord[0], coord[1])).append(", ");
		}
		return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "";
	}
// ----------------------------------------
    /**
     * Draws a string on the diagram at the specified coordinates.
     *
     * @param s The string to draw.
     * @param x The x-coordinate of the start of the string.
     * @param y The y-coordinate of the start of the string.
     */
    private void drawString(String s, int x, int y) {
        g2d.setColor(textColor);
        g2d.drawString(s, x, y);
    }

    /**
     * Creates a histogram diagram of the given distribution.
     *
     * @param distribution The list of double values representing the distribution.
     * @return A BufferedImage containing the histogram diagram.
     */
    public BufferedImage histogram(List<Double> distribution) {
        // Set background
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, width);

        // Calculate statistics
        String stats = StatsUtils.statistics(distribution);
        double[] metrics = Arrays.stream(stats.split(",")).mapToDouble(Double::parseDouble).toArray();
        double mean = metrics[0];
        double median = metrics[1];
        double min = metrics[2];
        double max = metrics[3];
        double variance = metrics[4];
        double stdDev = metrics[5];
        double skewness = metrics[7];
        double kurtosis = metrics[8];

        // Draw histogram
        drawHistogram(distribution, 50, 70);

        // Display statistics
        g2d.setFont(new Font("Onyx", Font.PLAIN, 12));
        drawString(String.format("Mean: %.2f", mean), 60, 20);
        drawString(String.format("Md: %.2f", median), 60, 35);
        drawString(String.format("Skew: %.2f", skewness), 60, 50);
        drawString(String.format("Min: %.2f", min), 200, 20);
        drawString(String.format("Max: %.2f", max), 200, 35);
        drawString(String.format("Kurt: %.2f", kurtosis), 200, 50);
        drawString(String.format("SD: %.2f", stdDev), 340, 20);
        drawString(String.format("Var: %.2f", variance), 340, 35);
        textColor = Color.RED;
        drawString(String.format("P = %.5f", ObtuseTriangle.getProbability()), 340, 50);
        textColor = Color.BLACK;
        drawString(ObtuseTriangle.diagramSuffix, 80, 510);

        return image;
    }

    /**
     * Draws a histogram of the given values at the specified coordinates.
     *
     * @param values The list of double values to plot in the histogram.
     * @param x The x-coordinate of the top-left corner of the histogram frame.
     * @param y The y-coordinate of the top-left corner of the histogram frame.
     */
    private void drawHistogram(List<Double> values, int x, int y) {
        int width = 400;
        int height = 400;
        
        double min = values.stream().mapToDouble(d -> d).min().orElse(0);
        double max = values.stream().mapToDouble(d -> d).max().orElse(1);

	    int numBins = 20;
	    double binWidth = (max - min) / numBins;
	    int[] histogram = new int[numBins];
	    for (double value : values) {
	        int bin = (int) ((value - min) / binWidth);
	        if (bin == numBins) bin--; // Handle maximum value
	        histogram[bin]++;
	    }
	    
	    int maxFrequency = Arrays.stream(histogram).max().orElse(1);

        // Draw axes
        g2d.setColor(drawColor);
        g2d.drawLine(x, y + height, x + width, y + height); // X-axis
        g2d.drawLine(x, y, x, y + height);   // Y-axis

        // Draw histogram bars
        int barWidth = width / numBins;
        for (int i = 0; i < numBins; i++) {
            int barHeight = (int) (((double) histogram[i] / maxFrequency) * height);
            int barX = x + i * barWidth;
            int barY = y + height - barHeight;
            g2d.setColor(new Color(0, 0, 255, 128)); // Semi-transparent blue
            g2d.fillRect(barX, barY, barWidth, barHeight);
            g2d.setColor(drawColor);
            g2d.drawRect(barX, barY, barWidth, barHeight);
        }

        // Mark X-axis
        g2d.setFont(new Font("Onyx", Font.PLAIN, 10));
        for (int i = 0; i <= numBins; i += numBins / 5) {
            int markX = x + i * barWidth;
            double value = min + i * binWidth;
            g2d.drawLine(markX, y + height, markX, y + height + 5);
            g2d.drawString(String.format("%.2f", value), markX - 15, y + height + 20);
        }

        // Mark Y-axis
        for (int i = 0; i <= 10; i++) {
            int markY = y + height - i * (height / 10);
            int frequency = i * maxFrequency / 10;
            g2d.drawLine(x - 5, markY, x, markY);
            g2d.drawString(String.valueOf(frequency), x - 30, markY + 5);
        }
    }

    public void saveDiagram(List<Double> distribution, String folder, String fileName) throws IOException, FileSystemException {
		FileNames.ensureDirectory(folder);
		BufferedImage diagramImage = histogram(distribution);
		File outputFile = new File(folder, fileName + ".jpg");
		ImageIO.write(diagramImage, "jpg", outputFile);
	}

	public void save(String folder, String fileName) throws IOException, FileSystemException {
		FileNames.ensureDirectory(folder);
		File outputFile = new File(folder, fileName + ".jpg");
		ImageIO.write(image, "jpg", outputFile);
	}
}
