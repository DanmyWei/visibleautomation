package com.androidApp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.androidApp.emitter.EmitRobotiumCode;

/**
 * file utilities used in the robotium code emitter
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 */
public class FileUtility {
	// I'm so lazy, but I'm lazy-fast
	protected static final String TAG = "FileUtility";

	// read into a rubber bytearray.  Keep the length in a static.
	public static byte[] readToByteArray(InputStream is, int bufferIncrement) throws IOException {
		byte[] buffer = new byte[bufferIncrement];
		int byteArraySize = 0;
		int offset = 0;
		int nbytesReadPass = 0;
		do {
			int nbytesRead = is.read(buffer, offset, bufferIncrement - nbytesReadPass);
			if (nbytesRead <= 0) {
				byteArraySize = offset;
				break;
			} else {
				offset += nbytesRead;
				nbytesReadPass += nbytesRead;
				if (nbytesReadPass == bufferIncrement) {
					byte[] newBuffer = new byte[offset + bufferIncrement];
					System.arraycopy(buffer, 0, newBuffer, 0, offset);
					buffer = newBuffer;
					nbytesReadPass = 0;
				}
			}
		} while (true);
		byte[] sizedBuffer = new byte[byteArraySize];
		System.arraycopy(buffer, 0, sizedBuffer, 0, byteArraySize);
		return sizedBuffer;	
	}

