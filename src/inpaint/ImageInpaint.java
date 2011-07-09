/*
 * ImageInpaint.java: File that implements image inpainting Refer to
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

package inpaint;

import imageselection.Main;
import inpaintutils.GradientCalculator;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.util.Vector;

/**
 * Class to Inpaint the given Image. Call Function init with original image, fillImage and quickPaint as arguments
 * 
 * @author Sapan & Pulkit
 */
public class ImageInpaint {
	BufferedImage origImg; // BufferedImage Object to represent updated image at every step
	Image fillImg; // Image Object to represent Image with target region marked
	BufferedImage img; // BufferedImage Object to represent original Image
	WritableRaster raster; // Raster to write to the image
	int iw, ih; // iw: Width of Image, ih: Height of Image
	int pixels[]; // Temporary array that will initially store the grabbed pixels
	int pixelmap[][]; // Matrix to store the pixels of original image
	int fillPixelmap[][]; // Matrix to store the pixels of image marked with target region
	int sourceRegion[][]; // Matrix to store Boolean values for Source Region (is updated after each iteration)
	int initialSourceRegion[][]; // Matrix to store Boolean values for Initial Source Region
	double fillRegion[][]; // Matrix to store Boolean values for Target Region
	double gradientX[][]; // Matrix to represent the perpendicular Gradient at each point in X direction
	double gradientY[][]; // Matrix to represent the perpendicular Gradient at each point in Y direction
	double confidence[][]; // Matrix that stores the Confidence values for each pixel
	double data[][]; // Matrix to store the data terms for each pixel
	GradientCalculator gc; // Object of GradientCalculator class used to calculate gradient
	double omega = 0.7; // Represents the omega used in confidence term
	double Alpha = 0.2; // Represents the multiplier for confidence term in calculating priority
	double Beta = 0.8; // Represents the multiplier for data term in calculating priority
	int maxX; // The maximum X coordinate of the target region
	int maxY; // The maximum Y coordinate of the target region
	int minX; // The minimum X coordinate of the target region
	int minY; // The minimum Y coordinate of the target region
	int continuousCol = 0; // The maximum number of continuous green pixels in one column
	int continuousRow = 0; // The maximum number of continuous green pixels in one row
	protected Main owner; // Object to the Main Class. Used in passing updates after every iteration
	public Boolean halt = false; // Used to stop inpainting.
	public Boolean completed = false; // Used to report when the inpainting is complete.
	final int diamX = 50; // Diameter (in X direction) of the Region to be searched for
	final int diamY = 30; // Diameter (in Y direction) of the Region to be searched for
	private int pixelPosX; // The X-coordinate of current pixel being processed
	private int pixelPosY; // The Y-coordinate of current pixel being processed
	int w = 3; // Patch size is 2*w + 1
	double con[][] = new double[][] { { 1, 1, 1 }, { 1, -8, 1 }, { 1, 1, 1 } }; // Laplacian filter used in founding
																				// boundary of target region

	/**
	 * Constructor for the ImageInpaint Class
	 * 
	 * @param owner
	 *            Object to the Main class representing the owner of ImageInpaint object.
	 */
	public ImageInpaint(Main owner) {
		this.owner = owner;
	}

