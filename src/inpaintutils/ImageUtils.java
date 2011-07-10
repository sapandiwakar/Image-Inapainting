/**
 * 
 */
package inpaintutils;

import java.awt.Image;
import java.awt.image.PixelGrabber;

import exception.ImageReadException;

/**
 * @author Pulkit & Sapan
 */
public class ImageUtils {
	public static int[][] grabPixels(Image image) throws ImageReadException {
		int iw = image.getWidth(null);
		int ih = image.getHeight(null);

		int[] pixels = new int[iw * ih];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, iw, ih, pixels, 0, iw);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			throw new ImageReadException();
		}

		int[][] pixelmap = new int[ih][iw];

		for (int i = 0; i < ih; i++) {
			for (int j = 0; j < iw; j++) {
				pixelmap[i][j] = pixels[i * iw + j];
			}
		}
		return pixelmap;
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
	public static int[][] getPatch(int[][] pixelmap, Coordinate p, int patchRadius) {
		int iw = pixelmap[0].length;
		int ih = pixelmap.length;

		int temp1 = Math.max(p.y - patchRadius, 0); // Starting Y coordinate
		int temp2 = Math.min(p.y + patchRadius, ih - 1); // Ending Y coordinate
		int temp3 = Math.max(p.x - patchRadius, 0); // Starting X Coordinate
		int temp4 = Math.min(p.x + patchRadius, iw - 1); // Ending X Coordinate

		int ii = temp4 - temp3 + 1;
		int jj = temp2 - temp1 + 1;
		if (ii < 0 || jj < 0) {
			return null;
		}
		int[][] N = new int[ii][jj];

		for (int i = 0; i < temp4 - temp3 + 1; i++) {
			for (int j = 0; j < temp2 - temp1 + 1; j++) {
				N[i][j] = pixelmap[temp1 + j][temp3 + i];
			}
		}
		return N;
	}

	public static Coordinate addCoordinates(Coordinate a, Coordinate b) {
		return new Coordinate(a.x + b.x, a.y + b.y);
	}

	public static double getDistance(int[][] patchA, int[][] patchB) {
		if (patchA == null || patchB == null) {
			return Double.MAX_VALUE;
		}
		double patchD = 0;
		for (int i = 0; i < patchA.length; ++i) {
			for (int j = 0; j < patchA[i].length; ++j) {
				int rA = 0xff & patchA[i][j] >> 16;
				int gA = 0xff & patchA[i][j] >> 8;
				int bA = 0xff & patchA[i][j];

				int rB = 0xff & patchB[i][j] >> 16;
				int gB = 0xff & patchB[i][j] >> 8;
				int bB = 0xff & patchB[i][j];

				double pixelD = 0;
				/**
				 * Calculate the mean square Dor for the patch.
				 */
				pixelD = rA - rB;
				patchD += pixelD * pixelD;
				pixelD = gA - gB;
				patchD += pixelD * pixelD;
				pixelD = bA - bB;
				patchD += pixelD * pixelD;
			}
		}
		return patchD;
	}

	public static boolean isValidCoordinate(Coordinate c, int[][] pixelmap) {
		if (c.x - 3 >= 0 && c.x + 3 < pixelmap[0].length && c.y - 3 >= 0 && c.y + 3 < pixelmap.length) {
			return true;
		}
		return false;
	}

	public static Coordinate toValidCoordinate(Coordinate orig, int[][] pixelmap) {
		Coordinate c = new Coordinate(orig.x, orig.y);
		if (c.x - 3 < 0) {
			c.x = 3;
		}
		if (c.x + 3 >= pixelmap[0].length) {
			c.x = pixelmap[0].length - 4;
		}
		if (c.y - 3 < 0) {
			c.y = 3;
		}
		if (c.y + 3 >= pixelmap.length) {
			c.y = pixelmap.length - 4;
		}
		return c;
	}
}
