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

	public NearestNeighborField generateNNF(Image a, Image b) throws ImageReadException, IOException {

		int[][] pixelmapA = ImageUtils.grabPixels(a);
		int[][] pixelmapB = ImageUtils.grabPixels(b);

		init(pixelmapA, pixelmapB);
		for (int i = 1; i <= MAX_ITERATIONS; ++i) {
			BufferedImage c = new BufferedImage(a.getWidth(null), a.getHeight(null), ((BufferedImage) a).getType());
			WritableRaster raster = c.getRaster();
			propogateForward(pixelmapA, pixelmapB);
			updateImage(pixelmapA, pixelmapB, raster, nnf);
			ImageIO.write(c, "png", new File("D:/Study/Extra/Research/OutputImage" + i + ".png"));
		}

		return null;
	}

	public static void main(String args[]) throws IOException, ImageReadException {
		Image a = ImageIO.read(new File("D:/Study/Extra/Research/Image3.png"));
		Image b = ImageIO.read(new File("D:/Study/Extra/Research/Image4.png"));

		NNFGenerator nnfg = new NNFGenerator();

		nnfg.generateNNF(a, b);

	}

	static int count = 0;

	private static void updateImage(int[][] pixelmapA, int[][] pixelmapB, WritableRaster raster, NearestNeighborField nnf) {
		for (int i = 0; i < pixelmapA.length; i++) {
			for (int j = 0; j < pixelmapA[i].length; j++) {
				/**
				 * Update the image pixels
				 */
				Coordinate temp = nnf.getOffsetAt(i, j);
				Coordinate coordinate = ImageUtils.addCoordinates(new Coordinate(j, i), temp);
				if (coordinate.x >= pixelmapA[0].length || coordinate.y >= pixelmapA.length) {
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

	private void init(int[][] pixelmapA, int[][] pixelmapB) {
		nnf = new NearestNeighborField();
		nnf.init(pixelmapA.length, pixelmapA[0].length);

		int iw = pixelmapA[0].length;
		int ih = pixelmapA.length;

		for (int i = 0; i < pixelmapA.length; ++i) {
			for (int j = 0; j < pixelmapA[i].length; ++j) {
				Coordinate offset = new Coordinate();
				offset.x = (int) (Math.random() * (pixelmapB[i].length - 20)) - j;
				offset.y = (int) (Math.random() * (pixelmapB.length - 20)) - i;
				nnf.setOffsetAt(i, j, offset);
			}
		}
	}

	private void propogateForward(int[][] pixelmapA, int[][] pixelmapB) {
		for (int i = PATCH_RADIUS; i < pixelmapA.length - PATCH_RADIUS; ++i) { // TODO Fix i and condition
			for (int j = PATCH_RADIUS; j < pixelmapA[i].length - PATCH_RADIUS; ++j) { // TODO Fix j and condition
				Coordinate current = new Coordinate(j, i);
				Coordinate current1 = new Coordinate(-j, -i);
				Coordinate left = ImageUtils.addCoordinates(new Coordinate(1, 0), ImageUtils.addCoordinates(current, nnf.getOffsetAt(i, j - 1)));
				Coordinate up = ImageUtils.addCoordinates(new Coordinate(0, 1), ImageUtils.addCoordinates(current, nnf.getOffsetAt(i - 1, j)));
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

				double DLeft = Double.MAX_VALUE, DUp = Double.MAX_VALUE;

				if (ImageUtils.isValidCoordinate(left, pixelmapA)) {
					int[][] patchBLeft = ImageUtils.getPatch(pixelmapB, left, PATCH_RADIUS);
					DLeft = ImageUtils.getDistance(patchA, patchBLeft);
				}
				if (ImageUtils.isValidCoordinate(up, pixelmapA)) {
					int[][] patchBUp = ImageUtils.getPatch(pixelmapB, up, PATCH_RADIUS);
					DUp = ImageUtils.getDistance(patchA, patchBUp);
				}

				if (D == DLeft && D == DUp) {
					continue;
				}
				if (D < DLeft && D < DUp) {
					// Current nnf is correct
				} else if (DLeft < D && DLeft < DUp) {
					nnf.setOffsetAt(i, j, ImageUtils.addCoordinates(current1, left));
				} else {
					nnf.setOffsetAt(i, j, ImageUtils.addCoordinates(current1, up));
				}
			}
		}
	}
}