	/**
	 * Function to inpaint the given image.
	 * 
	 * @param a_origImg
	 *            Original Image (without the target region marked)
	 * @param a_fillImg
	 *            Image with the target region marked in green color
	 * @param quickInpaint
	 *            Flag to specify whether quick painting is enabled or not
	 */
	@SuppressWarnings("unchecked")
	public void init(BufferedImage a_origImg, BufferedImage a_fillImg, Boolean quickInpaint) {
		halt = false;
		int i, j;
		try {
			origImg = a_origImg;
			img = a_origImg;
			fillImg = a_fillImg;
			raster = origImg.getRaster();

			iw = img.getWidth(null);
			ih = img.getHeight(null);

			/**
			 * Grab the pixels of Original Image.
			 */
			pixels = new int[iw * ih];
			PixelGrabber pg = new PixelGrabber(img, 0, 0, iw, ih, pixels, 0, iw);
			pg.grabPixels();
			pixelmap = new int[ih][iw];

			for (i = 0; i < ih; i++) {
				for (j = 0; j < iw; j++) {
					pixelmap[i][j] = pixels[i * iw + j];
				}
			}

			/**
			 * Grab the pixels of Fill Image.
			 */
			PixelGrabber fillPg = new PixelGrabber(fillImg, 0, 0, iw, ih, pixels, 0, iw);
			fillPg.grabPixels();
			fillPixelmap = new int[ih][iw];

			for (i = 0; i < ih; i++) {
				for (j = 0; j < iw; j++) {
					fillPixelmap[i][j] = pixels[i * iw + j];
				}
			}

		} catch (InterruptedException e1) {
			javax.swing.JOptionPane.showMessageDialog(owner, "Error " + e1, "Error!!", javax.swing.JOptionPane.ERROR_MESSAGE);
			System.out.println("Error " + e1);
		}

		/**
		 * Create instance of GradientCalculator to calculate gradients.
		 */
		gc = new GradientCalculator();
		gc.calculateGradientFromImage(pixelmap, ih, iw); // Calculate Perpendicular Gradient for original image.
		gradientX = gc.gradientX;
		gradientY = gc.gradientY;

		initialize_confidence_term(); // Initialize the confidence term
		initialize_data_term(); // Initialize the data term

		fillRegion = new double[ih][iw]; // Allocate Space for fillRegion
		sourceRegion = new int[ih][iw]; // Allocate Space for SourceRegion
		initialSourceRegion = new int[ih][iw]; // Allocate Space for initialSourceRegion

		/**
		 * Initialize minX, minY, maxX and maxY to find these coordinates
		 */
		minX = iw;
		minY = ih;
		maxX = maxY = 0;

		int pixel, r, g, b, countrow, countcol;
		continuousRow = continuousCol = 0;
		for (i = 0; i < ih; i++) {
			countrow = 0;
			for (j = 0; j < iw; j++) {
				pixel = fillPixelmap[i][j];
				r = 0xff & pixel >> 16; // Obtain the red Component
				g = 0xff & pixel >> 8; // Obtain the green Component
				b = 0xff & pixel; // Obtain the blue Component
				/**
				 * If the color is green, mark it as fillRegion Otherwise mark it as SourceRegion.
				 */
				if (r == 0 && g == 255 && b == 0) {
					countrow++; // Increase the number of continuous green pixels
					fillRegion[i][j] = 1;
					sourceRegion[i][j] = 0;
					initialSourceRegion[i][j] = 0;
					if (j < minX) {
						minX = j;
					}
					if (i < minY) {
						minY = i;
					}
					if (j > maxX) {
						maxX = j;
					}
					if (i > maxY) {
						maxY = i;
					}
				} else {
					if (countrow > continuousRow) {
						continuousRow = countrow;
					}
					countrow = 0;
					fillRegion[i][j] = 0;
					sourceRegion[i][j] = 1;
					initialSourceRegion[i][j] = 1;
				}
			}
		}

		for (i = 0; i < iw; ++i) {
			countcol = 0;
			for (j = 0; j < ih; ++j) {
				pixel = fillPixelmap[j][i];
				r = 0xff & pixel >> 16; // Obtain the red Component
				g = 0xff & pixel >> 8; // Obtain the green Component
				b = 0xff & pixel; // Obtain the blue Component
				/**
				 * If pixel is green, increase the countcol by 1.
				 */
				if (r == 0 && g == 255 && b == 0) {
					countcol++; // Increase the number of continuous green pixels
				} else {
					if (countcol > continuousCol) {
						continuousCol = countcol;
					}
					countcol = 0;
				}
			}
		}

		// System.out.println("Col = " + continuousCol);
		// System.out.println("Col = " + continuousRow);

		Boolean flag = true; // Flag to represent the completion of the inpainting process

		double[][] temp = new double[ih][iw];
		double[][] sourceGradX = new double[ih][iw];
		double[][] sourceGradY = new double[ih][iw];

		Vector dR = new Vector(); // Vector to store pixels of the boundary
		Vector Nx = new Vector(); // Vector to store corresponding gradient in X direction
		Vector Ny = new Vector(); // Vector to store corresponding gradient in Y direction

		double count; // Temporary variable used in calculating confidence terms for patches
		double Rcp; // Temporary variable used in calculating confidence terms for patches
		double tempPriority; // Temporary variable used in calculating maximum priority

		while (flag) {

			/**
			 * Filter the fillRegion with Laplacian Filter. Done to find the pixels on the boundary.
			 */
			temp = conv2(fillRegion, con);

			/**
			 * Find the gradient of Source Region.
			 */
			gc.calculateGradient(sourceRegion, ih, iw);
			sourceGradX = gc.gradientX;
			sourceGradY = gc.gradientY;

			dR.clear();
			Nx.clear();
			Ny.clear();

			/**
			 * Add the coordinates of boundary pixels to dR and their gradients to Nx and Ny.
			 */
			for (i = 0; i < temp[0].length; i++) {
				for (j = 0; j < temp.length; j++) {
					if (temp[j][i] > 0) {
						dR.add(i * temp.length + j);
						Nx.add(sourceGradX[j][i]);
						Ny.add(sourceGradY[j][i]);
					}
				}
			}

			double[][] N = normr(Nx, Ny); // Normalize Nx and Ny and store in N

			Vector q = new Vector();
			count = 0.0;

			/**
			 * Now calculate Confidence Terms for all the pixels in the boundary of target region.
			 */
			for (i = 0; i < dR.size(); i++) {
				int[][] Hp = getpatch(pixelmap, (Integer) dR.get(i));
				for (j = 0; j < Hp.length; j++) {
					for (int k = 0; k < Hp[0].length; k++) {
						int col = Hp[j][k] / ih;
						int row = Hp[j][k] % ih;
						if (fillRegion[row][col] == 0) {
							count += confidence[row][col];
							q.add(Hp[j][k]);
						}
					}
				}

				int col = (Integer) dR.get(i) / ih;
				int row = (Integer) dR.get(i) % ih;
				confidence[row][col] = count / (Hp.length * Hp[0].length);
				count = 0;
			}

			double maxPriority = 0.0;
			int maxPriorityIndex = -1;
			/**
			 * After calculating confidence terms, now calculate data term and then find the patch with maximum
			 * priority. This is done using the additive priority term.
			 */
			for (i = 0; i < dR.size(); i++) {
				int col = (Integer) dR.get(i) / ih;
				int row = (Integer) dR.get(i) % ih;

				/**
				 * Calculate Data term. +0.001 to prevent it from getting value Zero.
				 */
				data[row][col] = Math.abs(gradientX[row][col] * N[i][0] + gradientY[row][col] * N[i][1]) + 0.001;

				/**
				 * Elevated Confidence term. This prevents it from reaching Zero to soon.
				 */
				Rcp = (1 - omega) * confidence[row][col] + omega;

				tempPriority = Alpha * Rcp + Beta * data[row][col]; // Calculate Priority using additive function

				/**
				 * find the patch with maximum priority
				 */
				if (tempPriority >= maxPriority) {
					maxPriority = tempPriority;
					maxPriorityIndex = i;
				}
			}

			if (maxPriorityIndex == -1) {
				break; // If no patch is found, then inpainting is complete.
			}

			/**
			 * Obtain the patch with maximum priority.
			 * 
			 * @Hp: Coordinates of patch with maximum priority
			 * @toFill: Represents which pixels (from the patch) are to be filled
			 * @toFillTrans: Transpose of ToFill matrix
			 */
			int[][] Hp = getpatch(pixelmap, (Integer) dR.get(maxPriorityIndex));
			double[][] toFill = new double[Hp.length][Hp[0].length];
			double[][] toFillTrans = new double[Hp[0].length][Hp.length];

			/**
			 * Calculate ToFill and ToFillTrans
			 */
			for (i = 0; i < Hp.length; i++) {
				for (j = 0; j < Hp[0].length; j++) {
					int col = Hp[i][j] / ih;
					int row = Hp[i][j] % ih;
					toFill[i][j] = fillRegion[row][col];
					toFillTrans[j][i] = fillRegion[row][col];
				}
			}

			pixelPosX = (Integer) dR.get(maxPriorityIndex) / ih;
			pixelPosY = (Integer) dR.get(maxPriorityIndex) % ih;
			/**
			 * Find the Best Exemplar. if quickInpaint is set, then only a small region of complete image is scanned. We
			 * now get the starting and ending X and Y coordinates of the best Patch.
			 */
			int[] best = bestExemplar(Hp, toFillTrans, initialSourceRegion, quickInpaint);

			int nRows = best[3] - best[2] + 1;
			int nCols = best[1] - best[0] + 1;

			/**
			 * Find the patch represented by best.
			 */
			int[][] X = new int[nRows][nCols];
			int[][] Y = new int[nRows][nCols];
			int[][] Hq = new int[nRows][nCols];
			for (i = 0; i < nRows; i++) {
				for (j = 0; j < nCols; j++) {
					X[i][j] = best[0] + j;
					Y[i][j] = best[2] + i;
					Hq[i][j] = X[i][j] + Y[i][j] * ih;
				}
			}

			int p = (Integer) dR.get(maxPriorityIndex);

			/**
			 * Update the image, data values and confidence values.
			 */
			for (i = 0; i < toFill.length; i++) {
				for (j = 0; j < toFill[0].length; j++) {
					if (toFill[i][j] != 0) {
						toFill[i][j] = 1;
						int col = Hp[i][j] / ih;
						int row = Hp[i][j] % ih;

						int col1 = Hq[i][j] / ih;
						int row1 = Hq[i][j] % ih;

						fillRegion[row][col] = 0; // Since the region is reconstructed, remove from fillRegion
						sourceRegion[row][col] = 1; // Since the region is reconstructed, add to SourceRegion

						/** Propagate Confidence and Isophote Values */
						confidence[row][col] = confidence[p % ih][p / ih];
						gradientX[row][col] = gradientX[row1][col1];
						gradientY[row][col] = gradientY[row1][col1];
						pixelmap[row][col] = pixelmap[row1][col1];

						/**
						 * Update the image pixels
						 */
						int[] color = new int[3];
						color[0] = 0xff & pixelmap[row1][col1] >> 16;
						color[1] = 0xff & pixelmap[row1][col1] >> 8;
						color[2] = 0xff & pixelmap[row1][col1];
						raster.setPixel(col, row, color);
					}
				}
			}

			if (halt) {
				break;
			}
			owner.updateStats(origImg); // Inform the owner about the updated image.
			Thread.yield();

			/**
			 * Find whether any other pixels are remaining to be inpainted.
			 */
			flag = false;
			for (i = 0; i < fillRegion.length; ++i) {
				for (j = 0; j < fillRegion[0].length; ++j) {
					if (fillRegion[i][j] == 1) {
						flag = true;
						break;
					}
				}
				if (flag) {
					break; // If none of the pixels are in target region, break
				}
			}

			if (halt) {
				break; // Stop Inpainting if owner halts the process.
			}
		}
		/**
		 * No need of gradient calculator now. Mark it as null to drop a hint to garbageCollector that it can now be
		 * removed.
		 */
		gc = null;
		if (!halt) {
			completed = true;
		} else {
			completed = false;
		}
		owner.updateStats(origImg); // Inform the owner about the updates.
		Thread.yield();
	}

