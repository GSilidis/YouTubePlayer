package com.asuscomm.gsilidis.youtubeplayer;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import org.jnativehook.GlobalScreen;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;


// examples

// Single video:
// https://www.youtube.com/watch?v=3wC2NC6h_tQ - full
// https://www.youtube.com/watch?v=VIDEO_ID - full
// https://youtu.be/3wC2NC6h_tQ - short
// https://youtu.be/VIDEO_ID - short

// Video inside playlist
// https://www.youtube.com/watch?list=PLVuPx0yOoyGBaAki4vLd9gxCpV8WF5bGB&v=TFvk-eJij4M - full
// https://www.youtube.com/watch?list=PLAYLIST_ID&v=VIDEO_ID - full
// https://www.youtube.com/watch?list=PLVuPx0yOoyGBaAki4vLd9gxCpV8WF5bGB - full without certain position
// https://www.youtube.com/watch?list=PLAYLIST_ID - full without certain position
// https://youtu.be/TFvk-eJij4M?list=PLVuPx0yOoyGBaAki4vLd9gxCpV8WF5bGB - short
// https://youtu.be/VIDEO_ID?list=PLAYLIST_ID - short

public class Main
{
	public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, IOException
	{
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF); // Blocks debug messages from JNativeHooks
		File swtJar = new File(getSWTPath());
		addJarToClasspath(swtJar);
		UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		NativeInterface.open();
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				MainWindow frame = new MainWindow("YouTube player");
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.pack();
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
			}
		});
		NativeInterface.runEventPump();
	}


	/**
	 * Used for adding SWT jar to project
	 * @param jarFile location of SWT jar
	 */
	public static void addJarToClasspath(File jarFile)
	{
		try
		{
			URL url = jarFile.toURI().toURL();
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Class<?> urlClass = URLClassLoader.class;
			Method method = urlClass.getDeclaredMethod("addURL", new Class<?>[] { URL.class });
			method.setAccessible(true);
			method.invoke(urlClassLoader, new Object[] { url });
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	/**
	 * Used for getting path to SWT jar that corresponds current system
	 * @return path to SWT
	 */
	private static String getSWTPath()
	{
		String osNameProperty = System.getProperty("os.name");
		if (osNameProperty == null)
		{
			throw new RuntimeException("os.name property is not set");
		}
		else
		{
			osNameProperty = osNameProperty.toLowerCase();
		}
		if (osNameProperty.contains("win"))
		{
			return "lib/org.eclipse.swt.win32.win32." + getArchitecture() + "-4.3.jar";
		}
		else if (osNameProperty.contains("mac"))
		{
			return "lib/org.eclipse.swt.cocoa.macosx.x86_64-4.3.jar"; // SWT for OSX supports only x86_64
		}
		else if (osNameProperty.contains("linux") || osNameProperty.contains("nix"))
		{
			return "lib/org.eclipse.swt.gtk.linux." + getArchitecture() + "-4.3.jar";
		}
		else
		{
			throw new RuntimeException("Unknown OS name: " + osNameProperty);
		}
	}

	/**
	 * Used for getting path to swt that corresponds current architecture
	 * @return Architecture name x86_64 or x86
	 */
	private static String getArchitecture()
	{
		String osArch = System.getProperty("os.arch");

		if (osArch != null && osArch.contains("64"))
		{
			return "x86_64";
		}
		else
		{
			return "x86";
		}
	}
}
