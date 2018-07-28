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
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.swing.JOptionPane;

import my.crypto.Cryptor;

/**
 * A retrieved message.
 * <P>
 * A retrieved message is any sort of file, to be written
 * to disk as it is retrieved from a steganographic object.
 *
 * @author Kathryn Hempstalk.
 */
public class RetrievedMessage{
	
	public RetrievedMessage(String outdir, byte[] filename, String password)
	throws FileNotFoundException, SecurityException{
		
		mPassword = password;
		mName = filename;

		//try to decrypt the filename
		try {
			mName = Cryptor.decrypt(mName, mPassword);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
										  "Could not be decrypted. Please report this bug.",
										  "Error!",
										  JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		mRetrievedMessage = new ByteArrayOutputStream();
		mPath = outdir + "/" + new String(mName);
		mIsFinished = false;
		mBitCount = 0;
		mBuffer = 0;
	}
	
	public void setNext(boolean bit) throws IOException{
		
		//check file hasn't been closed
		if(mIsFinished)
			throw new IOException
			("File has finished writing!");
		//set the new bit
		int newbit = 0x0;
		if(bit) newbit = 0x1;
		//change the buffer and increment count
		mBuffer = mBuffer << 1 | newbit;
		mBitCount++;
		
		//if the buffer is full, write it out
		if(mBitCount == 8){
			this.writeBuffer();
		}	    
	}
	
	private void writeBuffer() throws IOException{
		mRetrievedMessage.write(mBuffer);
		mBitCount = 0;
		mBuffer = 0;
	}
	
	public byte[] getName(){		
		return mName;
	}
	
	public String getPassword(){		
		return mPassword;
	}
	
	public String getPath(){
		return mPath;
	}
	
	public void close() throws IOException{
		//check there isn't something left to write
		if(mBitCount > 0){
			//write it
			int left = 8 - mBitCount;
			mBuffer = mBuffer << left;
			this.writeBuffer();
		}
		//close it all off
		mRetrievedMessage.close();
		mIsFinished = true;
		
		byte[] data = mRetrievedMessage.toByteArray();
		
		//try to decrypt the file
		try {				
			byte[] plaintext = Cryptor.decrypt(data, mPassword);
			
			FileOutputStream outputStream = new FileOutputStream(mPath);
			outputStream.write(plaintext);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
										  "Could not be decrypted. Please report this bug.",
										  "Error!",
										  JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}
	
	private ByteArrayOutputStream mRetrievedMessage;
	private boolean mIsFinished;
	private int mBitCount;
	private int mBuffer;
	private String mPath;
	private String mPassword;	
	private byte[] mName;
}
