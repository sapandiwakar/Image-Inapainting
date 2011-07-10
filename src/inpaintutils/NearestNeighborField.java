/**
 * 
 */
package inpaintutils;

/**
 * @author Pulkit & Sapan
 */
public class NearestNeighborField {
	Coordinate[][] offsets;

	public Coordinate[][] getOffsets() {
		return offsets;
	}

	public void setOffsets(Coordinate[][] offsets) {
		this.offsets = offsets;
	}

	public void init(int h, int w) {
		offsets = new Coordinate[h][w];
	}

	public Coordinate getOffsetAt(int i, int j) {
		return offsets[i][j];
	}

	public void setOffsetAt(int i, int j, Coordinate c) {
		offsets[i][j] = c;
	}

}
