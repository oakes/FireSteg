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

package invisibleinktoolkit.stego;

import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;


/**
 * A cover image is an image that can hide a message.
 * <P>
 * A cover image can be any image that is on disk.  In this case
 * images that are not in a 24 bit colour format will be refused.
 * All the algorithms implemented in this toolkit rely solely on
 * 24 bit formats.
 * 
 * @author Kathryn Hempstalk.
 */
public class CoverImage{
	
	//CONSTRUCTORS
	
	/**
	 * Creates a new cover image, from a given path to the image.
	 *
	 * @param path The path to the image on disk.
	 * @throws IOException If there is a problem during reading
	 * of the file.
	 * @throws IllegalArgumentException If the file is not a 
	 * recognisable type.
	 * @throws NullPointerException If the path is null.
	 */
	public CoverImage(String path, Boolean shouldDelete) throws IOException, 
	IllegalArgumentException,
	NullPointerException{
		
		mPath = path;
		File temp = new File(path);		
		mName = temp.getName();
		if (!mName.endsWith(".png") && !mName.endsWith(".PNG")) mName = mName + ".png";
		
		//read in the image - appropriate reader will be 
		//automatically chosen from the registered list.
		mCover = ImageIO.read(temp);
		
		if (shouldDelete) temp.delete();
		
		if(mCover == null){
			throw new IllegalArgumentException
			("File type is not a recognisable type.");
		}
		
		//check that it is a type we can deal with
		if(this.getLayerCount() <= 1 
				|| mCover.getType() == BufferedImage.TYPE_USHORT_555_RGB
				|| mCover.getType() == BufferedImage.TYPE_USHORT_565_RGB){
			throw new IllegalArgumentException
			("Picture colour depth is not deep enough!");
		}
		
	}
	
	
	//FUNCTIONS
	
	/**
	 * Gets the number of layers the image has.
	 * <P>
	 * The number of layers is determined by the colour depth of
	 * the image.  If a colour has a 24 bit colour depth, then the
	 * number of layers is 3, one for each of red, green and blue. In
	 * contrast, if the colour depth is 8, 1 layer is returned.
	 * <P>
	 * To summarise, 24 bits = 3 layers, 16 bit = 3 layers, 8 bit = 1.
	 * Images which do not have a deep enough set of colours return 0.
	 *
	 * @return The number of "layers" an image has.
	 */
	public int getLayerCount(){
		int type = mCover.getType();
		
		if (type == BufferedImage.TYPE_BYTE_BINARY)
			//1, 2 and 4 bit images
			return 0;
		else if (type == BufferedImage.TYPE_BYTE_INDEXED 
				|| type == BufferedImage.TYPE_BYTE_GRAY
				|| type == BufferedImage.TYPE_USHORT_GRAY)
			//8 bit images
			return 1;
		else
			//all other image types
			return 3;
	}
	
	/**
	* Gets the number of pixels the image has.
	*/
	
	public int getPixelCount(){
		return mCover.getHeight() * mCover.getWidth();
	}
	
	/**
	 * Gets the name of the image.
	 */
	public String getName(){
		return mName;
	}
	
	/**
	 * Gets the image that is currently residing inside
	 * this cover.
	 *
	 * @return The image inside this cover.
	 */
	public BufferedImage getImage(){
		return mCover;
	}
	
	/**
	 * Gets a particular bit in the image, and puts
	 * it into the LSB of an integer.
	 *
	 * @param xpos The x position of the pixel on the image.
	 * @param ypos The y position of the pixel on the image.
	 * @param layer The layer (R,G,B) containing the bit.
	 * @param bitpos The bit position (0 - LSB -> 7 - MSB).
	 * @return The bit at the given position, as the LSB of an integer.
	 */
	public int getPixelBit(int xpos, int ypos, int layer, int bitpos){
		int pixel = mCover.getRGB(xpos, ypos);
		int layerpos = (layer * 8) + bitpos;
		return ((pixel >> layerpos) & 0x1);
	}
	
