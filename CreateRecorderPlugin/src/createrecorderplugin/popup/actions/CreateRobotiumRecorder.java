package createrecorderplugin.popup.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import com.android.ide.eclipse.adt.AdtConstants;

import createrecorder.util.FileUtility;
import createrecorderplugin.parser.ManifestParser;
import createrecorderplugin.parser.ProjectParser;
import createrecorderplugin.parser.ProjectPropertiesScan;


public class CreateRobotiumRecorder implements IObjectActionDelegate {
	private static final String ALLTESTS_FILE = "AllTests.java";
	protected final String PROJECT_FILENAME = ".project";
	protected final String CLASSPATH_TEMPLATE = "classpath_template.txt";
	protected final String CLASSPATH_FILENAME = ".classpath";
	protected final String MANIFEST_TEMPLATE = "manifest_template.txt";
	protected final String MANIFEST_FILENAME = "AndroidManifest.xml";
	protected final String PROJECT_PROPERTIES_TEMPLATE = "project_properties.txt";
	protected final String PROJECT_PROPERTIES_FILENAME = "project.properties";
	protected final String PROJECT_TEMPLATE = "project_template.txt";
	protected final String TESTCLASS_TEMPLATE = "testclass_template.txt";
	protected final String ALLTESTS_TEMPLATE = "AllTests.txt";
	protected final String CLASSPATH_VARIABLE = "%CLASSPATH%";
	protected final String CLASSPACKAGE_VARIABLE = "%CLASSPACKAGE%";
	protected final String CLASSNAME_VARIABLE = "%CLASSNAME%";
	protected final String TARGET_VARIABLE = "%TARGET%";
	protected final String MIN_SDK_VERSION_VARIABLE = "%MIN_SDK_VERSION%";
	protected final String ANDROID_MANIFEST_XML = "AndroidManifest.xml";
	protected final String RECORDER_SUFFIX = "Recorder";
	protected final String RECORDER_JAR = "recorder.jar";
	protected final String SRC_FOLDER = "src";
	protected final String GEN_FOLDER = "gen";
	protected final String LIBS_FOLDER = "libs";
	protected final String RES_FOLDER = "res";
	protected final String TEST_EXTENSION = ".test";
	protected final String TEMPLATES_SOURCE_FOLDER = "/templates/";
	
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
				File manifestFile = new File(projectDir, ANDROID_MANIFEST_XML);
				File projectFile = new File(projectDir, PROJECT_FILENAME);
				File projectPropertiesFile = new File(projectDir, PROJECT_PROPERTIES_FILENAME);
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
				String newProjectName = project.getName() + RECORDER_SUFFIX;
				IProject testProject = createBaseProject(newProjectName);
				
				// create the .classpath, AndroidManifest.xml, .project, and project.properties files
				createClasspath(testProject, projectParser);
				createManifest(testProject, manifestParser);
				createProject(testProject, projectParser);
				createProjectProperties(testProject, projectParser, projectPropertiesScan);
				IJavaProject javaProject = createJavaNature(testProject);
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
		String classpath = FileUtility.readTemplate(CLASSPATH_TEMPLATE);
		classpath = classpath.replace(CLASSNAME_VARIABLE, projectParser.getProjectName());
		IFile file = testProject.getFile(CLASSPATH_FILENAME);
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
		String manifest = FileUtility.readTemplate(MANIFEST_TEMPLATE);
		manifest = manifest.replace(CLASSPACKAGE_VARIABLE, manifestParser.getPackage());
		manifest = manifest.replace(MIN_SDK_VERSION_VARIABLE, manifestParser.getMinSDKVersion());
		IFile file = testProject.getFile(MANIFEST_FILENAME);
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
		String project = FileUtility.readTemplate(PROJECT_TEMPLATE);
		project = project.replace(CLASSNAME_VARIABLE, projectParser.getProjectName());
		IFile file = testProject.getFile(PROJECT_FILENAME);
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
		String projectProperties = FileUtility.readTemplate(PROJECT_PROPERTIES_TEMPLATE);
		projectProperties = projectProperties.replace(TARGET_VARIABLE, propertiesScan.getTarget());
		projectProperties = projectProperties.replace(CLASSNAME_VARIABLE, projectParser.getProjectName());
		IFile file = testProject.getFile(PROJECT_PROPERTIES_FILENAME);
		file.delete(false, null);
		InputStream is = new StringBufferInputStream(projectProperties);
		file.create(is, IFile.FORCE, null);
	}
	

	
	/**
	 *  we like-us some java project nature.  With Birkenstocks
	 * @param project project reference
	 * @return java project
	 * @throws CoreException
	 */
	public IJavaProject createJavaNature(IProject project) throws CoreException {
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
		String allTests = FileUtility.readTemplate(ALLTESTS_TEMPLATE);
		allTests = allTests.replace(CLASSPACKAGE_VARIABLE, manifestParser.getPackage());
		IFolder sourceFolder = testProject.getFolder(SRC_FOLDER);
		String packagePath = manifestParser.getPackage() + TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		ICompilationUnit classFile = pack.createCompilationUnit(ALLTESTS_FILE, allTests, true, null);	
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
		String testClass = FileUtility.readTemplate(TESTCLASS_TEMPLATE);
		testClass = testClass.replace(CLASSPACKAGE_VARIABLE, manifestParser.getPackage());
		testClass = testClass.replace(CLASSNAME_VARIABLE, manifestParser.getStartActivity());
		// write to the fully qualified path
		IFolder sourceFolder = CreateRobotiumRecorder.createFolder(testProject, SRC_FOLDER);
		IFolder genFolder = CreateRobotiumRecorder.createFolder(testProject, GEN_FOLDER);
		IFolder resFolder = CreateRobotiumRecorder.createFolder(testProject, RES_FOLDER);
		String packagePath = manifestParser.getPackage() + TEST_EXTENSION;
		IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment(packagePath, false, null);
		String startActivity = manifestParser.getStartActivity();
		ICompilationUnit classFile = pack.createCompilationUnit(startActivity + RECORDER_SUFFIX + ".java", testClass, true, null);
 	}
	
	/**
	 * create the libs directory and put the recorder.jar file in it
	 * @param testProject
	 * @throws CoreException
	 */
	public void addLibrary(IProject testProject) throws CoreException {
	
		IFolder libFolder = CreateRobotiumRecorder.createFolder(testProject, LIBS_FOLDER);
		IFile file = testProject.getFile(LIBS_FOLDER + '/' + RECORDER_JAR);
		InputStream fis = CreateRobotiumRecorder.class.getResourceAsStream(TEMPLATES_SOURCE_FOLDER + RECORDER_JAR);
		file.create(fis, IFile.FORCE, null);	
	}
	
	/**
	 * create the actual project
	 * @param projectName name of the test project
	 * @return created project reference
	 */
	
	private static IProject createBaseProject(String projectName) {
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
	
 
    //com.android.ide.eclipse.adt.AndroidNature
    private static void addAndroidNature(IProject project) throws CoreException {
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
 
}
