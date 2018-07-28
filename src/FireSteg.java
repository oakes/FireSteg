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

import jargs.gnu.CmdLineParser;
import invisibleinktoolkit.stego.InsertableMessage;
import invisibleinktoolkit.stego.CoverImage;
import invisibleinktoolkit.stego.RetrievedMessage;
import invisibleinktoolkit.util.Shot;
import invisibleinktoolkit.gui.WorkingPanel;

import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import my.crypto.Cryptor;

public class FireSteg {

    private static void printUsage() {
        System.err.println(
"Usage: FireSteg [-e,--encode] [-p,--pass] [-f,--file] [-c,--cover] [-o,--output] [--dfile] [--dcover]");
    }

    public static void main( String[] args ) throws Exception {
		
        // First, we create a command line parser.
        // If -e exists, we're encoding; otherwise, we're decoding.
		// If we're encoding, -p marks the password to encrypt with.
        // If we're encoding, -f marks the file(s) we want to hide.
		// In both cases, -c marks the cover image and -o marks the output directory.
		
        CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option encode = parser.addBooleanOption('e', "encode");
		CmdLineParser.Option pass = parser.addStringOption('p', "pass");
		CmdLineParser.Option file = parser.addStringOption('f', "file");
		CmdLineParser.Option cover = parser.addStringOption('c', "cover");
		CmdLineParser.Option output = parser.addStringOption('o', "output");
		//The following two are for files/covers whose original should be deleted afterwards
		CmdLineParser.Option dfile = parser.addStringOption("dfile");
		CmdLineParser.Option dcover = parser.addStringOption("dcover");

        //Next, we parse the command line arguments, and catch any errors therein.
		
        try {
            parser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
            printUsage();
            System.exit(2);
        }

        //Next, we get the value(s) from the arguments we just parsed.
		
		Boolean encodeValue = (Boolean)parser.getOptionValue(encode, Boolean.FALSE);
		password = (String)parser.getOptionValue(pass);
        Vector fileValues = parser.getOptionValues(file);
		Vector coverValues = parser.getOptionValues(cover);
		outputValue = (String)parser.getOptionValue(output);
		Vector dfileValues = parser.getOptionValues(dfile);
		Vector dcoverValues = parser.getOptionValues(dcover);

		//Finally, we perform the action.
		
		CoverImage tempCover;
		
		//First, we create the list of cover images, since we always need to do this.
		imageList = new ArrayList<CoverImage>();
		Enumeration c = coverValues.elements();
		while (c.hasMoreElements()) {
			tempCover = new CoverImage((String)c.nextElement(), false);
			pixelCount = pixelCount + tempCover.getPixelCount();
			imageList.add(tempCover);
		}
		
		c = dcoverValues.elements();
		while (c.hasMoreElements()) {
			tempCover = new CoverImage((String)c.nextElement(), true);
			pixelCount = pixelCount + tempCover.getPixelCount();
			imageList.add(tempCover);
		}
		
		pane = new WorkingPanel();
		
		if (encodeValue) { //if we're encoding
		
			pane.show();
			pane.setLabel("Initializing...");
			
			InsertableMessage tempFile;
			
			//Create the list of files to be hidden
			fileList = new ArrayList<InsertableMessage>();
			Enumeration f = fileValues.elements();
			while (f.hasMoreElements()) {
				tempFile = new InsertableMessage((String)f.nextElement(), false, password);
				totalFileSize = totalFileSize + tempFile.getSize() * 8 + tempFile.getName().length * 8 + sizeOfFileMetadata;
				fileList.add(tempFile);
			}
			
			f = dfileValues.elements();
			while (f.hasMoreElements()) {
				tempFile = new InsertableMessage((String)f.nextElement(), true, password);
				totalFileSize = totalFileSize + tempFile.getSize() * 8 + tempFile.getName().length * 8 + sizeOfFileMetadata;
				fileList.add(tempFile);
			}
			
			pane.setMax((int) totalFileSize);
			
			//Exit if they haven't supplied enough info
			if (imageList.size() < 1 || fileList.size() < 1 || outputValue == null) {
				JOptionPane.showMessageDialog(null, 
							"To encode, you must select at least one file and cover image.",
							"Error!",
							JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			
			//Exit if the cover images don't have enough room
			if (pixelCount * 3 * 8 - sizeOfImageMetadata * imageList.size() < totalFileSize) {
				JOptionPane.showMessageDialog(null, 
							"Not enough room to hide.",
							"Error!",
							JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			
			//Determine the smallest number of bits we can write to
			bitNumber = 7;
			while (bitNumber > 0 && pixelCount * 3 * bitNumber - sizeOfImageMetadata * imageList.size() > totalFileSize) {
				bitNumber = bitNumber - 1;
			}
			
			//Begin encoding, one cover image at a time
			for ( currentImage=0; currentImage<imageList.size(); currentImage++ ) {
				pane.setLabel("Encoding " + (currentImage + 1) + " of " + imageList.size() + ": " + imageList.get(currentImage).getName());
				encode();
				pane.setLabel("Saving " + (currentImage + 1) + " of " + imageList.size() + ": " + imageList.get(currentImage).getName());
				ImageIO.write(imageList.get(currentImage).getImage(), "png", new File(outputValue + "/" + imageList.get(currentImage).getName()));
				
				if (currentFile+1 > fileList.size()) break;
			}
		}
		else { //if we're decoding
		
			pane.show();
			pane.setLabel("Initializing...");
			
			//Exit if they haven't supplied enough info
			if (imageList.size() < 1 || outputValue == null) {
				JOptionPane.showMessageDialog(null, 
							"To decode, you must select at least one image.",
							"Error!",
							JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			
			//get the bit number
			bitNumber = getInt(0, 0);
			
			if (bitNumber < 0 || bitNumber > 7) {
				JOptionPane.showMessageDialog(null, 
											  "This doesn't appear to be an encoded image.",
											  "Error!",
											  JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
			
			//must artifically increase this count because
			//the bitNumber was written at the lowest significant bit
			bitsWrittenForImage *= 8;
			
			//get the token
			long token = getLong(bitNumber, 0);
			
			while (true) {
				if(Cryptor.checkToken(token, password)) {
					break;
				}
				else {
					JLabel label = new JLabel("Please try a new password.");
					JPasswordField jpf = new JPasswordField();
					int result = JOptionPane.showConfirmDialog(null,
						new Object[]{label, jpf}, "Password incorrect!",
						JOptionPane.OK_CANCEL_OPTION);
					
					if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION)
						System.exit(1);
					else {
						password = new String(jpf.getPassword());
						//using a blank string for decryption results in a divide by zero error,
						//so we need to supply a default password for those who try it
						if (password.equals(""))
							password = "analrape";
					}
				}
			}
			
			//get the next three numbers stored
			totalFileCount = getInt(bitNumber, 0);
			totalFileSize = getLong(bitNumber, 0);
			int coverCount = getInt(bitNumber, 0);
			
			pane.setMax((int) totalFileSize);
			
			int i;
			
			//get the order numbers of each image and store them in an arraylist
			ArrayList<Integer> orderList = new ArrayList<Integer>();
			for ( i=0; i<imageList.size(); i++ ) {
				//must artificially set this count to be the last 32 bits
				//of the metadata space because we're working on a new image each time
				bitsWrittenForImage = sizeOfImageMetadata-32;
				
				int ordernum = getInt(bitNumber, i);
				
				if (orderList.indexOf(new Integer(ordernum)) == -1) orderList.add(new Integer(ordernum));
				else { //there's more than one cover image with the same order number
					JOptionPane.showMessageDialog(null, 
							"At least two of your images have the same order number.",
							"Error!",
							JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
			}
			
			//Exit if the order list isn't completely in line
			for ( i=0; i<orderList.size(); i++ ) {
				if (orderList.indexOf(new Integer(i)) == -1) {
					JOptionPane.showMessageDialog(null,
							"You either are missing images or are trying to decode images that don't belong together.",
							"Error!",
							JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
			}
			
			//Begin decoding, one cover image at a time, in the correct order
			for ( i=0; i<imageList.size(); i++ ) {
				currentImage = orderList.indexOf(new Integer(i));
				decode();
			}
		}
		
		pane.hide();
        System.exit(0);
    }
	
	//Function for encoding file(s) into image(s).
	//First, four numbers are encoded in the beginning of the image:
	//Bit number (int), password checker (long), total file count (int), total file size (long), cover count (int) and cover order (int).
	//Then, before each file, their size (long), namelength (int), and name is encoded
	//Finally, the file itself is encoded.

	public static void encode() throws Exception {
		long messagesize = fileList.get(currentFile).getSize();
		final int messagecount = fileList.size();
		byte[] chars = fileList.get(currentFile).getName();
		bitsWrittenForImage = 0;
		bitsLeftForImage = imageList.get(currentImage).getPixelCount() * 3 * (bitNumber+1);
		nameLength = fileList.get(currentFile).getName().length;
		int coverCount = imageList.size();
		
		//set the bitnumber
		setInt(0, bitNumber);
		
		//must artifically increase this count because
		//the bitNumber was written at the lowest significant bit
		bitsWrittenForImage *= 8;
		bitsLeftForImage = imageList.get(currentImage).getPixelCount() * 3 * (bitNumber+1) - bitsWrittenForImage;
		
		//set the other four numbers
		setLong(bitNumber, Cryptor.getToken(password));
		setInt(bitNumber, messagecount);
		setLong(bitNumber, totalFileSize);
		setInt(bitNumber, coverCount);
		setInt(bitNumber, currentImage);
		
		//Enter the header info and then the file itself
		while (bitsLeftForImage > 0) {
			
			if (bitsLeftForImage % 100000 == 0)
				pane.addValue(100000);
			
			if (!fileList.get(currentFile).isHeaderFinished()) { //still need to put the header info in
				
				if (bitCounter < 64) { //put in the file size
					setBit(bitNumber, ((messagesize >> bitCounter) & 0x1) == 0x1);
					bitCounter++;
				}
				else if (bitCounter < 96) { //put in the name length
					setBit(bitNumber, ((nameLength >> bitCounter % 32) & 0x1) == 0x1);
					bitCounter++;
				}
				else { //put in the name
					setBit(bitNumber, (((int) chars[intCounter] >> bitCounter % 8) & 0x1) == 0x1);
					
					bitCounter++;
					if (bitCounter % 8 == 0) {
						intCounter++;
					}
				}
				
				if (bitCounter == sizeOfFileMetadata + nameLength*8) {
					fileList.get(currentFile).headerIsFinished();
				}
			}
			else if (!fileList.get(currentFile).isFinished()) {				
				setBit(bitNumber, fileList.get(currentFile).nextBit());
			}
			else { //finished writing this file
				currentFile++;
				if (currentFile == messagecount) { //if there are no more files
					return;
				}
				else { //reset these variables to get ready for the next file
					chars = fileList.get(currentFile).getName();
					bitCounter = 0;
					intCounter = 0;
					nameLength = fileList.get(currentFile).getName().length;
					messagesize = fileList.get(currentFile).getSize();
				}
			}
		}
	}
	
	public static void decode() throws Exception {
		bitsWrittenForImage = sizeOfImageMetadata;
		bitsLeftForImage = imageList.get(currentImage).getPixelCount() * 3 * (bitNumber+1) - sizeOfImageMetadata;
		long temp = 0;
		
		while(bitsLeftForImage > 0) { //the cover image has more to go
			if (bitsWrittenForFile < 64) { //need to get file size
				temp = temp << 1 | getBit(bitNumber, currentImage);
				
				if (bitsWrittenForFile==63) { //finished getting file size
					//reverse it as it was retrieved backwards
					for(int z = 0; z < 64; z++){
						bitsLeftForFile = bitsLeftForFile << 1 | ((temp >> z) & 0x1);
					}
					bitsLeftForFile = bitsLeftForFile * 8;
					temp = 0;
				}
				
				bitsWrittenForFile++;
			}
			else if (bitsWrittenForFile < sizeOfFileMetadata) { //need to get nameLength
				temp = temp << 1 | getBit(bitNumber, currentImage);
				
				if (bitsWrittenForFile==95) { //finished getting nameLength
					//reverse it as it was retrieved backwards
					for(int y = 0; y < 32; y++){
						nameLength = nameLength << 1 | (((int) temp >> y) & 0x1);
					}
					temp =0;
				}
				fileName = new byte[nameLength];
				bitsWrittenForFile++;
			}
			else if (bitsWrittenForFile < nameLength*8 + sizeOfFileMetadata) { //need to get name
				temp = temp << 1 | getBit(bitNumber, currentImage);
				
				bitsWrittenForFile++;
				
				if (bitsWrittenForFile % 8 == 0) { //finished getting byte
					int temp2 = 0;
					//reverse it as it was retrieved backwards
					for(int x = 0; x < 8; x++){
						temp2 = temp2 << 1 | (((int) temp >> x) & 0x1);
					}
					fileName[intCounter] = (byte) temp2;
					intCounter++;
				}
				
				if (bitsWrittenForFile == nameLength*8 + sizeOfFileMetadata) { //finished getting name
					rmess = new RetrievedMessage(outputValue, fileName, password);
					password = rmess.getPassword(); //in case the password was updated by the user
					fileName = rmess.getName(); //if the name is encrypted, this will decrypt it
					pane.setLabel("Decoding " + (currentFile + 1) + " of " + totalFileCount + ": " + new String(fileName));
					intCounter = 0;
				}
			}
			else if (bitsLeftForFile > 0) { //need to get file
				//start retrieving and writing out the message
				Shot sh = getShot(bitNumber);
				rmess.setNext( (imageList.get(currentImage).getPixelBit(sh.getX(), sh.getY(), sh.getLayer(), sh.getBitPosition())) == 0x1);
				bitsLeftForFile--;
				if (bitsLeftForFile % 100000 == 0) pane.addValue(100000);
			}
			else { //file is done
				rmess.close();
				password = rmess.getPassword(); //in case the password was updated by the user
				currentFile++;
				if (currentFile+1 > totalFileCount) { //if there are no more files
					return;
				}
				else {
					bitsWrittenForFile = 0;
				}
			}
		}
	}
	
	public static Shot getShot(int bitnum) {
		int height = imageList.get(currentImage).getImage().getHeight();
		int width = imageList.get(currentImage).getImage().getWidth();
		
		//get the number of rows written so far...
		int bitsperpixel = bitnum + 1;
		if(height * width * bitsperpixel * 3 < bitsWrittenForImage)
			return null;
		int rangeupto = (int)(bitsWrittenForImage % (bitsperpixel * 3));
		int xrow = (int)(((bitsWrittenForImage - rangeupto)/(bitsperpixel * 3)) % width);
		int yrow = (int)((((bitsWrittenForImage - rangeupto)/(bitsperpixel * 3)) - xrow) / width);			 
		Shot sh = new Shot(xrow, yrow, rangeupto % bitsperpixel, ((rangeupto - (rangeupto % bitsperpixel)) / bitsperpixel));
		bitsLeftForImage--;
		bitsWrittenForImage++;
		return sh;
	}
	
	public static void setBit(int bitnum, boolean b) {
		Shot sh = getShot(bitnum);
		imageList.get(currentImage).setPixelBit(sh.getX(), sh.getY(), sh.getLayer(), sh.getBitPosition(), b);
	}
	
	public static int getBit(int bitnum, int coverImage) {
		Shot sh = getShot(bitNumber);
		return imageList.get(coverImage).getPixelBit(sh.getX(), sh.getY(), sh.getLayer(), sh.getBitPosition());
	}
	
	public static void setInt(int bitnum, int n) {
		for(int i = 0; i < 32; i++) { 
			Shot sh = getShot(bitnum);
			boolean bit = ((n >> i) & 0x1) == 0x1;
			imageList.get(currentImage).setPixelBit(sh.getX(), sh.getY(), sh.getLayer(), sh.getBitPosition(), bit);
		}
	}
	
	public static int getInt(int bitnum, int coverImage) {
		int temp = 0, n = 0;
		//get the bit number
		for(int i = 0; i < 32; i++){
			temp = temp << 1 | getBit(bitnum, coverImage);
		}
		//reverse it as it was retrieved backwards
		for(int j = 0; j < 32; j++){
			n = n << 1 | ((temp >> j) & 0x1);
		}
		return n;
	}
	
	public static void setLong(int bitnum, long n) {
		for(int i = 0; i < 64; i++) { 
			Shot sh = getShot(bitnum);
			boolean bit = ((n >> i) & 0x1) == 0x1;
			imageList.get(currentImage).setPixelBit(sh.getX(), sh.getY(), sh.getLayer(), sh.getBitPosition(), bit);
		}
	}
	
	public static long getLong(int bitnum, int coverImage) {
		long temp = 0, n = 0;
		//get the bit number
		for(int i = 0; i < 64; i++){
			temp = temp << 1 | getBit(bitnum, coverImage);
		}
		//reverse it as it was retrieved backwards
		for(int j = 0; j < 64; j++){
			n = n << 1 | ((temp >> j) & 0x1);
		}
		return n;
	}
	
	/* Variables used exclusively during encoding */
	
	private static ArrayList<InsertableMessage> fileList; //holds all the files you want to hide
	private static long pixelCount = 0; //keeps track of the total number of pixels in all cover images
	private static int bitCounter = 0; //number of bits taken up by the file size and nameLength
	private static int intCounter = 0; //number of ints taken up by the file name
	
	/* Variables used exclusively during decoding */
	
	private static RetrievedMessage rmess; //a file being retrieved
	private static long bitsLeftForFile = 0; //bits left in the file
	private static long bitsWrittenForFile = 0; //bits of the file written so far
	private static int totalFileCount = 0;
	//private static String fileName = "";
	private static byte[] fileName;
	
	/* Variables used during both encoding and decoding */
	
	private static String outputValue; //directory the retrieved files will be saved in
	private static ArrayList<CoverImage> imageList; //holds all the cover images
	private static int sizeOfImageMetadata = 8*32 + 2*64 + 3*32; //8 ints for bitnumber, 2 longs and 3 ints for the five other numbers
	private static int sizeOfFileMetadata = 64 + 32; //includes only file size and name length, not the name itself
	private static int currentImage; //the cover image currently being worked on
	private static int currentFile = 0; //the file currently being hidden or extracted
	private static long bitsLeftForImage = 0; //bits left in the cover image
	private static long bitsWrittenForImage = 0; //bits read/written so far in the cover image
	private static int nameLength = 0;
	private static String password;
	private static int bitNumber = 0;
	private static long totalFileSize = 0; //keeps track of the cumulative size of all the files you're hiding
	private static WorkingPanel pane;
}