	/**
	 * Initializes the confidence term to zero for pixels in target region and 1 for pixels in source region.
	 */
	void initialize_confidence_term() {
		confidence = new double[ih][iw];
		for (int i = 0; i < ih; i++) {
			for (int j = 0; j < iw; j++) {
				int p = pixelmap[i][j];
				int r = 0xff & p >> 16;
				int g = 0xff & p >> 8;
				int b = 0xff & p;
				if (r == 0 && g == 255 && b == 0) {
					confidence[i][j] = 0;
				} else {
					confidence[i][j] = 1;
				}
			}
		}
	}

	/**
	 * Initialize the data term.
	 */
	void initialize_data_term() {
		data = new double[ih][iw];
		for (int i = 0; i < ih; i++) {
			for (int j = 0; j < iw; j++) {
				data[i][j] = -0.1;
			}
		}
	}

	/**
	 * Function to convolve the given matrix with another matrix
	 * 
	 * @param a
	 *            Matrix to be convolved
	 * @param b
	 *            Matrix to be convolved with
	 * @return The result after convolution.
	 */
	double[][] conv2(double a[][], double b[][]) {
		int ra = a.length;
		int ca = a[0].length;
		int rb = b.length;
		int cb = b[0].length;

		// do full convolution
		double c[][] = new double[ra + rb - 1][ca + cb - 1];
		for (int i = 0; i < rb; i++) {
			for (int j = 0; j < cb; j++) {
				int r1 = i;
				int r2 = r1 + ra - 1;
				int c1 = j;
				int c2 = c1 + ca - 1;
				for (int k = r1; k < r2; k++) {
					for (int l = c1; l < c2; l++) {
						c[k][l] = c[k][l] + b[i][j] * a[k - r1 + 1][l - c1 + 1];
					}
				}
			}
		}

		double out[][] = new double[ra][ca];
		// extract region of size(a) from c
		int r1 = rb / 2;
		int r2 = r1 + ra - 1;
		int c1 = cb / 2;
		int c2 = c1 + ca - 1;
		for (int i = r1; i < r2; i++) {
			for (int j = c1; j < c2; j++) {
				out[i - r1 + 1][j - c1 + 1] = c[i][j];
			}
		}
		return out;
	}

