/**
 * 
 */
package inpaintutils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import exception.ImageReadException;

/**
 * @author Pulkit & Sapan
 */
public class NNFGenerator {

	NearestNeighborField nnf;
	final int MAX_ITERATIONS = 5;
	final static int PATCH_RADIUS = 3;
	final static double alpha = 0.5;
	static int w;

	final int TYPE_BACKWARD = 1;
	final int TYPE_FORWARD = 2;

	static int count = 0;

	public static void main(String args[]) throws IOException, ImageReadException {
		Image a = ImageIO.read(new File("D:/Study/Extra/Research/Image1.png"));
		Image b = ImageIO.read(new File("D:/Study/Extra/Research/Image2.png"));

		NNFGenerator nnfg = new NNFGenerator();

		nnfg.generateNNF(a, b);

	}

	public NearestNeighborField generateNNF(Image a, Image b) throws ImageReadException, IOException {

		w = Math.max(a.getHeight(null), a.getWidth(null));

		int[][] pixelmapA = ImageUtils.grabPixels(a);
		int[][] pixelmapB = ImageUtils.grabPixels(b);

		init(pixelmapA, pixelmapB);
		for (int i = 1; i <= MAX_ITERATIONS; ++i) {
			System.out.println("Iteration " + i);

			BufferedImage c = new BufferedImage(a.getWidth(null), a.getHeight(null), ((BufferedImage) a).getType());
			WritableRaster raster = c.getRaster();
			if (i % 2 == 0) {
				propogate(pixelmapA, pixelmapB, TYPE_BACKWARD);
			} else {
				propogate(pixelmapA, pixelmapB, TYPE_FORWARD);
			}

			propogateRandom(pixelmapA, pixelmapB);
			updateImageByPixel(pixelmapA, pixelmapB, raster, nnf);
			ImageIO.write(c, "png", new File("D:/Study/Extra/Research/OutputImage" + i + ".png"));
		}

		return null;
	}

	private void init(int[][] pixelmapA, int[][] pixelmapB) {
		nnf = new NearestNeighborField();
		nnf.init(pixelmapA.length, pixelmapA[0].length);

		double[][] D = new double[pixelmapA.length][pixelmapA[0].length];
		for (int i = 0; i < pixelmapA.length; ++i) {
			for (int j = 0; j < pixelmapA[i].length; ++j) {
				D[i][j] = Double.MAX_VALUE;
				Coordinate offset = new Coordinate();
				offset.x = (int) (Math.random() * (pixelmapB[i].length - 20)) - j;
				offset.y = (int) (Math.random() * (pixelmapB.length - 20)) - i;
				nnf.setOffsetAt(i, j, offset);
			}
		}

		for (int k = 0; k < 5; ++k) {
			NearestNeighborField tempNNF = new NearestNeighborField();
			tempNNF.init(pixelmapA.length, pixelmapA[0].length);
			for (int i = 0; i < pixelmapA.length; ++i) {
				for (int j = 0; j < pixelmapA[i].length; ++j) {
					Coordinate offset = new Coordinate();
					offset.x = (int) (Math.random() * (pixelmapB[i].length - 20)) - j;
					offset.y = (int) (Math.random() * (pixelmapB.length - 20)) - i;

					Coordinate current = new Coordinate(j, i);
					Coordinate currentB = ImageUtils.addCoordinates(current, offset);

					int[][] patchA = ImageUtils.getPatch(pixelmapA, current, PATCH_RADIUS);
					double distance = Double.MAX_VALUE;
					if (ImageUtils.isValidCoordinate(currentB, pixelmapA)) {
						int[][] patchB = ImageUtils.getPatch(pixelmapB, currentB, PATCH_RADIUS);
						distance = ImageUtils.getDistance(patchA, patchB);
					} else {
						Coordinate c = ImageUtils.toValidCoordinate(currentB, pixelmapA);
						nnf.setOffsetAt(i, i, c);
						int[][] patchB = ImageUtils.getPatch(pixelmapB, c, PATCH_RADIUS);
						distance = ImageUtils.getDistance(patchA, patchB);
					}
					if (distance < D[i][j]) {
						D[i][j] = distance;
						nnf.setOffsetAt(i, j, offset);
					}
				}
			}
		}
	}

	private boolean propogationCondition(int type, int i, int length) {
		if (type == TYPE_FORWARD) {
			return i < length - PATCH_RADIUS;
		} else {
			return i >= PATCH_RADIUS;
		}
	}

