package createrecorderplugin.popup.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.androidApp.emitter.EmitRobotiumCode;
import com.androidApp.parser.ManifestParser;
import com.androidApp.parser.ProjectParser;
import com.androidApp.parser.ProjectPropertiesScan;
import com.androidApp.util.FileUtility;
import com.androidApp.util.Constants;

import createrecorder.util.EclipseUtility;
import createrecorder.util.RecorderConstants;


public class CreateRobotiumRecorder implements IObjectActionDelegate {
	
	private Shell mShell;
	private StructuredSelection mSelection;
	
	/**
	 * Constructor for Action1.
	 */
	public CreateRobotiumRecorder() {
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
				IWorkspace workspace = project.getWorkspace();
				IWorkspaceRoot workspaceRoot = workspace.getRoot();
				IPath projectPath = project.getLocation();
				File projectDir = projectPath.toFile();
				
				// parse AndroidManifest.xml .project and project.properties
				File manifestFile = new File(projectDir, Constants.Filenames.ANDROID_MANIFEST_XML);
				File projectFile = new File(projectDir, Constants.Filenames.PROJECT_FILENAME);
				File projectPropertiesFile = new File(projectDir, Constants.Filenames.PROJECT_PROPERTIES_FILENAME);
				ManifestParser manifestParser = null;
				ProjectParser projectParser = null;
				ProjectPropertiesScan projectPropertiesScan = null;
				try {
					manifestParser = new ManifestParser(manifestFile);
					projectParser = new ProjectParser(projectFile);
					projectPropertiesScan = new ProjectPropertiesScan(projectPropertiesFile);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// create the new project
				String newProjectName = project.getName() + RecorderConstants.RECORDER_SUFFIX;
				IProject testProject = EclipseUtility.createBaseProject(newProjectName);
				
				// create the .classpath, AndroidManifest.xml, .project, and project.properties files
				createProjectProperties(testProject, projectParser, projectPropertiesScan);
				createProject(testProject, projectParser);
				createClasspath(testProject, projectParser);
				createManifest(testProject, manifestParser);

				// create the java project, the test class output, and the "AllTests" driver which
				// iterates over all the test cases in the folder, so we can run with a single-click
				IJavaProject javaProject = EclipseUtility.createJavaNature(testProject);
				createFolders(testProject);
				createTestClass(testProject, javaProject, projectParser, manifestParser);
				createAllTests(testProject, javaProject, manifestParser);
				addLibrary(testProject);
			} catch (Exception ex) {
				MessageDialog.openInformation(
						mShell,
						"CreateRecorderPlugin",
						"There was an exception creating the test project " + ex.getMessage());
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
	
	/**
	 * add the project under test to the template .classpath file.
	 * @param testProject selected android project that we want to create a recording for
	 * @param projectParser parser for .project file.
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createClasspath(IProject testProject, ProjectParser projectParser) throws CoreException, IOException {
		String classpath = FileUtility.readTemplate(RecorderConstants.CLASSPATH_TEMPLATE);
		classpath = classpath.replace(Constants.VariableNames.CLASSNAME, projectParser.getProjectName());
		IFile file = testProject.getFile(Constants.Filenames.CLASSPATH);
		InputStream is = new StringBufferInputStream(classpath);
		file.create(is, IFile.FORCE, null);
	}
	
	/**
	 * populate the template for the AndroidManifest.xml file
	 * @param testProject android project under test.
	 * @param manifestParser parser for the AndroidManifest.xml file under test.
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createManifest(IProject testProject, ManifestParser manifestParser) throws CoreException, IOException {
		String manifest = FileUtility.readTemplate(RecorderConstants.MANIFEST_TEMPLATE);
		manifest = manifest.replace(Constants.VariableNames.CLASSPACKAGE, manifestParser.getPackage());
		manifest = manifest.replace(Constants.VariableNames.MIN_SDK_VERSION, manifestParser.getMinSDKVersion());
		IFile file = testProject.getFile(Constants.Filenames.ANDROID_MANIFEST_XML);
		InputStream is = new StringBufferInputStream(manifest);
		file.create(is, IFile.FORCE, null);
	}
	
	/**
	 * popupate the .project file.
	 * @param testProject  android project under test.
	 * @param projectParser parser for the .project file under test.
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createProject(IProject testProject, ProjectParser projectParser) throws CoreException, IOException {
		String project = FileUtility.readTemplate(RecorderConstants.PROJECT_TEMPLATE);
		project = project.replace(Constants.VariableNames.CLASSNAME, projectParser.getProjectName());
		project = project.replace(Constants.VariableNames.MODE, Constants.Names.RECORDER);
		IFile file = testProject.getFile(Constants.Filenames.PROJECT_FILENAME);
		file.delete(false, null);
		InputStream is = new StringBufferInputStream(project);
		file.create(is, IFile.FORCE, null);
	}
	
	/**
	 * create the project.properties file
	 * @param testProject reference to the project
	 * @param projectParser parsed information from .properties
	 * @param propertiesScan scanned information from project.properties
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createProjectProperties(IProject testProject, ProjectParser projectParser, ProjectPropertiesScan propertiesScan) throws CoreException, IOException {
		String projectProperties = FileUtility.readTemplate(RecorderConstants.PROJECT_PROPERTIES_TEMPLATE);
		projectProperties = projectProperties.replace(Constants.VariableNames.TARGET, propertiesScan.getTarget());
		EclipseUtility.writeString(testProject, Constants.Filenames.PROJECT_PROPERTIES_FILENAME, projectProperties);
	}
	
	
	/**
	 * create the AllTests.java file, which iterates through the test files and runs them
	 * @param testProject project 
	 * @param javaProject java reference to the project so we can create packages and stuff
	 * @param manifestParser parsed data from AndroidManifest.xml file
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createAllTests(IProject 		testProject,
							   IJavaProject 	javaProject, 
							   ManifestParser	manifestParser) throws CoreException, IOException {
		String allTests = FileUtility.readTemplate(RecorderConstants.ALLTESTS_TEMPLATE);
		allTests = allTests.replace(Constants.VariableNames.CLASSPACKAGE, manifestParser.getPackage());
		IFolder sourceFolder = testProject.getFolder(Constants.Dirs.SRC);
		String packagePath = manifestParser.getPackage() + RecorderConstants.TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		ICompilationUnit classFile = pack.createCompilationUnit(RecorderConstants.ALLTESTS_FILE, allTests, true, null);	
	}
	
	/**
	 * create the src, gen, and res folders
	 * @param testProject
	 */
	public void createFolders(IProject testProject) {
		IFolder sourceFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.SRC);
		IFolder resFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.RES);
	}
	