	/**
	 * Function to normalize the given vectors and return them concatenated in one single matrix
	 * 
	 * @param X
	 *            Vector 1
	 * @param Y
	 *            Vector 2
	 * @return The result after normalizing and then concatenating the two vectors.
	 */
	@SuppressWarnings("unchecked")
	double[][] normr(Vector X, Vector Y) {
		double normalized[][] = new double[X.size()][2];
		for (int i = 0; i < X.size(); i++) {
			double temp1 = (Double) X.get(i);
			double temp2 = (Double) Y.get(i);

			temp1 *= temp1;
			temp2 *= temp2;

			double temp3;

			if (temp1 + temp2 == 0) {
				temp3 = 0;
			} else {
				temp3 = 1 / (temp1 + temp2);
				temp3 = Math.sqrt(temp3);
			}

			normalized[i][0] = temp3 * (Double) X.get(i);
			normalized[i][1] = temp3 * (Double) Y.get(i);
		}

		return normalized;
	}

	/**
	 * Function that returns the patch around the given pixel.
	 * 
	 * @param pixelmap
	 *            Matrix representing the pixelmap of the image.
	 * @param p
	 *            Position of the pixel around which patch is to be calculated
	 * @return a matrix that contains the coordinates of the pixels that form the patch.
	 */
	@SuppressWarnings("unchecked")
	int[][] getpatch(int[][] pixelmap, int p) {

		int y;
		int x;

		p = p - 1;
		y = p / ih;// + 1;
		p = p % ih;
		x = p + 1;

		int temp1 = Math.max(x - w, 0); // Starting X coordinate
		int temp2 = Math.min(x + w, ih - 1); // Ending X coordinate
		int temp3 = Math.max(y - w, 0); // Starting Y Coordinate
		int temp4 = Math.min(y + w, iw - 1); // Ending Y Coordinate

		// int[][] X = new int[temp4-temp3+1][temp2-temp1+1];
		// int[][] Y = new int[temp4-temp3+1][temp2-temp1+1];
		int[][] N = new int[temp4 - temp3 + 1][temp2 - temp1 + 1];

		for (int i = 0; i < temp4 - temp3 + 1; i++) {
			for (int j = 0; j < temp2 - temp1 + 1; j++) {
				// X[i][j] = temp1 + j;
				// Y[i][j] = temp3 + i;
				N[i][j] = temp1 + j + (temp3 + i) * ih;
			}
		}
		return N;
	}

