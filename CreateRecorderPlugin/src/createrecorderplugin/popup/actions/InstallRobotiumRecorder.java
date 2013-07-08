package createrecorderplugin.popup.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.androidApp.util.FileUtility;

import createrecorder.util.EclipseExec;

/**
 * install the custom keyboard and log service components for recording.
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class InstallRobotiumRecorder implements IObjectActionDelegate {
	
	private Shell mShell;
	private StructuredSelection mSelection;
	
	/**
	 * Constructor for Action1.
	 */
	public InstallRobotiumRecorder() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		mShell = targetPart.getSite().getShell();
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
			} catch (Exception ex) {
				MessageDialog.openInformation(
						mShell,
						"Install Recorder",
						"There was an exception running the test project " + ex.getMessage());
				ex.printStackTrace();				
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		mSelection = (StructuredSelection) selection;
	}

}
