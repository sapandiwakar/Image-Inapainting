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

}
