package com.asuscomm.gsilidis.youtubeplayer;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Main window that contains menu and JPanel with player and controls
 */
public class MainWindow extends JFrame implements WindowListener, NativeKeyListener
{
	/** JPanel that contains player and its controls */
	private PlayerPanel playerPanel;
	/** JPanel that contains lyrics*/
	private LyricsWindow lyricsWindow;

	public MainWindow(String s)
	{
		super(s);
		GlobalScreen.setEventDispatcher(new SwingDispatchService());

		addWindowListener(this);
		playerPanel = new PlayerPanel();

		// Menu panel
		JMenuBar menuBar;
		menuBar = new JMenuBar();

		JMenu playlistMenu = new JMenu("Playlist");
		playlistMenu.getAccessibleContext().setAccessibleDescription("Playlist management");
		menuBar.add(playlistMenu);
		JMenuItem playlistEditor = new JMenuItem("Playlist editor");
		playlistEditor.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				PlaylistWindow playlistWindow = new PlaylistWindow(MainWindow.this);
				playlistWindow.pack();
				playlistWindow.setVisible(true);

			}
		});
		playlistMenu.add(playlistEditor);


		JMenu extraMenu = new JMenu("Extra");
		extraMenu.getAccessibleContext().setAccessibleDescription("Additional features");
		menuBar.add(extraMenu);
		JMenuItem lyricsMenuItem = new JMenuItem("Lyrics");
		lyricsMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if (lyricsWindow == null)
				{
					lyricsWindow = new LyricsWindow(playerPanel.getVideoTitle(), MainWindow.this);
					lyricsWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					lyricsWindow.pack();
					GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
					Rectangle windowBounds = environment.getMaximumWindowBounds(); // Getting maximum possible size for window
					lyricsWindow.setSize((int) Math.min(windowBounds.getWidth(), lyricsWindow.getWidth()),
							(int) Math.min(windowBounds.getHeight(), lyricsWindow.getHeight())); // Doesn't allow window to get bigger than screen
					lyricsWindow.setLocationByPlatform(true);
					lyricsWindow.setVisible(true);
				}
				else
				{
					lyricsWindow.setNewTitle(playerPanel.getVideoTitle());
					lyricsWindow.setVisible(true);
				}
			}});
		extraMenu.add(lyricsMenuItem);
		setJMenuBar(menuBar);
		getContentPane().add(playerPanel, BorderLayout.CENTER);
	}

	/** Used for creating playlist using id of videos
	 * @param IDs list of videos
	 * @param isMoreThanOneVideo true when playlist contains more tha one video
	 */
	public void setNewPlaylist(String IDs, boolean isMoreThanOneVideo)
	{
		playerPanel.setInPlaylist(isMoreThanOneVideo);
		playerPanel.setPlaylist(IDs);
	}

	/** Parses link and returns ID of video and playlist
	 * @param link URL to video
	 * @return Array, [0] - video ID (null if not present), [1] - playlist ID (null if not present)
	 * Returns null in case of error
	 */
	protected String[] getVideoPlaylistID(String link)
	{
		String[] result = new String[2];
		URL url;
		if (!link.contains("https://") && !link.contains("http://")) link = "http://" + link;
		try
		{
			url = new URL(link);
		} catch (MalformedURLException e)
		{
			JOptionPane.showMessageDialog(null, "URL is not recognised", "Error", JOptionPane.ERROR_MESSAGE);
			return result;
		}

		if (url.getHost().toLowerCase().contains("youtube"))
		{
			String[] params = url.getQuery().split("&");
			Map<String, String> map = new HashMap<String, String>();
			for (String param : params)
			{
				String name = param.split("=")[0];
				String value = param.split("=")[1];
				map.put(name, value);
			}
			if (map.containsKey("list")) // If we got playlist
			{
				if (map.containsKey("v"))
				{
					result[0] =  map.get("v");
				}
				result[1] =  map.get("list");
			} else // If only single video
			{
				result[0] = map.get("v");
			}
		} else
		{
			if (url.getHost().toLowerCase().equals("youtu.be")) // short link
			{
				String query = url.getQuery();
				result[0] = url.getPath().substring(1); // Because it gets path with '/'
				if (query != null) // If playlist
				{
					 result[1] = query.substring(query.indexOf('=') + 1);
				}
			} else
			{
				JOptionPane.showMessageDialog(null, "Resource \'" + url.getHost() + "\' is not supported", "Error", JOptionPane.ERROR_MESSAGE);
				return result;
			}
		}
		return result;
	}

	/**
	 * Reads data from web resource by url
	 * @param urlString URL to resource
	 * @return String with content from resource
	 * @throws IOException In case of connection problems or too big content
	 */
	protected String readFromUrl(String urlString) throws IOException
	{
		BufferedReader reader = null;
		try
		{
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream(),  Charset.forName("UTF-8")));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[4096];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);
			return buffer.toString();
		}
		finally
		{
			if (reader != null)
				reader.close();
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent)
	{
		if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_UNDEFINED) // For some reason this happens under Linux
		{
			switch (nativeKeyEvent.getRawCode())
			{
				case 65300:
					playerPanel.toggleVideo();
					break;
				case 65301:
					playerPanel.stopVideo();
					break;
				case 65302:
					playerPanel.prevVideo();
					break;
				case 65303:
					playerPanel.nextVideo();
					break;
				default:
					break;
			}
		}
		else
		{
			switch (nativeKeyEvent.getKeyCode())
			{
				case NativeKeyEvent.VC_MEDIA_PLAY:
					playerPanel.toggleVideo();
					break;
				case NativeKeyEvent.VC_MEDIA_STOP:
					playerPanel.stopVideo();
					break;
				case NativeKeyEvent.VC_MEDIA_PREVIOUS:
					playerPanel.prevVideo();
					break;
				case NativeKeyEvent.VC_MEDIA_NEXT:
					playerPanel.nextVideo();
					break;
				default:
					break;
			}
		}

	}

	@Override
	public void windowOpened(WindowEvent windowEvent)
	{
		try
		{
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex)
		{
			JOptionPane.showMessageDialog(null, "There was a problem registering native hook\nMedia keys is not supported", "Error", JOptionPane.ERROR_MESSAGE);
		}

		GlobalScreen.addNativeKeyListener(this);
	}

	@Override
	public void windowClosing(WindowEvent windowEvent) {}

	@Override
	public void windowClosed(WindowEvent windowEvent)
	{
		//Clean up the native hook.
		try
		{
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e)
		{
			e.printStackTrace();
		}
		System.runFinalization();
		System.exit(0);
	}

	@Override
	public void windowIconified(WindowEvent windowEvent) {}

	@Override
	public void windowDeiconified(WindowEvent windowEvent) {}

	@Override
	public void windowActivated(WindowEvent windowEvent) {}

	@Override
	public void windowDeactivated(WindowEvent windowEvent) {}
}
