package createrecorder.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.androidApp.util.FileUtility;

import createrecorder.util.Exec;
import createrecorder.util.RecorderConstants;

// menu handler to install the log service and keyboard apks to support the recorder.
public class InstallRecorderHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = window.getShell();
		try {
			installAPKFromTemplate(RecorderConstants.LOGSERVICE_APK, RecorderConstants.LOGSERVICE_PACKAGE);
			installAPKFromTemplate(RecorderConstants.KEYBOARD_APK, RecorderConstants.KEYBOARD_PACKAGE);
		} catch (Exception ex) {
			MessageDialog.openInformation(
					shell,
					"CreateRecorderPlugin",
					"there was an exception installing the robotium test support APKS " + ex.getMessage());
			
		}
		return null;
	}
	
	public static boolean isPackageInstalled(String packageName) {
		String[] results = Exec.getAdbCommandOutput("shell pm list packages -3 | grep " + packageName);
		return results.length > 1;
	}

	public static void installAPKFromTemplate(String templateFile, String packageName) throws IOException {
		if (!isPackageInstalled(packageName)) {
			FileUtility.writeResource(templateFile);
			Exec.executeAdbCommand("install " + templateFile);
		}
	}
}