	/**
	 * create the java file which drives the test
	 * @param testProject project 
	 * @param javaProject java reference to the project so we can create packages and stuff
	 * @param projectParser parsed data from .project file
	 * @param manifestParser parsed data from AndroidManifest.xml file
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createTestClass(IProject 		testProject, 
								IJavaProject 	javaProject, 
								ProjectParser 	projectParser,
								ManifestParser 	manifestParser) throws CoreException, IOException {
		String testClass = FileUtility.readTemplate(RecorderConstants.TESTCLASS_TEMPLATE);
		testClass = testClass.replace(Constants.VariableNames.CLASSPACKAGE, manifestParser.getPackage());
		testClass = testClass.replace(Constants.VariableNames.CLASSNAME, manifestParser.getStartActivity());
		// write to the fully qualified path
		IFolder sourceFolder = testProject.getFolder(Constants.Dirs.SRC);
		String packagePath = manifestParser.getPackage() + RecorderConstants.TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		String startActivity = manifestParser.getStartActivity();
		String javaFile = startActivity + RecorderConstants.RECORDER_SUFFIX + "." + Constants.Extensions.JAVA;
		ICompilationUnit classFile = pack.createCompilationUnit(javaFile, testClass, true, null);
 	}
	
	/**
	 * create the libs directory and put the recorder.jar file in it
	 * @param testProject
	 * @throws CoreException
	 */
	public void addLibrary(IProject testProject) throws CoreException {
	
		IFolder libFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.LIBS);
		IFile file = libFolder.getFile(RecorderConstants.RECORDER_JAR);
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + RecorderConstants.RECORDER_JAR);
		file.create(fis, IFile.FORCE, null);	
	}
}

