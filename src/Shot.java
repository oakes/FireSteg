/*
 *    FireSteg
 *    Copyright (C) 2009  Zachary Oakes
 *
 *	  Digital Invisible Ink Toolkit
 *    Copyright (C) 2005  K. Hempstalk	
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package invisibleinktoolkit.util;  

/**
 * A shot on a cover image.
 * <P>
 * A shot is a (x,y) co-ordinate and bit position triplet
 * that is used to define an attempted "shot" onto a
 * steganographic image.  They represent the position of the 
 * pixel on the image, and the position of the bit that will
 * be changed by this "shot".
 *
 * @author Kathryn Hempstalk
 */
public class Shot{
	
	//CONSTRUCTORS
	
	/**
	 * Creates a new shot with a default bit position of 0 and layer of 0.
	 *
	 * @param xpos The x position of the shot.
	 * @param ypos The y position of the shot.
	 */
	public Shot(int xpos, int ypos){
		this(xpos, ypos, 0, 0);
	}
	
	/**
	 * Creates a new shot.
	 *
	 * @param xpos The x position of the shot.
	 * @param ypos The y position of the shot.
	 * @param bitpos The bit position of the shot.
	 * @param layer The layer for the shot.
	 */
	public Shot(int xpos, int ypos, int bitpos, int layer){
		mXPosition = xpos;
		mYPosition = ypos;
		mBitPosition = bitpos;
		mLayer = layer;
	}
	
	
	//FUNCTIONS
	
	/**
	 * Gets the x position of the shot.
	 *
	 * @return The x position of the shot.
	 */
	public int getX(){
		return mXPosition;
	}
	
	/**
	 * Gets the y position of the shot.
	 *
	 * @return The y position of the shot.
	 */
	public int getY(){
		return mYPosition;
	}
	
	/**
	 * Gets the bit position for the shot.
	 *
	 * @return The bit position for the shot.
	 */
	public int getBitPosition(){
		return mBitPosition;
	}
	
	/**
	 * Gets the layer for the shot.
	 *
	 * @return The bit position for the shot.
	 */
	public int getLayer(){
		return mLayer;
	}
	
	/**
	 * Provides a string representation of the shot.
	 *
	 * @return A string representation of the shot.
	 */
	public String toString(){
		return "X: " + mXPosition + " Y: " + mYPosition +
		" layer: " + mLayer + " bit: " + mBitPosition;
	}
	
	/**
	 * Provides a string representation of the shot, without a bit.
	 *
	 * @return A string representation of the shot, without the bit.
	 */
	public String toStringMinusBit(){
		return "X: " + mXPosition + " Y: " + mYPosition +
		" layer: " + mLayer;
	}
	
	/**
	 * Provides a string representation of the shot, without layer or bit.
	 *
	 * @return A string representation of the shot, just x and y positions.
	 */
	public String toStringXandY(){
		return "X: " + mXPosition + " Y: " + mYPosition;
	}
	
	
	//VARIABLES
	
	/**
	 * The x position of the pixel.
	 */
	private int mXPosition;
	
	/**
	 * The y position of the pixel.
	 */
	private int mYPosition;
	
	/**
	 * The bit position on the pixel.
	 */
	private int mBitPosition;
	
	/**
	 * The layer of the shot (0-based).
	 */
	private int mLayer;
	
}
//end of class
