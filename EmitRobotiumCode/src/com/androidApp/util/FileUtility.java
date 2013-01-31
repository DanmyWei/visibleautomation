package com.androidApp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.androidApp.emitter.EmitRobotiumCode;

public class FileUtility {
	// I'm so lazy, but I'm lazy-fast
	static int sByteArraySize = 0;
	protected static final String TAG = "FileUtility";
	protected static final String TEMPLATES = "/templates/";

	// read into a rubber bytearray.  Keep the length in a static.
	public static byte[] readToByteArray(InputStream is, int bufferIncrement) throws IOException {
		byte[] buffer = new byte[bufferIncrement];
		int offset = 0;
		int nbytesReadPass = 0;
		do {
			int nbytesRead = is.read(buffer, offset, bufferIncrement - nbytesReadPass);
			if (nbytesRead <= 0) {
				sByteArraySize = offset;
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
		return buffer;	
	}

	public static int getLastByteArrayReadSize() {
		return sByteArraySize;
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
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + templateName);
		return readToString(fis);
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
	 * write a list of lines to a file
	 * @param bw BufferedWriter
	 * @param lines list of lines
	 * @throws IOException
	 */
	public static void writeLines(BufferedWriter bw, List<String> lines) throws IOException {
		for (String line : lines) {
			bw.write(line);
		}
	}
}
