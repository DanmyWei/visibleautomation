package createrecorder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import com.android.ide.eclipse.adt.AdtConstants;
import com.androidApp.emitter.EmitRobotiumCode;
import com.androidApp.util.AndroidUtil;
import com.androidApp.util.Constants;
import com.androidApp.util.Exec;
import com.androidApp.util.FileUtility;
import com.androidApp.util.StringUtils;

/**
 * Utility functions for the eclipse plugin
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class EclipseUtility {
	private static MessageConsole sMessageConsole = null;
	private static IOConsoleOutputStream sMessageConsoleStream = null;
	/**
	 *  we like-us some java project nature.  With Birkenstocks
	 * @param project project reference
	 * @return java project
	 * @throws CoreException
	 */
	public static IJavaProject createJavaNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
	
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = JavaCore.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
		
		// Now we can finally create a JavaProject:
		IJavaProject javaProject = JavaCore.create(project);
		return javaProject;
	}
	
	
	/**
	 * utility function to create a folder in a project
	 * @param folder
	 * @throws CoreException
	 */
	private static void createFolder(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder) {
			createFolder((IFolder) parent);
		}
		if (!folder.exists()) {
			folder.create(false, true, null);
		}
	}

	/**
	 * utility to create a folder in a project
	 * @param project
	 * @param folderName name of the folder to create
	 * @return reference to the folder
	 * @throws CoreException
	 */
	public static IFolder createFolder(IProject project, String folderName) {
		IFolder folder = project.getFolder(folderName);
		try {
			folder.create(true, true, null);
		} catch (CoreException cex) {
		}
		return folder;
	}
	
	/**
	 * utility to create a subfolder under a folder.
	 * @param folder
	 * @param subFolderName
	 * @return
	 */
	public static IFolder createFolder(IFolder folder, String subFolderName) {
		IFolder subfolder = folder.getFolder(subFolderName);
		try {
			subfolder.create(true, true, null);
		} catch (CoreException cex) {
		}
		return subfolder;
	}
	
	/**
	 * find a file which matches the specified regular expression
	 * @param container container to search
	 * @param regex regular expression to match
	 * @return
	 * @throws CoreException
	 */
	public static IFile findFile(IContainer container, String regex) throws CoreException {
		IResource[] resources = container.members();
		for (IResource resource : resources) {
			if (resource.getType() == IResource.FILE) {
				if (resource.getName().matches(regex)) {
					return (IFile) resource;
				}
			}
		}
		return null;
	}

	//com.android.ide.eclipse.adt.AndroidNature
	public static void addAndroidNature(IProject project) throws CoreException {
	    if (!project.hasNature(AdtConstants.NATURE_DEFAULT)) {
	        IProjectDescription description = project.getDescription();
	        String[] prevNatures = description.getNatureIds();
	        String[] newNatures = new String[prevNatures.length + 1];
	        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
	        newNatures[prevNatures.length] = AdtConstants.NATURE_DEFAULT;
	        description.setNatureIds(newNatures);
	
	        IProgressMonitor monitor = null;
	        project.setDescription(description, monitor);
	    }
	}

	/**
	 * create the actual project
	 * @param projectName name of the test project
	 * @return created project reference
	 */
	
	public static IProject createBaseProject(String projectName) {
		// it is acceptable to use the ResourcesPlugin class
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!newProject.exists()) {
			IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
			try {
				newProject.create(desc, null);
				if (!newProject.isOpen()) {
					newProject.open(null);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return newProject;
	}
	
	/**
	 * write a file with contents s to filename
	 * @param project project reference
	 * @param filename file to write to
	 * @param s string to write to the file.
	 * @throws CoreException
	 */
	public static void writeString(IProject project, String filename, String s) throws CoreException, IOException {
		IFile file = project.getFile(filename);
		file.delete(false, null);
		InputStream is = new StringBufferInputStream(s);
		file.create(is, IFile.FORCE, null);
		is.close();
	}
	
	public static void writeString(IFolder folder, String filename, String s) throws CoreException, IOException {
		IFile file = folder.getFile(filename);
		file.delete(false, null);
		InputStream is = new StringBufferInputStream(s);
		file.create(is, IFile.FORCE, null);
		is.close();
	}

	public static void writeResource(IFolder folder, String resourceName) throws CoreException, IOException {
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + resourceName);
		IFile file = folder.getFile(resourceName);
		file.delete(false, null);
		file.create(fis, IFile.FORCE, null);	
		fis.close();
	}
	
	public static void writeFile(IFolder folder, String srcFileName, String dstFileName) throws CoreException, FileNotFoundException, IOException {
		InputStream fis = new FileInputStream(srcFileName);
		IFile file = folder.getFile(dstFileName);
		file.delete(false, null);
		file.create(fis, IFile.FORCE, null);	
		fis.close();
	}
	
	public static void copyFileToProjectDirectory(IProject project, String srcFileName, String dstFileName) throws CoreException, FileNotFoundException, IOException {
		InputStream fis = new FileInputStream(srcFileName);
		IFile file = project.getFile(dstFileName);
		file.delete(false, null);
		file.create(fis, IFile.FORCE, null);	
		fis.close();
	}
	
	public static void copyFileToProjectDirectory(IFolder srcFolder, String srcFileName, IProject project, String dstFileName) throws CoreException, FileNotFoundException, IOException {
		IFile srcFile = srcFolder.getFile(srcFileName);
		InputStream is = srcFile.getContents();
		IFile dstFile = project.getFile(dstFileName);
		dstFile.delete(false, null);
		dstFile.create(is, IFile.FORCE, null);
		is.close();
		
	}

	
	/** 
	 * given a folder, and a templateFileName, return a number which if suffixed to that templateFile, would return 
	 * a unique filename.  return 0 if there's no matching template
	 * @param folder eclipse folder
	 * @param templateFilename template file to match
	 * @return max suffix index or 0 if no match
	 * @throws CoreException
	 */
	public static int uniqueFileIndex(IFolder folder, String templateFilename) throws CoreException {
		
		// get rid of the file extension.  we want foo2.java not foo.java2
		int ichDot = templateFilename.lastIndexOf('.');
		String name = templateFilename;
		if (ichDot != -1) {
			name = templateFilename.substring(0, ichDot);
		}
		
		// scan through the elements in the folder that start with the same name.
		int maxFileNumber = 0;
		IResource[] members = folder.members();
		int fileMatchCount = 0;
		for (IResource candFile : members) {
			if (candFile.getName().startsWith(name)) {
				fileMatchCount++;
				String candFileName = candFile.getName();
				String numericSuffix = "0";
				
				// strip the extension off the candidate.
				ichDot = candFileName.lastIndexOf('.');
				if (ichDot != -1) {
					numericSuffix = candFileName.substring(name.length(), ichDot);
				} else {
					numericSuffix = candFileName.substring(name.length());
				}
				
				// max value comparison
				if (StringUtils.isNumber(numericSuffix)) {
					int candFileNumber = Integer.parseInt(numericSuffix);
					if (candFileNumber > maxFileNumber) {
						maxFileNumber = candFileNumber;
					}
				}
			}
		}
		if (fileMatchCount == 0) {
			return 0;
		} else {
			return maxFileNumber + 1;
		}
	}
	
	/**
	 * utility to create an eclipse console.
	 * @param name
	 * @return
	 */
	private static MessageConsole findConsole(String name) {
		  ConsolePlugin plugin = ConsolePlugin.getDefault();
		  IConsoleManager conMan = plugin.getConsoleManager();
		  IConsole[] existing = conMan.getConsoles();
		  for (int i = 0; i<existing.length; i++){
			  if (name.equals(existing[i].getName())){
				  return (MessageConsole)existing[i];
			  }
		  }
		  //no console found -> create new one
		  MessageConsole newConsole = new MessageConsole(name, null);
		  conMan.addConsoles(new IConsole[]{newConsole});
		  return newConsole;
	}
	
	/**
	 * print to the eclipse console
	 * @param s
	 */
	public static void printConsole(String s) {
		if (sMessageConsole == null) {
			sMessageConsole = EclipseUtility.findConsole(RecorderConstants.VISIBLE_AUTOMATION);
			sMessageConsoleStream = sMessageConsole.newOutputStream();
		}
		try {
			sMessageConsoleStream.write(s + "\n");
		} catch (IOException ex) {
			System.out.println(s);
		}
	}
	
	/**
	 * a lot of the shell functions output arrays of strings, so this utility is handy
	 */
	public static void printConsole(String[] sarray) {
		if (sMessageConsole == null) {
			sMessageConsole = EclipseUtility.findConsole(RecorderConstants.VISIBLE_AUTOMATION);
			sMessageConsoleStream = sMessageConsole.newOutputStream();
		}
		try {
			for (int i = 0; i < sarray.length; i++) {
				sMessageConsoleStream.write(sarray[i] + "\n");
			}
		} catch (IOException ex) {
			for (int i = 0; i < sarray.length; i++) {
				System.out.println(sarray[i] + "\n");
			}
		}
	}
	
	
	/**
	 * they keep moving aapt around, so we have to search for it
	 * @param androidSDK
	 * @return
	 */
	public static File findAAPT(String androidSDK) {
		String buildToolsPath = androidSDK;
		File buildToolsDir = new File(buildToolsPath);
		String os = Platform.getOS();
		if (os.equals(Platform.OS_WIN32)) {	
			return FileUtility.findFile(buildToolsDir, RecorderConstants.AAPT_WIN32);
		} else {
			return FileUtility.findFile(buildToolsDir, RecorderConstants.AAPT);			
		}
	}
	
	/**
	 * same thing for dexdump
	 * @param android SDK location
	 * @return full path to ex.
	 */
	public static File findDexdump(String androidSDK) {
		String buildToolsPath = androidSDK;
		File buildToolsDir = new File(buildToolsPath);
		String os = Platform.getOS();
		if (os.equals(Platform.OS_WIN32)) {	
			return FileUtility.findFile(buildToolsDir, RecorderConstants.DEXDUMP_WIN32);
		} else {
			return FileUtility.findFile(buildToolsDir, RecorderConstants.DEXDUMP);			
		}
	}
	
	
	// change the.class.name to the/class/path
	public static String classNameToPath(String className) {
		return className.replace('.', File.separatorChar);
	}
	// change the.class.name to the/class/path
	public static String pathToClassName(String className) {
		return className.replace(File.separatorChar, '.');
	}
	
	/**
	 * get the same or next higher android SDK, or the SDK if it wasn't found.
	 * @param level
	 * @return
	 */
	public static int getBestAndroidSDKLevel(int level) {
		IPreferencesService service = Platform.getPreferencesService();
		String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
		return AndroidUtil.getBestAndroidSDKLevel(androidSDK, level);
	}
	
	/**
	 * recorder needs 4.0 or better.
	 * @return
	 */
	public static int getRecorderAndroidSDKLevel() {
		IPreferencesService service = Platform.getPreferencesService();
		String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
		return AndroidUtil.getBestAndroidSDKLevel(androidSDK, Constants.ANDROID_40);
	}
	
	/**
	 * is the specified APK installed on the device?
	 * @param packageName package.name to search for
	 * @return true if it's returned by pm list packages
	 */
	public static boolean isAPKInstalled(String packageName) {
		String execStr = "shell pm list packages " + packageName;
		String[] results = EclipseExec.getAdbCommandOutput(execStr);
		// output is of the form package:package.name
		for (int i = 0; i < results.length; i++) {
			int ichColon = results[i].indexOf(':');
			String candPackage = results[i].substring(ichColon + 1);
			if (candPackage.equals(packageName)) {
				return true;
			}
		}
		return false;
	}
	

    /**
     * given the list of jarfiles, create the .classpath file entries.
     */
    public static String createJarClasspathEntries(List<String> jarfiles) {
        StringBuffer sb = new StringBuffer();
        for (String jarfile : jarfiles) {
            sb.append("\t<classpathentry exported=\"true\" kind=\"lib\" path=\"libs/" + jarfile + "\"/>\n");
        }
        return sb.toString();
    }

	/**
	 * run dexdump on the specified APK, then search for references contained in the various support libraries
	 * and map them to the appropriate jar files so we don't get undefined class references.
	 * @param apkFilename name of the .APK file we copied to the recorder project
	 * @return list of the names of the support libraries used in the project.
	 */
	public static List<String> getSupportLibraries(String apkFilename) {
		HashSet<String> supportLibrarySet = new HashSet<String>();
		IPreferencesService service = Platform.getPreferencesService();
		String androidSDK = service.getString(RecorderConstants.ECLIPSE_ADT, RecorderConstants.ANDROID_SDK, null, null);
		File dexdump = EclipseUtility.findDexdump(androidSDK);
		String dexdumpCommand = dexdump.getAbsolutePath() + " " + apkFilename;
		String[] dexdumpOutput = Exec.getShellCommandOutput(dexdumpCommand);
		
		boolean fV13References = false;
		for (int i = 0; i < dexdumpOutput.length; i++) {
			if (dexdumpOutput[i].contains(RecorderConstants.SupportClasses.SUPPORT_V4)) {	
				supportLibrarySet.add(RecorderConstants.SupportLibraries.SUPPORT_V4);
			} else if (dexdumpOutput[i].contains(RecorderConstants.SupportClasses.SUPPORT_V13)) {
				// the v13 library contains v4 references, so we have to remove the v4 library if v13 was found.
				supportLibrarySet.add(RecorderConstants.SupportLibraries.SUPPORT_V13);
				fV13References = true;
			} else if (dexdumpOutput[i].contains(RecorderConstants.SupportClasses.SUPPORT_V7_APPCOMPAT)) {
				supportLibrarySet.add(RecorderConstants.SupportLibraries.SUPPORT_V7_APPCOMPAT);
			} else if (dexdumpOutput[i].contains(RecorderConstants.SupportClasses.SUPPORT_V7_GRIDLAYOUT)) {
				supportLibrarySet.add(RecorderConstants.SupportLibraries.SUPPORT_V7_GRIDLAYOUT);
			} else if (dexdumpOutput[i].contains(RecorderConstants.SupportClasses.SUPPORT_V7_MEDIA)) {
				supportLibrarySet.add(RecorderConstants.SupportLibraries.SUPPORT_V7_MEDIA);
			}
		}
		// it seems that everyone uses v7-appcompat, so we add it explicitly if there are any other support libraries
		if (!supportLibrarySet.isEmpty()) {
			supportLibrarySet.add(RecorderConstants.SupportLibraries.SUPPORT_V7_APPCOMPAT);
		}
		// The V13 reference library contains the V4 reference, so we remove it here.
		if (fV13References) {
			supportLibrarySet.remove(RecorderConstants.SupportLibraries.SUPPORT_V4);
		}
		List<String> supportLibraryList = new ArrayList<String>();
		for (String supportLibrary : supportLibrarySet) {
			supportLibraryList.add(supportLibrary);
		}
		return supportLibraryList;
	}	
}
