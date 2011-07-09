/**
 * 
 */
package inpaintutils;

import java.awt.Image;

import exception.ImageReadException;

/**
 * @author Pulkit & Sapan
 */
public class NNFGenerator {

	NearestNeighborField nnf;

	public NearestNeighborField generateNNF(Image a, Image b) throws ImageReadException {

		int[][] pixelmapA = ImageUtils.grabPixels(a);
		int[][] pixelmapB = ImageUtils.grabPixels(b);

		init(pixelmapA, pixelmapB);

		return null;
	}

	private void init(int[][] pixelmapA, int[][] pixelmapB) {
		nnf.init(pixelmapA.length, pixelmapA[0].length);

		for (int i = 0; i < pixelmapA.length; ++i) {
			for (int j = 0; j < pixelmapA[i].length; ++j) {
				Coordinate offset = new Coordinate();
				offset.x = (int) (Math.random() * pixelmapB[i].length) - j;
				offset.y = (int) (Math.random() * pixelmapB.length) - i;
				nnf.getOffsets()[i][j] = offset;
			}
		}
	}
}
