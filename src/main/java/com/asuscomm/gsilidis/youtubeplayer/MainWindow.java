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
import java.io.IOException;

/**
 * Main window that contains menu and JPanel with player and controls
 */
public class MainWindow extends JFrame implements WindowListener, NativeKeyListener
{
	/** JPanel that contains player and its controls */
	private PlayerPanel playerPanel;
	private LyricsWindow lyricsWindow;

	public MainWindow(String s)
	{
		super(s);
		GlobalScreen.setEventDispatcher(new SwingDispatchService());

		addWindowListener(this);
		playerPanel = new PlayerPanel();

		// Menu panel
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;
		menuBar = new JMenuBar();
		menu = new JMenu("Extra");
		menu.getAccessibleContext().setAccessibleDescription("Additional features");
		menuBar.add(menu);
		menuItem = new JMenuItem("Lyrics");
		menuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if (lyricsWindow == null)
				{
					lyricsWindow = new LyricsWindow(playerPanel.getVideoTitle());
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
		menu.add(menuItem);
		setJMenuBar(menuBar);

		getContentPane().add(playerPanel, BorderLayout.CENTER);
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
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
			ex.printStackTrace();

			System.exit(1);
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