	private int propogationUpdation(int i, int type) {
		if (type == TYPE_FORWARD) {
			return ++i;
		} else {
			return --i;
		}
	}

	private void propogate(int[][] pixelmapA, int[][] pixelmapB, int type) {
		int start;
		if (type == TYPE_FORWARD) {
			start = PATCH_RADIUS;
		} else {
			start = pixelmapA.length - PATCH_RADIUS - 1;
		}

		for (int i = start; propogationCondition(type, i, pixelmapA.length); i = propogationUpdation(i, type)) { // TODO
			for (int j = start; propogationCondition(type, j, pixelmapA[i].length); j = propogationUpdation(j, type)) { // TODO
				Coordinate current = new Coordinate(j, i);
				Coordinate current1 = new Coordinate(-j, -i);
				Coordinate newCoordinate1;
				Coordinate newCoordinate2;

				if (type == TYPE_FORWARD) {
					newCoordinate1 = /* ImageUtils.addCoordinates(new Coordinate(1, 0), */ImageUtils.addCoordinates(current, nnf.getOffsetAt(i, j - 1));// );
					newCoordinate2 = /* ImageUtils.addCoordinates(new Coordinate(0, 1), */ImageUtils.addCoordinates(current, nnf.getOffsetAt(i - 1, j));// );
				} else {
					newCoordinate1 = /* ImageUtils.addCoordinates(new Coordinate(-1, 0), */ImageUtils.addCoordinates(current, nnf.getOffsetAt(i, j + 1));// );
					newCoordinate2 = /* ImageUtils.addCoordinates(new Coordinate(0, -1), */ImageUtils.addCoordinates(current, nnf.getOffsetAt(i + 1, j));// );
				}

				Coordinate currentB = ImageUtils.addCoordinates(current, nnf.getOffsetAt(i, j));

				int[][] patchA = ImageUtils.getPatch(pixelmapA, current, PATCH_RADIUS);

				double D = Double.MAX_VALUE;
				if (ImageUtils.isValidCoordinate(currentB, pixelmapA)) {
					int[][] patchB = ImageUtils.getPatch(pixelmapB, currentB, PATCH_RADIUS);
					D = ImageUtils.getDistance(patchA, patchB);
				} else {
					Coordinate c = ImageUtils.toValidCoordinate(currentB, pixelmapA);
					nnf.setOffsetAt(i, i, c);
					int[][] patchB = ImageUtils.getPatch(pixelmapB, c, PATCH_RADIUS);
					D = ImageUtils.getDistance(patchA, patchB);
				}

				double newD1 = Double.MAX_VALUE, newD2 = Double.MAX_VALUE;

				if (ImageUtils.isValidCoordinate(newCoordinate1, pixelmapA)) {
					int[][] patchB1 = ImageUtils.getPatch(pixelmapB, newCoordinate1, PATCH_RADIUS);
					newD1 = ImageUtils.getDistance(patchA, patchB1);
				}
				if (ImageUtils.isValidCoordinate(newCoordinate2, pixelmapA)) {
					int[][] patchB2 = ImageUtils.getPatch(pixelmapB, newCoordinate2, PATCH_RADIUS);
					newD2 = ImageUtils.getDistance(patchA, patchB2);
				}

				if (D == newD1 && D == newD2) {
					continue;
				}
				if (D < newD1 && D < newD2) {
					// Current nnf is correct
				} else if (newD1 < D && newD1 < newD2) {
					nnf.setOffsetAt(i, j, ImageUtils.addCoordinates(current1, newCoordinate1));
				} else {
					nnf.setOffsetAt(i, j, ImageUtils.addCoordinates(current1, newCoordinate2));
				}
			}
		}
	}

