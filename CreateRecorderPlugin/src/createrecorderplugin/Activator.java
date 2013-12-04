package createrecorderplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/*
import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.ValidationStatus;
*/
import createrecorder.util.RecorderConstants.EnvironmentVariables;

/**
 * The activator class controls the plug-in life cycle
 * @author Matthew Reynolds
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 */
public class Activator extends AbstractUIPlugin {

    private static String publickey = "30819f300d06092a864886f70d010101050003818d003081893032301006072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e00043019c7a5cf2700ca82e855f742dc492d2fd42dea6916daf4c2a92173G02818100b1ffd83b41cf40b79f4d49d290e6067f8f9be81e0b635655d7decdd774a111344c5d1c245e8eb0b503fbd95bc9ff07f0147c99a8e41ebc26717096efe003b72e1b17d58d84431f2da63f6ecf67be8a725ac2b72c9118b3c6e9ecbbd3ae4aa98c03RSA4102413SHA512withRSA56b09be3d77f7c1208663344935d71c1d70d3a793d56b2ef684127f31549b15f0203010001";
    private static String internalString = "Im curious as to what you have to say because it had better be really good";
    // The plug-in ID
	public static final String PLUGIN_ID = "CreateRecorderPlugin"; //$NON-NLS-1$
	public static final String LICENSE_FILE = ".valicense";
	public static final String KEY = "key";
	public static final String NAME = "name";
	public static final String COMPANY = "company";
	public static final int HARDWARE_ID_METHOD = 0;
	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/*
	public static boolean verifyLicense() {
		String licenseFilename = System.getenv(EnvironmentVariables.HOME) + File.separator + LICENSE_FILE;
		String key = null;
		String name = null;
		String company = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(licenseFilename));
			String line = br.readLine();
			while (line != null) {
				String[] tokens = line.split(":");
				if (tokens[0].equals(KEY)) {
					key = tokens[1];
				} else if (tokens[0].equals(NAME)) {
					name = tokens[1];
				} else if (tokens[0].equals(COMPANY)) {
					company = tokens[1];
				}
				line = br.readLine();
			}	
		} catch (Exception ex) {
			return false;
		}

       License license = LicenseValidator.validate(key, publickey, internalString, name, company, HARDWARE_ID_METHOD);
       return (license.getValidationStatus() == ValidationStatus.LICENSE_VALID);
	}
	*/
}
