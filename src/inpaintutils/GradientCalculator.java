/*
 * GradientCalculator.java: Fucntions to calculate gradient of an image. Refer to
 * "Fast and Enhanced Algorithm for Examplar Based Image Inpainting" Copyright (C) 2010-2011 Sapan Diwakar and Pulkit
 * Goyal DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. Please
 * contact Sapan Diwakar or Pulkit Goyal diwakar.sapan@gmail.com or pulkit110@gmail.com or visit
 * <http://sapandiwakar.wordpress.com> or <http://pulkitgoyal.wordpress.com> if you need any additional information or
 * have any questions.
 */

package inpaintutils;

import java.util.Vector;

/**
 * @author Pulkit & Sapan
 */
public class GradientCalculator {

	public double[][] gradientX; // Variable to communicate the gradient in X direction with calling class
	public double[][] gradientY; // Variable to communicate the gradient in X direction with calling class
	int r, g, b; // temporary variables to store the r, g and b values of the pixels.

	/**
	 * Function that calculates the gradient from the image given in argument using the central difference method.
	 * 
	 * @param pixelmap
	 *            Pixelmap of the image
	 * @param ih
	 *            Height of the image
	 * @param iw
	 *            Width of the image
	 */
	public void calculateGradientFromImage(int pixelmap[][], int ih, int iw) {
		gradientX = new double[ih][iw];
		gradientY = new double[ih][iw];

		int[] row = new int[ih];
		int[] column = new int[iw];

		for (int i = 0; i < ih; ++i) {
			row[i] = i + 1;
		}

		for (int i = 0; i < iw; ++i) {
			column[i] = i + 1;
		}

		double t1, t2, t3;
		if (ih > 1) {
			/**
			 * Calculate gradient for boundary pixels.
			 */
			for (int j = 0; j < iw; ++j) {
				extractRGB(pixelmap, 1, j);
				int r2 = r;
				int g2 = g;
				int b2 = b;

				extractRGB(pixelmap, 0, j);
				int r1 = r;
				int g1 = g;
				int b1 = b;

				t1 = (double) (r2 - r1) / (row[1] - row[0]);
				t2 = (double) (g2 - g1) / (row[1] - row[0]);
				t3 = (double) (b2 - b1) / (row[1] - row[0]);
				gradientX[0][j] = -(t1 + t2 + t3) / (3.0 * 255.0);

				extractRGB(pixelmap, ih - 1, j);
				int rn = r;
				int gn = g;
				int bn = b;

				extractRGB(pixelmap, ih - 2, j);
				int rn1 = r;
				int gn1 = g;
				int bn1 = b;

				t1 = (double) (rn - rn1) / (row[ih - 1] - row[ih - 2]);
				t2 = (double) (gn - gn1) / (row[ih - 1] - row[ih - 2]);
				t3 = (double) (bn - bn1) / (row[ih - 1] - row[ih - 2]);
				gradientX[ih - 1][j] = -(t1 + t2 + t3) / (3.0 * 255.0);
			}
		}

		if (ih > 2) {
			/**
			 * Calculate the gradient for central pixels.
			 */
			for (int i = 1; i < ih - 1; ++i) {
				for (int j = 0; j < iw; ++j) {
					extractRGB(pixelmap, i + 1, j);
					int r2 = r;
					int g2 = g;
					int b2 = b;

					extractRGB(pixelmap, i - 1, j);
					int r1 = r;
					int g1 = g;
					int b1 = b;

					t1 = (double) (r2 - r1) / (row[i + 1] - row[i - 1]);
					t2 = (double) (g2 - g1) / (row[i + 1] - row[i - 1]);
					t3 = (double) (b2 - b1) / (row[i + 1] - row[i - 1]);
					gradientX[i][j] = -(t1 + t2 + t3) / (3.0 * 255.0);
				}
			}
		}

		if (iw > 1) {
			/**
			 * Calculate gradient for boundary pixels.
			 */
			for (int j = 0; j < ih; ++j) {
				extractRGB(pixelmap, j, 1);
				int r2 = r;
				int g2 = g;
				int b2 = b;

				extractRGB(pixelmap, j, 0);
				int r1 = r;
				int g1 = g;
				int b1 = b;

				t1 = (double) (r2 - r1) / (column[1] - column[0]);
				t2 = (double) (g2 - g1) / (column[1] - column[0]);
				t3 = (double) (b2 - b1) / (column[1] - column[0]);
				gradientY[j][0] = (t1 + t2 + t3) / (3.0 * 255.0);

				extractRGB(pixelmap, j, iw - 1);
				int rn = r;
				int gn = g;
				int bn = b;

				extractRGB(pixelmap, j, iw - 2);
				int rn1 = r;
				int gn1 = g;
				int bn1 = b;

				t1 = (double) (rn - rn1) / (column[iw - 1] - column[iw - 2]);
				t2 = (double) (gn - gn1) / (column[iw - 1] - column[iw - 2]);
				t3 = (double) (bn - bn1) / (column[iw - 1] - column[iw - 2]);
				gradientY[j][iw - 1] = (t1 + t2 + t3) / (3.0 * 255.0);
			}
		}

		if (iw > 2) {
			/**
			 * Calculate gradient for central pixels.
			 */
			for (int i = 0; i < ih; ++i) {
				for (int j = 1; j < iw - 1; ++j) {

					extractRGB(pixelmap, i, j + 1);
					int r2 = r;
					int g2 = g;
					int b2 = b;

					extractRGB(pixelmap, i, j - 1);
					int r1 = r;
					int g1 = g;
					int b1 = b;

					t1 = (double) (r2 - r1) / (column[j + 1] - column[j - 1]);// g(2:n-1,:) =
																				// (f(3:n,:)-f(1:n-2,:))./h(:,ones(p,1));
					t2 = (double) (g2 - g1) / (column[j + 1] - column[j - 1]);
					t3 = (double) (b2 - b1) / (column[j + 1] - column[j - 1]);
					gradientY[i][j] = (t1 + t2 + t3) / (3.0 * 255.0);
				}
			}
		}
	}