	private void propogateRandom(int[][] pixelmapA, int[][] pixelmapB) {
		for (int i = 0; i < pixelmapA.length; i++) {
			for (int j = 0; j < pixelmapA[i].length; j++) {
				int k = 0;
				Coordinate orig = nnf.getOffsetAt(i, j);
				Coordinate current = new Coordinate(j, i);
				int[][] patchA = ImageUtils.getPatch(pixelmapA, current, PATCH_RADIUS);
				double D;
				if (!ImageUtils.isValidCoordinate(ImageUtils.addCoordinates(current, orig), pixelmapA)) {
					D = Double.MAX_VALUE;
				} else {
					int[][] patchB = ImageUtils.getPatch(pixelmapB, ImageUtils.addCoordinates(current, orig), PATCH_RADIUS);
					D = ImageUtils.getDistance(patchA, patchB);
				}

				while (w * Math.pow(alpha, k) > 1) {
					double signX = Math.random() > 0.5 ? -1 : 1;
					double signY = Math.random() > 0.5 ? -1 : 1;
					double rX = signX * Math.random();
					double rY = signY * Math.random();
					int uX = (int) (nnf.getOffsetAt(i, j).x + w * Math.pow(alpha, k) * rX);
					int uY = (int) (nnf.getOffsetAt(i, j).y + w * Math.pow(alpha, k) * rY);
					Coordinate newC = ImageUtils.addCoordinates(current, new Coordinate(uX, uY));
					if (ImageUtils.isValidCoordinate(newC, pixelmapA)) {
						int[][] patchBNew = ImageUtils.getPatch(pixelmapB, newC, PATCH_RADIUS);
						double DNew = ImageUtils.getDistance(patchA, patchBNew);
						if (DNew < D) {
							D = DNew;
							orig = new Coordinate(uX, uY);
						}
					}
					++k;
				}
				nnf.setOffsetAt(i, j, orig);
			}
		}

	}

	private static void updateImageByPatch(int[][] pixelmapA, int[][] pixelmapB, WritableRaster raster, NearestNeighborField nnf) {
		for (int i = 0; i < pixelmapA.length; i += 6) {
			for (int j = 0; j < pixelmapA[i].length; j += 6) {
				Coordinate temp = nnf.getOffsetAt(i, j);
				Coordinate coordinate = ImageUtils.addCoordinates(new Coordinate(j, i), temp);
				if (coordinate.x < 0 || coordinate.y < 0 || coordinate.x >= pixelmapA[0].length || coordinate.y >= pixelmapA.length) {
					System.out.println(++count);
					continue;
				}
				int[][] patch = ImageUtils.getPatch(pixelmapB, coordinate, PATCH_RADIUS);

				for (int k = 0, m = -patch.length / 2; k < patch.length; ++k, ++m) {
					for (int l = 0, n = -patch[k].length / 2; l < patch[k].length; ++l, ++n) {
						int pixel = patch[k][l];
						int[] color = new int[3];
						color[0] = 0xff & pixel >> 16;
						color[1] = 0xff & pixel >> 8;
						color[2] = 0xff & pixel;

						if (j + n > 0 && j + n < pixelmapB[0].length && i + m > 0 && i + m < pixelmapB.length) {
							raster.setPixel(j + n, i + m, color);
						}
					}
				}

			}
		}
	}

	private static void updateImageByPixel(int[][] pixelmapA, int[][] pixelmapB, WritableRaster raster, NearestNeighborField nnf) {
		for (int i = 0; i < pixelmapA.length; i++) {
			for (int j = 0; j < pixelmapA[i].length; j++) {
				/**
				 * Update the image pixels
				 */
				Coordinate temp = nnf.getOffsetAt(i, j);
				Coordinate coordinate = ImageUtils.addCoordinates(new Coordinate(j, i), temp);
				if (coordinate.x < 0 || coordinate.y < 0 || coordinate.x >= pixelmapA[0].length || coordinate.y >= pixelmapA.length) {
					System.out.println(++count);
					continue;
				}

				// int[][] patch = ImageUtils.getPatch(pixelmapB, coordinate, PATCH_RADIUS);
				// for (int k = 0, m = -patch.length / 2; k < patch.length; ++k, ++m) {
				// System.out.println(i + m);
				// for (int l = 0, n = -patch[k].length / 2; l < patch[k].length; ++l, ++n) {
				// int pixel = patch[k][l];
				int pixel = pixelmapB[coordinate.y][coordinate.x];
				int[] color = new int[3];
				color[0] = 0xff & pixel >> 16;
				color[1] = 0xff & pixel >> 8;
				color[2] = 0xff & pixel;

				// if (j + n > 0 && j + n < pixelmapB[0].length && i + m > 0 && i + m < pixelmapB.length) {
				// raster.setPixel(j + n, i + m, color);
				raster.setPixel(j, i, color);
				// }
				// }
				// }

				//

			}
			int j = 0;
			int k = j;
		}
	}
}
