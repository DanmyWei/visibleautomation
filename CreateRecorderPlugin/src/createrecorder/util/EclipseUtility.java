package createrecorder.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.android.ide.eclipse.adt.AdtConstants;
import com.androidApp.emitter.EmitRobotiumCode;
import com.androidApp.emitter.EmitterException;
import com.androidApp.emitter.EmitRobotiumCode.LineAndTokens;
import com.androidApp.util.Constants;
import com.androidApp.util.StringUtils;

public class EclipseUtility {

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
	public static void writeString(IProject project, String filename, String s) throws CoreException {
		IFile file = project.getFile(filename);
		file.delete(false, null);
		InputStream is = new StringBufferInputStream(s);
		file.create(is, IFile.FORCE, null);
	}
	

	public static void writeResource(IFolder folder, String resourceName) throws CoreException {
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + resourceName);
		IFile file = folder.getFile(resourceName);
		file.delete(false, null);
		file.create(fis, IFile.FORCE, null);	
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
				ichDot = candFileName.indexOf('.');
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
}
