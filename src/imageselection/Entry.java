/*
 * Entry.java: Contains functions for selecting the target region. Refer to
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

package imageselection;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * Class to enable selection of region to be inpainted
 * 
 * @author Pulkit & Sapan
 */
public class Entry extends JPanel {

	protected Image entryImage; // Image to be displayed
	protected Graphics entryGraphics; // graphics object to the entry image
	protected int lastX = -1; // X co-ordinate of last pressed co-ordinate
	protected int lastY = -1; // Y co-ordinate of last pressed co-ordinate
	protected int polySides = 0; // number of sides of the polygon to be inpainted
	private Vector PolygonCoordinatesX; // vector to store X co-ordinate of all vertices of a polygon
	private Vector PolygonCoordinatesY; // vector to store Y co-ordinate of all vertices of a polygon
	private Image img; // instance of Image class
	public Stack SavedImages; // stack used to implement undo option
	public Stack RedoImages; // stack used to implement redo option
	private int firstX = -1; // variable to store first X co-ordinate of a polygon
	private int firstY = -1; // variable to store first Y co-ordinate of a polygon
	private int currX = -1; // variable to store current X co-ordinate of a polygon
	private int currY = -1; // variable to store current Y co-ordinate of a polygon
	public Boolean isDisabled = false; // flag to determine status of the inpainting module
	public int maxX = -1; // stores maximum of all X co-ordinates of all polygon
	public int maxY = -1; // stores maximum of all Y co-ordinates of all polygon
	public int minX = -1; // stores minimum of all X co-ordinates of all polygon
	public int minY = -1; // stores minimum of all Y co-ordinates of all polygon

	Boolean pressed; // Defines whether the polygon is started or not

	/**
	 * constructor of Entry class
	 * 
	 * @param img
	 *            default image to display
	 */
	Entry(Image img) {
		entryImage = img;
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
		pressed = false;
		SavedImages = new Stack();
		RedoImages = new Stack();
		PolygonCoordinatesX = new Vector();
		PolygonCoordinatesY = new Vector();
	}

	/**
	 * method to show updated image
	 * 
	 * @param img
	 *            image to display
	 */
	public void showImage(Image img) {
		entryImage = img;
		entryGraphics = entryImage.getGraphics();
		repaint();
	}

	/**
	 * method to get entryimage
	 * 
	 * @return entryimage
	 */
	public Image getImage() {
		return entryImage;
	}

	@SuppressWarnings("unchecked")
	protected void initImage() {
		img = entryImage;
		entryGraphics = entryImage.getGraphics();
		Image tmg = createImage(entryImage.getWidth(this), entryImage.getHeight(this));
		Graphics tg = tmg.getGraphics();
		tg.drawImage(entryImage, 0, 0, null);
		SavedImages.push(tmg);
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		if (entryImage == null) {
			initImage();
		}
		g.drawImage(entryImage, 0, 0, this);

		/**
		 * Draw red rectangle on first selected co-ordinate of a polygon
		 */
		if (firstX != -1) {
			g.setColor(Color.red);
			g.drawRect(firstX - 5, firstY - 5, 10, 10);
			g.drawRect(firstX - 4, firstY - 4, 8, 8);
		}

		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.green);

		if (PolygonCoordinatesX == null || PolygonCoordinatesY == null) {
			return;
		}

		/**
		 * Draw black rectangle on every other co-ordinate of a polygon except the first co-ordinate
		 */
		g.setColor(Color.black);
		for (int i = 1; i < PolygonCoordinatesX.size(); ++i) {
			g.drawRect((Integer) PolygonCoordinatesX.get(i) - 5, (Integer) PolygonCoordinatesY.get(i) - 5, 10, 10);
			g.drawRect((Integer) PolygonCoordinatesX.get(i) - 4, (Integer) PolygonCoordinatesY.get(i) - 4, 8, 8);
		}
		g.setColor(Color.green);

		if (lastX != -1) {
			g.drawLine(lastX, lastY, currX, currY); // draw line between successive co-ordinates
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processMouseEvent(MouseEvent e) {
		if (!isDisabled) {
			if (e.getID() == MouseEvent.MOUSE_PRESSED && !pressed) {
				lastX = e.getX();
				lastY = e.getY();
				firstX = lastX;
				firstY = lastY;
				pressed = true;
				PolygonCoordinatesX.add(lastX);
				PolygonCoordinatesY.add(lastY);
				repaint();
			} else if (e.getID() == MouseEvent.MOUSE_PRESSED && pressed) {
				entryGraphics.setColor(Color.green);
				entryGraphics.drawLine(lastX, lastY, e.getX(), e.getY());
				getGraphics().drawImage(entryImage, 0, 0, this);
				repaint();
				lastX = e.getX();
				lastY = e.getY();
				maxX = maxY = -1;
				minX = lastX;
				minY = lastY;
				PolygonCoordinatesX.add(lastX);
				PolygonCoordinatesY.add(lastY);
				if (Math.abs(lastX - (Integer) PolygonCoordinatesX.get(0)) < 10 && Math.abs(lastY - (Integer) PolygonCoordinatesY.get(0)) < 10) {
					int[] PolyX = new int[PolygonCoordinatesX.size()];
					int[] PolyY = new int[PolygonCoordinatesY.size()];
					for (int i = 0; i < PolygonCoordinatesX.size(); ++i) {
						PolyX[i] = (Integer) PolygonCoordinatesX.get(i);
						PolyY[i] = (Integer) PolygonCoordinatesY.get(i);

						if (minX > PolyX[i]) {
							minX = PolyX[i];
						}
						if (minY > PolyY[i]) {
							minY = PolyY[i];
						}
						if (maxX < PolyX[i]) {
							maxX = PolyX[i];
						}
						if (maxY < PolyY[i]) {
							maxY = PolyY[i];
						}
						System.out.println(PolygonCoordinatesX.get(i) + ", " + PolygonCoordinatesY.get(i));
					}
					polySides = PolygonCoordinatesX.size();
					entryGraphics.fillPolygon(PolyX, PolyY, polySides);
					PolygonCoordinatesX.clear();
					PolygonCoordinatesY.clear();
					currX = currY = -1;
					lastX = lastY = -1;
					firstX = firstY = -1;
					pressed = false;
					Image tmg = createImage(entryImage.getWidth(this), entryImage.getHeight(this));
					Graphics tg = tmg.getGraphics();
					tg.drawImage(entryImage, 0, 0, null);
					// UndoImages.add(tmg);
					SavedImages.push(tmg);
					RedoImages.clear();
					repaint();
				}
			} else {
				return;
			}
		}
	}

	@Override
	protected void processMouseMotionEvent(MouseEvent e) {
		if (!isDisabled) {
			if (e.getID() != MouseEvent.MOUSE_MOVED) {
				return;
			}

			currX = e.getX();
			currY = e.getY();
			repaint();
		}
	}

	void setDisabled() {
		isDisabled = true;
	}

	void setEnabled() {
		isDisabled = false;
	}

	public Boolean getPressed() {
		return pressed;
	}

	void entryReset() {
		PolygonCoordinatesX.clear();
		PolygonCoordinatesY.clear();
		currX = currY = -1;
		lastX = lastY = -1;
		firstX = firstY = -1;
		pressed = false;
	}

	Object entryImage() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
