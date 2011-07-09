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

}
