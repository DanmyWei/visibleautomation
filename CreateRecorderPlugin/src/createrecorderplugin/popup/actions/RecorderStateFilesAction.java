package createrecorderplugin.popup.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.androidApp.util.Constants;

import createrecorder.util.EclipseExec;

/**
 * when we record a session with the device, we save the files, database, and shared_prefs file into the eclipse
 * workspace. This copies those files back to the /sdcard, and the test driver copies the files into the application's
 * private data directory before running the test, thus restoring the state of the application before playback
 * @author matt2
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class RecorderStateFilesAction  implements IObjectActionDelegate {
	
	private Shell mShell;
	private StructuredSelection mSelection;
	
	/**
	 * Constructor for Action1.
	 */
	public RecorderStateFilesAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		mShell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		mSelection = (StructuredSelection) selection;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 * create the robotium recorder project
	 */
	public void run(IAction action) {
		if (mSelection != null) {
			try {
				IProject project = (IProject) mSelection.getFirstElement();
				IPath projectPath = project.getLocation();
				File projectDir = projectPath.toFile();
				// get the save state folder, and copy all the files from it to the device.
				IFolder saveStateFolder = project.getFolder(Constants.Dirs.SAVESTATE);
				IResource[] testResources = saveStateFolder.members();
				for (IResource testResource : testResources) {
					if (testResource.getType() == IResource.FOLDER) {
						IFolder testFolder = (IFolder) testResource;
						restoreStateFiles(testFolder, testFolder.getName(), Constants.Dirs.EXTERNAL_STORAGE);
					}
				}
			} catch (Exception ex) {
				MessageDialog.openInformation(
						mShell,
						"Install Save State Files",
						"There was an exception running the test project " + ex.getMessage());
				ex.printStackTrace();				
			}
		}
	}
	
	/**
	 * copy the files from the <testClass>/(files|databases|shared_prefs) folder to /sdcard/<testclass>/(files|databases|shared_prefs)
	 * @param testFolder <testClass>
	 * @param testFolderName <testClass>
	 * @param extDir hopefully /sdcard
	 * @throws CoreException
	 */
	public void restoreStateFiles(IFolder testFolder, String testFolderName, String extDir) throws CoreException {
		IFolder filesFolder = testFolder.getFolder(Constants.Dirs.FILES);
		String filesDestPath = testFolderName + File.separator + Constants.Dirs.FILES;
		copyFilesToDevice(filesFolder, filesDestPath, extDir);
		IFolder databasesFolder = testFolder.getFolder(Constants.Dirs.DATABASES);
		String databasesDestPath = testFolderName + File.separator + Constants.Dirs.DATABASES;
		copyFilesToDevice(databasesFolder, databasesDestPath, extDir);
		IFolder prefsFolder = testFolder.getFolder(Constants.Dirs.SHARED_PREFS);
		String prefsDestPath = testFolderName + File.separator + Constants.Dirs.SHARED_PREFS;
		copyFilesToDevice(prefsFolder, prefsDestPath, extDir);	
	}

	/**
	 * copy files from an eclipse folder to a directory under sdcard
	 * @param folder eclipse source folder
	 * @param destDir device destination folder
	 * @param extDir hopefully /sdcard
	 * @throws CoreException
	 */
	public void copyFilesToDevice(IFolder folder, String destDir, String extDir) throws CoreException {
		IResource[] resources = folder.members();
		EclipseExec.executeAdbCommand("shell mkdir -p " + extDir + File.separator + destDir);
		for (IResource resource : resources) {
			if (resource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				String srcFilePath = file.getLocation().toOSString();
				String destFilePath = extDir + File.separator + destDir + File.separator + file.getName();
				EclipseExec.executeAdbCommand("push " + srcFilePath + " " + destFilePath);
			}
		}
	}

}
