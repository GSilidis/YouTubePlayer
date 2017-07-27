package com.asuscomm.gsilidis.youtubeplayer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Used for loading contents of resource bundle
 */
public class CustomResourceBundleControl extends ResourceBundle.Control
{
	/**
	 * @see java.util.ResourceBundle.Control#newBundle(String, Locale, String, ClassLoader, boolean)
	 * The only difference is encoding being changed from ISO to UTF-8
	 * Used for loading localised strings
	 */
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader classLoader,
	                                boolean reload) throws IllegalAccessException, InstantiationException, IOException
	{
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "properties");
		ResourceBundle bundle = null;
		InputStream stream = null;
		if (reload)
		{
			URL url = classLoader.getResource(resourceName);
			if (url != null)
			{
				URLConnection connection = url.openConnection();
				if (connection != null)
				{
					connection.setUseCaches(false);
					stream = connection.getInputStream();
				}
			}
		} else
		{
			stream = classLoader.getResourceAsStream(resourceName);
		}
		if (stream != null)
		{
			try
			{
				// Only this line is changed to make it to read resource files as UTF-8.
				bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
			} finally
			{
				stream.close();
			}
		}
		return bundle;
	}
}
