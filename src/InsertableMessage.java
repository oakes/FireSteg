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
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import my.crypto.Cryptor;

/**
 * A message to hide.
 * <P>
 * A message is a file on disk that will be embedded into
 * a cover object in order to hide it.  It can read out the
 * message in terms of 0's and 1's (boolean false and true) to
 * make life easier for the encoding process.
 *
 * @author Kathryn Hempstalk.
 */
public class InsertableMessage{
	
	public InsertableMessage(String path, Boolean shouldDelete, String password) throws 
	Exception{
		
		mPath = path;
		mFile = new File(path);
		mName = mFile.getName().getBytes();
		
		//Pull the file into memory
		byte[] plaintext = new byte[(int)mFile.length()];
		FileInputStream tempStream = new FileInputStream(path);
		tempStream.read(plaintext);
		
		//Encrypt the file and filename
		byte[] ciphertext = Cryptor.encrypt(plaintext, password);
		mFileSize = ciphertext.length;
		mMsgFile = new ByteArrayInputStream(ciphertext);
		mName = Cryptor.encrypt(mFile.getName().getBytes(), password);
		
		mBuffer = new byte[1];
		mIsFileFinished = false;
		mIsHeaderFinished = false;
		mShouldDelete = shouldDelete;
		
		//get the first byte
		int status = mMsgFile.read(mBuffer);
		mCount = 8;
		if (status == -1)
			throw new IOException("File is empty!");
	}
	
	public boolean nextBit() throws IOException{
		//first check if we need to get another byte.
		if(mIsFileFinished)
			throw new IOException("File reading has finished!");
		
		//have byte, must manipulate to get bits
		boolean bit = (((mBuffer[0] >> (mCount - 1)) &0x1) == 0x1);
		mCount--;
		
		if(mCount == 0){
			//get another byte
			int status = mMsgFile.read(mBuffer);
			mCount = 8;
			if( status == -1){
				mIsFileFinished = true;
				if (mShouldDelete) mFile.delete();
				mMsgFile.close();
			}
		}
		
		return bit;
	}
	
	public boolean isFinished(){
		return mIsFileFinished;
	}
	
	public boolean isHeaderFinished(){
		return mIsHeaderFinished;
	}
	
	public void headerIsFinished(){
		mIsHeaderFinished = true;
	}
	
	public long getSize() {		
		return mFileSize;
	}
	
	public byte[] getName(){
		return mName;
	}
	
	private String mPath;
	private int mCount;
	private InputStream mMsgFile;
	private byte[] mBuffer;
	private boolean mIsFileFinished;
	private boolean mIsHeaderFinished;
	private byte[] mName;
	private File mFile;
	private Boolean mShouldDelete;
	private long mFileSize;
}
