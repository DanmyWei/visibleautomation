package com.visibleautomation.whitelist;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
 * -c "cd $ANDROID_HOME/platforms; 
 * for dir in `ls -d android-*`; 
 * do targetfile=`echo whitelist_$dir.txt | sed -e 's/\-/_/g'`; 
 * echo $targetfile; 
 *  jar -tf $dir/android.jar | grep -f ${project_loc}/android_package_filter.txt | sed -e 's/\.class$//g' | sed -e 's/\//./g' > ${project_loc}/res/raw/$targetfile
done"
 */
public class AndroidWhitelist {
	
	public static void main(String[] args) {
		try {
			String androidDir = System.getenv("ANDROID_HOME");
			List<String> packageFilter = readToLines(new FileInputStream(args[0]));
			File platformsDir = new File(androidDir + File.separator + "platforms");
			File[] platformFiles = platformsDir.listFiles();
			File destDir = new File(args[1]);
			for (File platformFile : platformFiles) {
				File destFile = new File(destDir, platformFile.getName());
				whitelistAndroidJar(platformFile, packageFilter, destFile);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	public static void whitelistAndroidJar(File 			androidJar, 
										    List<String> 	packageFilter, 
										    File 			whitelistFile) throws FileNotFoundException, IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(whitelistFile));
		String[] jarFiles = getShellCommandOutput("jar -tf " + androidJar.getAbsolutePath() + File.separator + "android.jar");
		for (String classPath : jarFiles) {
			classPath = classPath.replace(".class", "").replace("/", ".");
			if (inPackageList(packageFilter, classPath)) {
				bw.write(classPath + "\n");
			}
		}
		bw.close();
	}
	
	public static String[] getShellCommandOutput(String cmd) {
		Process proc = null;
		InputStream procStream = null;
		List<String> lines = new ArrayList<String>();
		try {
            proc = Runtime.getRuntime().exec(cmd);
  		  	BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
  		  	while ((line = input.readLine()) != null) {
  		  		lines.add(line);
  		  	}
  		  	input.close();
  		  	String[] array = new String[lines.size()];
  		  	lines.toArray(array);
  		  	return array;
        } catch (IOException e) {
            System.err.println("failed to execute " + cmd + " " + e.getMessage());
        }
		return null;
 	}
	
	public static List<String> readToLines(InputStream is) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		String line = buf.readLine();
		while (line != null) {	
			if (line != null) {
				lines.add(line);
			}
			line = buf.readLine();
		}
		return lines;
	}
	
	public static boolean inPackageList(List<String> stringList, String s) {
		for (String cand : stringList) {
			if (s.startsWith(cand)) {
				return true;
			}
		}
		return false;
	}


}
