package createproject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

import com.androidApp.emitter.EmitRobotiumCode;
import com.androidApp.emitter.EmitRobotiumCodeSource;
import com.androidApp.emitter.IEmitCode;
import com.androidApp.emitter.IEmitCode.LineAndTokens;
import com.androidApp.emitter.SetupRobotiumProject;
import com.androidApp.util.Constants;
import com.androidApp.util.FileUtility;

import createrecorder.util.EclipseUtility;
import createrecorder.util.Exec;
import createrecorder.util.RecorderConstants;

/**
 * extract the events file from the device, and either create a new project, or add a test class to an
 * existing junit project which plays back the recording
 * @author mattrey
 * Copyright (c) 2013 Matthew Reynolds.  All Rights Reserved.
 *
 */
public class GenerateRobotiumTestCode {	
	
	/**
	 * create the directories required by the test project
	 * src - source directory
	 * res - resources directory
	 * res/drawable - directory for icons and stuff
	 * res/values - directory for strings and stuff
	 * libs - directory for libraries (specifically the robotium jar)
	 * gen - for generated android files
	 * @param testProject reference to project to create directories under
	 */
	public void createFolders(IProject testProject) throws IOException {
		IFolder libFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.LIBS);
		IFolder srcFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.SRC);
		IFolder resFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.RES);
		IFolder drawableFolder = EclipseUtility.createFolder(resFolder, Constants.Dirs.DRAWABLE);
		IFolder genFolder = EclipseUtility.createFolder(testProject, Constants.Dirs.GEN);
	}
	
	public void writeBuildXML(IProject project, String targetClassPath) throws CoreException, IOException {
		String buildXML = SetupRobotiumProject.createBuildXML(targetClassPath);
		EclipseUtility.writeString(project, Constants.Filenames.BUILD_XML, buildXML);
	}
	
	public void writeManifest(IProject project, String testClassName, String testClassPath, String targetPackage) throws CoreException, IOException {
		String manifest = SetupRobotiumProject.createManifest(testClassName, testClassPath, targetPackage); 
		EclipseUtility.writeString(project, Constants.Filenames.ANDROID_MANIFEST_XML, manifest);
	}	
	
	/**
	 * create the project.properties file
	 * @param testProject reference to the project
	 * @param projectParser parsed information from .properties
	 * @param propertiesScan scanned information from project.properties
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createProjectProperties(IProject testProject, String target, String projectName) throws CoreException, IOException {
		String projectProperties = FileUtility.readTemplate(RecorderConstants.PROJECT_PROPERTIES_TEMPLATE);
		projectProperties = projectProperties.replace(Constants.VariableNames.TARGET, target);
		projectProperties = projectProperties.replace(Constants.VariableNames.CLASSNAME, projectName);
		EclipseUtility.writeString(testProject, Constants.Filenames.PROJECT_PROPERTIES_FILENAME, projectProperties);
	}

	
	/**
	 * popupate the .project file.
	 * @param testProject  android project under test.
	 * @param projectParser parser for the .project file under test.
	 * @throws CoreException
	 * @throws IOException
	 */
	public void createProject(IProject testProject, String projectName) throws CoreException, IOException {
		String project = FileUtility.readTemplate(GenerateRobotiumTestCode.class, RecorderConstants.PROJECT_TEMPLATE);
		project = project.replace(Constants.VariableNames.CLASSNAME, projectName);
		project = project.replace(Constants.VariableNames.MODE, Constants.Names.TEST);
		IFile file = testProject.getFile(Constants.Filenames.PROJECT_FILENAME);
		file.delete(false, null);
		InputStream is = new StringBufferInputStream(project);
		file.create(is, IFile.FORCE, null);
	}
	
	/**
	 * copy the project.properties file to the targets
	 * @param project
	 * @throws IOException
	 * @throws CoreException
	 */
	public void copyBuildFiles(IProject project, String target) throws IOException, CoreException {
		String projectProperties = SetupRobotiumProject.createProjectProperties(target);
		EclipseUtility.writeString(project, Constants.Filenames.PROJECT_PROPERTIES, projectProperties);
	}
	
	/**
	 * generate the .classpath file for building the project.  We  add the target project name
	 * for eclipse/ant, and the robotium jar in the libs directory.
	 * @param projectName name of the target project
	 * @param name of the robotium-solo-X.XX.jar
	 * @throws IOException if the file can't be written
	 */
	public void writeClasspath(IProject project, String projectName, String robotiumJar) throws IOException, CoreException {
		String classpath = SetupRobotiumProject.createClasspath(projectName, robotiumJar);
		EclipseUtility.writeString(project, Constants.Filenames.CLASSPATH, classpath);
	}
	
	/**
	 * write out the AllTests.java to the output class directory src\foo\bar\path
	 * @param packagePath com.foo.bar.test
	 * @param applicationClassPath fully.qualified.path.to.application.under.test
	 * @throws IOException if the template can't be found
	 */
	public void copyTestDriverFile(IPackageFragment pack, String packagePath, String applicationClassPath) throws IOException, JavaModelException {
		String allTests = FileUtility.readTemplate(RecorderConstants.ALL_TESTS_CREATETEST);
		allTests = allTests.replace(Constants.VariableNames.CLASSPACKAGE, packagePath);
		ICompilationUnit classFile = pack.createCompilationUnit(RecorderConstants.ALLTESTS_FILE, allTests, true, null);			
	}

	/**
	 * copy the launcher.png file into the res/drawable directorys
	 * @param project
	 * @throws IOException
	 * @throws CoreException
	 */
	public void writeResources(IProject project) throws IOException, CoreException {
		IFolder resFolder = project.getFolder(Constants.Dirs.RES);
		IFolder drawableFolder = resFolder.getFolder(Constants.Dirs.DRAWABLE);
		EclipseUtility.writeResource(drawableFolder, Constants.Filenames.LAUNCHER_PNG);
	}
	
	/**
	 * copy the robotium jar file to the output library directory
	 * @param project target project
	 * @param robotiumJar robotium-solo-version.jar
	 * @throws FileNotFoundException
	 * @throws CoreException
	 */
	public void copyRobotiumJarToLibs(IProject project, String robotiumJarPath, String robotiumJar) throws FileNotFoundException, CoreException {
		FileInputStream fis = new FileInputStream(new File(robotiumJarPath));
		IFolder libsFolder = project.getFolder(Constants.Dirs.LIBS);
		IFile file = libsFolder.getFile(robotiumJar);
		file.delete(false, null);
		file.create(fis, IFile.FORCE, null);
	}
	/**
	 * copy support library to the output directory
	 * @param libraryDir libs directory
	 * @throws IOException if the template can't be found
	 */
	public void copyJarToLibs(IProject project, String templateName) throws IOException, CoreException {
		InputStream fis = EmitRobotiumCode.class.getResourceAsStream("/" + templateName);
		IFolder libsFolder = project.getFolder(Constants.Dirs.LIBS);
		IFile file = libsFolder.getFile(templateName);
		file.delete(false, null);
		file.create(fis, IFile.FORCE, null);
	}

	
	/**
	 * extract the events file from the device via adb if the keyword "device" is specified, otherwise return the
	 * file that was passed ins
	 * @param eventsFilename
	 * @return
	 */
	public String getEventsFile(String androidSdkPath, String eventsFilename) {
		// if he specified device, use adb to pull the events file off the device.
		if (eventsFilename.equals(Constants.Names.DEVICE)) {
			Exec.executeAdbCommand("pull /sdcard/events.txt");
		}
		return Constants.Filenames.EVENTS;
	}
	
	/**
	 * write out the test function from the emitted code generated by the emitter
	 * @param emitter contains application class path and other information used in the output code
	 * @param lines output from the emitter
	 * @param packagePath package.name.for.the.test.package
	 * @param testClassName name of the test class
	 * @param outputCodeFileName output java file
	 * @throws IOException
	 */
	public void writeTestCode(IEmitCode emitter, List<LineAndTokens> lines, String packagePath, String testClassName, String outputCodeFileName) throws IOException {
		// write the header template, the emitter output, and the trailer temoplate.
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputCodeFileName));
		emitter.writeHeader(emitter.getApplicationClassPath(), packagePath, testClassName, emitter.getApplicationClassName(), bw);
		String testFunction = FileUtility.readTemplate(Constants.Templates.TEST_FUNCTION);
		bw.write(testFunction);
		emitter.writeLines(bw, lines);
		emitter.writeTrailer(bw);
		bw.close();
	}
}