	// read from a stream into a string until EOF.
	public static String readToString(InputStream is) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		do {
			String line = buf.readLine();
			if (line != null) {
				sb.append(line);
				sb.append('\n');
			} else {
				return sb.toString();
			}
		} while (true);
	}
	
	/**
	 * read a template file into a string
	 * @param templateName template file from templates directory
	 * @return file read to string
	 * @throws IOException
	 */
	public static String readTemplate(String templateName) throws IOException {
		return readTemplate(EmitRobotiumCode.class, templateName);
	}
	
	/**
	 * read a template file into a string variant with the 
	 * @param templateName template file from templates directory
	 * @return file read to string
	 * @throws IOException
	 */	
	public static String readTemplate(Class cls, String templateName) throws IOException {
		InputStream fis = cls.getResourceAsStream("/" + templateName);
		if (fis == null) {
			throw new IOException("failed to open resource " + templateName);
		}
		return FileUtility.readToString(fis);
	}
	
	/**
	 * read a template file into a bytearray
	 * @param templateName template file from templates directory
	 * @return file read to string
	 * @throws IOException
	 */
	public static byte[] readBinaryTemplate(String templateName) throws IOException {
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + templateName);
		return FileUtility.readToByteArray(fis, 2048);
	}
	
	/**
	 * write a resource out as a file.
	 * @param resourceName name of file in the resource directory
	 * @param targetDirectory directory to write the file to
	 * @throws IOException
	 */
	public static void writeResource(String resourceName, String targetDirectory) throws IOException {
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + resourceName);
		int size = fis.available();
		byte[] data = new byte[size];
		int nbytesRead = fis.read(data);
		if (nbytesRead != size) {
			throw new IOException("size not equal " + size + " !=" + nbytesRead);
		}
		fis.close();
		FileOutputStream fos = new FileOutputStream(targetDirectory + File.separator + resourceName);
		fos.write(data, 0,nbytesRead);
		fos.close();
	}

	/**
	 * write a resource out as a file.
	 * @param resourceName name of file 
	 * @throws IOException
	 */
	public static void writeResource(String resourceName) throws IOException {
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + resourceName);
		int size = fis.available();
		byte[] data = new byte[size];
		int nbytesRead = fis.read(data);
		if (nbytesRead != size) {
			throw new IOException("size not equal " + size + " !=" + nbytesRead);
		}
		fis.close();
		FileOutputStream fos = new FileOutputStream(resourceName);
		fos.write(data, 0,nbytesRead);
		fos.close();
	}

	/**
	 * copy a file from sourcePath to destPath
	 * @param sourcePath
	 * @param destPath
	 * @throws IOException
	 */
	public static void copyFile(String sourcePath, String destPath) throws IOException {
		FileInputStream fis = new FileInputStream(sourcePath);
		FileOutputStream fos = new FileOutputStream(destPath);
		int size = fis.available();
		byte[] data = new byte[size];
		int nbytesRead = fis.read(data);
		if (nbytesRead != size) {
			throw new IOException("size not equal " + size + " !=" + nbytesRead);
		}
		fis.close();
		fos.write(data, 0, nbytesRead);
		fos.close();
	}
	
	/**
	 * generate/a/file/path from.a.java.classpath
	 * @param className from.a.java.classpath
	 * @return generate/a/file/path
	 */
	public static String sourceDirectoryFromClassName(String className) {
		return className.replace('.', File.separatorChar);
	}	
	
	/**
	 * create a directory from a java.class.path
	 * @param className dot-delimited 
	 * @return
	 */
	public static boolean createDirectoryFromClassName(String className) {
		File file = new File(sourceDirectoryFromClassName(className));
		return file.mkdirs();	
	}
	
	/**
	 * given a directory which may contain files containing the name 'templateFilename',
	 * generate an index number such that templatePrefix<index>.templateExtension is unique
	 * @param directory path to directory containing files
	 * @param templateFilename template file to search for
	 * @return an integer of the unique file, or 0 if there was no matching file, or the
	 * directory was not found.
	 */
	public static int uniqueFileIndex(String directory, String templateFilename) {
		
		// strip the extension. We want foo2.java, not foo.java2
		int ichDot = templateFilename.lastIndexOf('.');
		String name = templateFilename;
		if (ichDot != -1) {
			name = templateFilename.substring(0, ichDot);
		}
		
		// check if we need to ever bother
		File dir = new File(directory);
		if (!dir.exists()) {
			return 0;
		}
		File path = new File(dir, templateFilename);
		if (!path.exists()) {
			return 0;
		}
		File[] filesInDir = dir.listFiles();
		int maxFileNumber = 0;
		
		// iterate over the matching files in the directory, strip their extensions, and strip out the numeric suffix if
		// there is one
		for (File candFile : filesInDir) {
			if (candFile.getName().startsWith(name)) {
				String candFileName = candFile.getName();
				String numericSuffix = "0";
				ichDot = candFileName.indexOf('.');
				if (ichDot != -1) {
					numericSuffix = candFileName.substring(name.length(), ichDot);
				} else {
					numericSuffix = candFileName.substring(name.length());
				}
				if (StringUtils.isNumber(numericSuffix)) {
					int candFileNumber = Integer.parseInt(numericSuffix);
					if (candFileNumber > maxFileNumber) {
						maxFileNumber = candFileNumber;
					}
				}
			}
		}
		return maxFileNumber + 1;
	}
	/**
	 * generate a file with a unique number based on a template, so for example ApiDemos.java will become ApiDemos5.java
	 * @param directory directory to search against.
	 * @param filename filename prefix
	 * @return unique indexed file name
	 */
	public static String uniqueIndexedFileName(String directory, String templateFilename) {
		int uniqueIndex = uniqueFileIndex(directory, templateFilename);
		if (uniqueIndex == 0) {
			return templateFilename;
		}
		String extension = null;
		String name = templateFilename;
		int ichDot = templateFilename.lastIndexOf('.');
		if (ichDot != -1) {
			name = templateFilename.substring(0, ichDot);
			extension = templateFilename.substring(ichDot + 1);
		}
		String uniqueFileName =  name + Integer.toString(uniqueIndex);
		if (extension != null) {
			uniqueFileName += "." + extension;
		}
		return uniqueFileName;
	}
	
	/**
	 * write a string to a file
	 * @param file filename to write to
	 * @param s string to write.
	 * @throws IOException
	 */
	public static void writeString(String file, String s) throws IOException {	
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(s);
		bw.close();
	}
}