	/**
	 * Sets the pixel bit at the given location
	 * to the new value.
	 *
	 * @param xpos The x position of the pixel.
	 * @param ypos The y position of the pixel.
	 * @param layer The layer of the bit.
	 * @param bitpos The position of the bit (0-7 or 0-4)
	 * @param newbit The new bit for the pixel.
	 * @throws IllegalArgumentException If the bit position is not
	 * in the right range, or the layer is incorrect.
	 */
	public void setPixelBit(int xpos, int ypos, int layer, int bitpos,
			boolean newbit) 
	throws IllegalArgumentException{
		
		//check layer
		if(layer > this.getLayerCount() || layer < 0 )
			throw new IllegalArgumentException
			("Layer is incorrect for image type!");
		
		
		//get the pixel we want to work on
		int pixel = mCover.getRGB(xpos, ypos);
		
		int newcolour = 0, newpixel;
		
		//hide the bit
		if(newbit){
			newcolour = 1;
			newcolour = newcolour << (bitpos + (layer * 8));
			newpixel = pixel | newcolour;
		}else{
			newcolour = 0xfffffffe;
			for(int i = 0; i < (bitpos + (layer * 8)); i++){
				newcolour = (newcolour << 1) | 0x1;
			}
			newpixel = pixel & newcolour;		 
		}
		
		//now set the pixel.
		mCover.setRGB(xpos, ypos, newpixel);
	}
	
	
	/**
	 * Matches the pixel bit instead of just overwriting it.
	 * <P>
	 * LSB matching is where the LSB of a number is matched instead of replaced. For example,
	 * if the new bit is 1 and the old bit is 1 then they match and nothing is changed.  If the new
	 * bit is 0 and the old bit is 1, then we either add or subtract 1 from the entire colour value.
	 * This has the same effect as overwriting (the old bit becomes 0) but reproduces natural variation
	 * in images more truely.  Of course, this is a problem for the filtered algorithms since they
	 * require the colour to be about the same, but this implementation leaves the bits used for
	 * filtering outside of the arithmetic.
	 * 
	 * @param x The x position of the pixel.
	 * @param y The y position of the pixel.
	 * @param layer The layer (colour) the bit to be changed is part of.
	 * @param maxChangePosition The maximum position to change - ie where the filter starts reading.
	 * @param newbit The new bit.
	 * @param subtract Whether to add or subtract one when it doesn't match.
	 * 
	 * @throws IllegalArgumentException If something incorrect was passed.
	 */
	public void matchPixelBit(int x, int y, int layer, 
			int maxChangePosition, boolean newbit, boolean subtract) 
	throws IllegalArgumentException{
		//check layer
		if(layer > this.getLayerCount() || layer < 0 )
			throw new IllegalArgumentException
			("Layer is incorrect for image type!");
		
		//get the pixel we want to work on
		int pixel = mCover.getRGB(x, y);
				
		byte thiscolour = (byte)((pixel >> (layer * 8)) & 0x000000ff);
		
		if( ((thiscolour & 0x1) == 0x1 && newbit) ||
			((thiscolour & 0x1) == 0x0 && !newbit)){
			//do nothing, bit matches.			
			return;
		}else{
			//subtract or add one.
			
			//MORE CODE HERE TO COPE WITH FILTERS...
			int leftmask = 0, rightmask = 0;
			rightmask = rightmask | 0x000000ff;
			for(int i = 0; i < maxChangePosition; i++){
				leftmask = (leftmask << 1) | 0x1;
				rightmask = (rightmask << 1) & 0x000000fe;
			}
			int tochange = thiscolour & leftmask;
			int top = thiscolour & rightmask;
			
			//make up the new colour
			int newcolour;
			if(subtract){
				 newcolour= ((int)(tochange & 0x000000ff)) - 1;
			}else{
				//add
				newcolour = ((int)(tochange & 0x000000ff)) + 1;
			}
			
			//cover out of range problems
			if (newcolour > leftmask && newbit)
				newcolour = leftmask;
			else if (newcolour > leftmask && !newbit)
				newcolour = leftmask - 1;
			if(newcolour < 0 && newbit){
				newcolour = 1;
			}
			if(newcolour < 0 && !newbit){
				newcolour = 0;
			}
			
			//put the top part back on
			newcolour = (newcolour | top) & 0x000000ff;			
			
			byte[] colours = new byte[4];
			for(int i = 0; i < 4; i++){
				colours[i] = (byte)((pixel >> (i * 8)) & 0x000000ff);
			}
			colours[layer] = (byte)newcolour;
			int finalcolour = 0;
			for(int i = 3; i >= 0 ; i--){
				finalcolour = ((finalcolour << 8) | (colours[i] & 0xff)) & 0xffffffff; 
			}			
			
			// now set the pixel.
			mCover.setRGB(x, y, finalcolour);			
		}		
	}
	
	//VARIABLES
	
	/**
	 * An image representation of the cover.
	 */
	private BufferedImage mCover;
	
	/**
	 * The path to the message (on disk).
	 */
	private String mPath;
	
	private String mName;
}
//end of class.
