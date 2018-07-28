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

package my.crypto;

import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.ByteBuffer;

public class Cryptor {
	
	public static byte[] encrypt(byte[] data, String password) throws Exception {
		byte[] salt = new byte[8];
		Random rand = new Random();
		rand.nextBytes(salt);
		
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede");
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 1000);
		
		Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
		cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
		byte[] ciphertext = cipher.doFinal(data);
		
		byte[] combo = new byte[salt.length + ciphertext.length];
		System.arraycopy(salt, 0, combo, 0, salt.length);
		System.arraycopy(ciphertext, 0, combo, salt.length, ciphertext.length);
		
		return combo;
	}
	
	public static byte[] decrypt(byte[] data, String password) throws Exception {
		byte[] salt = new byte[8];
		byte[] ciphertext = new byte[data.length - salt.length];
		
		System.arraycopy(data, 0, salt, 0, salt.length);
		System.arraycopy(data, salt.length, ciphertext, 0, ciphertext.length);
		
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede");
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 1000);
		
		Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
		cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		return cipher.doFinal(ciphertext);
	}
	
	public static long getToken(String password) throws Exception {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(42);
		byte[] encryptedToken = encryptWithoutSalt(bb.array(), password);
		byte[] token = new byte[8];
		System.arraycopy(encryptedToken, 0, token, 0, token.length);
		
		ByteBuffer bb2 = ByteBuffer.wrap(token);
		return bb2.getLong();
	}
	
	public static boolean checkToken(long i, String password) throws Exception {
		long neededToken = getToken(password);
		return i == neededToken;
	}
	
	private static byte[] encryptWithoutSalt (byte[] data, String password) throws Exception {
		byte[] salt = new byte[8];
		
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede");
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 1000);
		
		Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
		cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
		return cipher.doFinal(data);
	}

	private static byte[] decryptWithoutSalt(byte[] data, String password) throws Exception {
		byte[] salt = new byte[8];
		
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede");
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 1000);
		
		Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
		cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		return cipher.doFinal(data);
	}
}