	/**
	 * Function to calculate gradient for other matrices
	 * 
	 * @param a
	 *            The matrix for which gradient is to be calculated
	 * @param ih
	 *            Height of the matrix
	 * @param iw
	 *            Width of the matrix
	 */
	public void calculateGradient(int a[][], int ih, int iw) {
		gradientX = new double[ih][iw];
		gradientY = new double[ih][iw];
		Vector h = new Vector();
		int[] row = new int[ih];
		int[] column = new int[iw];

		for (int i = 0; i < ih; ++i) {
			row[i] = i + 1;
		}

		for (int i = 0; i < iw; ++i) {
			column[i] = i + 1;
		}

		if (ih > 1) {
			for (int j = 0; j < iw; ++j) {
				gradientY[0][j] = (double) (a[1][j] - a[0][j]) / (row[1] - row[0]);
				gradientY[ih - 1][j] = (double) (a[ih - 1][j] - a[ih - 2][j]) / (row[ih - 1] - row[ih - 2]);
			}
		}

		if (ih > 2) {
			for (int i = 1; i < ih - 1; ++i) {
				for (int j = 0; j < iw; ++j) {
					gradientY[i][j] = (double) (a[i + 1][j] - a[i - 1][j]) / (row[i + 1] - row[i - 1]);
				}
			}
		}

		if (iw > 1) {
			for (int j = 0; j < ih; ++j) {
				gradientX[j][0] = (double) (a[j][1] - a[j][0]) / (column[1] - column[0]);
				gradientX[j][iw - 1] = (double) (a[j][iw - 1] - a[j][iw - 2]) / (column[iw - 1] - column[iw - 2]);
			}
		}

		if (iw > 2) {
			for (int i = 0; i < ih; ++i) {
				for (int j = 1; j < iw - 1; ++j) {
					gradientX[i][j] = (double) (a[i][j + 1] - a[i][j - 1]) / (column[j + 1] - column[j - 1]);
				}
			}
		}
	}

	/**
	 * Function to calculate RGB values of the pixel
	 * 
	 * @param img
	 * @param i
	 * @param j
	 */
	void extractRGB(int img[][], int i, int j) {
		r = 0xff & img[i][j] >> 16;
		g = 0xff & img[i][j] >> 8;
		b = 0xff & img[i][j];
	}
}