	/**
	 * Function that finds the best exemplar from the image that matches the patch given to the fucntion. By default, it
	 * finds the complete image to look for the best exemplar. But if quickInpaint is set, it only looks for the patch
	 * inside a region defined by minX, maxX, minY, maxY, diamX and diamY
	 * 
	 * @param Hp
	 *            Patch for which the best exemplar is to be found
	 * @param toFill
	 *            Pixels that are to be filled from the patch.
	 * @param sourceRegion
	 *            the source region from where to look for the best exemplar
	 * @param quickInpaint
	 *            Flag that represents whether or not to perform quick Inpainting.
	 * @return Returns an integer array with four elements representing starting x coordinate, ending x coordinate,
	 *         starting y coordinate and ending y coordinate respectively.
	 */
	int[] bestExemplar(int[][] Hp, double[][] toFill, int[][] sourceRegion, Boolean quickInpaint) {
		int[][] Ip = new int[toFill.length][toFill[0].length];
		for (int i = 0; i < toFill[0].length; i++) {
			for (int j = 0; j < toFill.length; j++) {
				int col = Hp[i][j] / ih;
				int row = Hp[i][j] % ih;
				Ip[j][i] = pixelmap[row][col]; // transpose
			}
		}

		int[][] rIp = new int[Ip.length][Ip[0].length];
		int[][] gIp = new int[Ip.length][Ip[0].length];
		int[][] bIp = new int[Ip.length][Ip[0].length];

		for (int i = 0; i < Ip.length; ++i) {
			for (int j = 0; j < Ip[0].length; ++j) {
				/**
				 * Extract the RGB components of the image.
				 */
				rIp[i][j] = 0xff & Ip[i][j] >> 16;
				gIp[i][j] = 0xff & Ip[i][j] >> 8;
				bIp[i][j] = 0xff & Ip[i][j];
			}
		}

		int mm, nn;
		int startX;
		int startY;
		int endX;
		int endY;
		int m = Ip.length;
		int n = Ip[0].length;
		if (quickInpaint) {

			startX = Math.max(0, pixelPosX - n / 2 - continuousRow - diamX / 2); // Set the start X coordinate above the
																					// pixel's X-coordinate by some
																					// amount
			startY = Math.max(0, pixelPosY - m / 2 - continuousCol - diamY / 2); // Set the start Y coordinate above the
																					// pixel's Y-coordinate by some
																					// amount
			endX = Math.min(pixelmap[0].length - 1, pixelPosX + n / 2 + continuousRow + diamX / 2);
			endY = Math.min(pixelmap.length - 1, pixelPosY + m / 2 + continuousCol + diamY / 2);
			mm = endY - startY + 1;
			nn = endX - startX + 1;
		} else {
			mm = pixelmap.length;
			nn = pixelmap[0].length;
			startX = 0;
			startY = 0;
			endX = endY = 0;
		}
		int i, j, ii, jj, ii2, jj2, M, N, I, J;
		double patchErr = 0.0, err = 0.0, bestErr = 99999999999999999999.0, bestPatchErr1 = 99999999999999999999.0;
		int[] best = { 0, 0, 0, 0 };
		Boolean skipPatchFlag = false;

		/** for each patch */
		N = startX + nn - n + 1;
		M = startY + mm - m + 1;
		for (j = startX; j < N; j++) {
			J = j + n - 1;
			for (i = startY; i < M; i++) {
				I = i + m - 1;

				skipPatchFlag = false;

				double meanR = 0.0;
				double meanG = 0.0;
				double meanB = 0.0;

				/**
				 * Calculate patch error
				 */
				for (jj = j, jj2 = 0; jj <= J; jj++, jj2++) {
					for (ii = i, ii2 = 0; ii <= I; ii++, ii2++) {

						/**
						 * If any pixels does not belong to the source region, then skip the patch.
						 */
						if (sourceRegion[ii][jj] != 1) {
							skipPatchFlag = true;
							break;
						}
						if (toFill[ii2][jj2] == 0) {
							int rImage = 0xff & pixelmap[ii][jj] >> 16;
							int gImage = 0xff & pixelmap[ii][jj] >> 8;
							int bImage = 0xff & pixelmap[ii][jj];

							/**
							 * Calculate the mean square error for the patch.
							 */
							err = rImage - rIp[ii2][jj2];
							patchErr += err * err;
							err = gImage - gIp[ii2][jj2];
							patchErr += err * err;
							err = bImage - bIp[ii2][jj2];
							patchErr += err * err;
							/**
							 * Calculate the mean of the color values. Used when we get two patches with same best
							 * error.
							 */
							meanR += rImage;
							meanG += gImage;
							meanB += bImage;
						}
					}
					if (skipPatchFlag) {
						break;
					}
				}

				/**
				 * Update
				 */
				if (!skipPatchFlag && patchErr < bestErr) {
					bestErr = patchErr;
					best[0] = i;
					best[1] = I;
					best[2] = j;
					best[3] = J;

					/**
					 * Calculate the variance of the patch.
					 */
					double patchErr1 = 0.0;
					for (jj = j, jj2 = 0; jj <= J; jj++, jj2++) {
						for (ii = i, ii2 = 0; ii <= I; ii++, ii2++) {
							if (toFill[ii2][jj2] == 1) {
								int rImage = 0xff & pixelmap[ii][jj] >> 16;
								int gImage = 0xff & pixelmap[ii][jj] >> 8;
								int bImage = 0xff & pixelmap[ii][jj];

								err = rImage - meanR;
								patchErr1 += err * err;
								err = gImage - meanG;
								patchErr1 += err * err;
								err = bImage - meanB;
								patchErr1 += err * err;
							}
						}
					}
					bestPatchErr1 = patchErr1; // Update the variance of the best patch found so far.
				} else if (!skipPatchFlag && patchErr == bestErr) {
					/**
					 * If the current patch has same error as the previous best patch we find the variance of this
					 * patch. The patch with minimum variance is now selected as the best patch.
					 */
					double patchErr1 = 0.0;
					for (jj = j, jj2 = 0; jj <= J; jj++, jj2++) {
						for (ii = i, ii2 = 0; ii <= I; ii++, ii2++) {
							if (toFill[ii2][jj2] == 1) {
								int rImage = 0xff & pixelmap[ii][jj] >> 16;
								int gImage = 0xff & pixelmap[ii][jj] >> 8;
								int bImage = 0xff & pixelmap[ii][jj];

								err = rImage - meanR;
								patchErr1 += err * err;
								err = gImage - meanG;
								patchErr1 += err * err;
								err = bImage - meanB;
								patchErr1 += err * err;
							}
						}
					}
					/**
					 * Select the new patch if it has lower variance. Otherwise discard.
					 */
					if (bestPatchErr1 > patchErr1) {
						best[0] = i;
						best[1] = I;
						best[2] = j;
						best[3] = J;
						bestPatchErr1 = patchErr1;
					}
				}

				patchErr = 0.0;
			}
		}

		if (best[0] == 0 && best[1] == 0 && best[2] == 0 && best[3] == 0) {
			return bestExemplar(Hp, toFill, sourceRegion, false);
		}
		return best;
	}
}
