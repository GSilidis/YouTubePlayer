package com.asuscomm.gsilidis.youtubeplayer;

import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * JPanel that contains player and its controls
 */
public class PlayerPanel extends JPanel
{
	/** Component used for video playback */
	private final CustomJWebBrowser webBrowser;

	/** True if playing playlist, false otherwise */
	private boolean inPlaylist;

	/** Title of currently playing video */
	private String videoTitle;

	public PlayerPanel()
	{
		super(new BorderLayout());

		// Navigation panel
		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JLabel labelIn = new JLabel("Video URL:");
		final JTextField idIn = new JTextField("");
		//idIn.setColumns(30);
		Action navigateToAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				MainWindow parent = (MainWindow) SwingUtilities.getWindowAncestor(PlayerPanel.this);
				String[] videoPlaylist = parent.getVideoPlaylistID(idIn.getText());
				if (videoPlaylist[1] == null)
				{
					if (videoPlaylist[0] == null)
					{
						return; // URL cannot be parsed
					}
				}
				else
				{
					inPlaylist = true;
				}
				webBrowser.setVideo(videoPlaylist, null);

			}
		};
		idIn.addActionListener(navigateToAction);
		JButton inButton = new JButton("GO");
		inButton.setToolTipText("Play video or playlist");
		inButton.addActionListener(navigateToAction);
		c.insets = new Insets(0, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		inputPanel.add(labelIn, c);
		c.weightx = 1;
		inputPanel.add(idIn, c);
		c.weightx = 0;
		inputPanel.add(inButton, c);
		inputPanel.setBorder(BorderFactory.createTitledBorder("Navigator"));
		add(inputPanel, BorderLayout.NORTH);

		// Player
		JPanel webBrowserPanel = new JPanel(new BorderLayout());
		webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Player"));
		webBrowser = new CustomJWebBrowser();
		webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
		webBrowser.setBarsVisible(false);
		webBrowser.setPreferredSize(new Dimension(655, 380));
		webBrowser.setMaximumSize(webBrowser.getPreferredSize());
		webBrowser.addWebBrowserListener(new WebBrowserAdapter()
		{
			@Override
			public void commandReceived(WebBrowserCommandEvent e)
			{
				videoTitle = e.getCommand();
			}
		});
		add(webBrowserPanel, BorderLayout.CENTER);


		// Playback controls
		JPanel buttonPanel = new JPanel(new FlowLayout());
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("fonts/Symbola.ttf");
		Font unicodeFont = null;
		try
		{
			unicodeFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
			unicodeFont = unicodeFont.deriveFont(Font.PLAIN, 15f);
		} catch (FontFormatException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		JButton prevButton = new JButton("⏮");
		prevButton.setToolTipText("Previous song");
		prevButton.setFont(unicodeFont);
		prevButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				prevVideo();
			}
		});
		JButton startButton = new JButton("⏯");
		startButton.setToolTipText("Play/pause");
		startButton.setFont(unicodeFont);
		startButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				toggleVideo();
			}
		});
		JButton stopButton = new JButton("■");
		stopButton.setToolTipText("Stop");
		stopButton.setFont(unicodeFont);
		stopButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				stopVideo();
			}
		});
		JButton nextButton = new JButton("⏭");
		nextButton.setToolTipText("Next song");
		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				nextVideo();
			}
		});
		nextButton.setFont(unicodeFont);
		buttonPanel.add(prevButton);
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(nextButton);
		buttonPanel.setBorder(BorderFactory.createTitledBorder("Playback controls"));
		add(buttonPanel, BorderLayout.SOUTH);
	}


	/**
	 * Toggles state of player. If video is playing - pauses it and vice versa.
	 */
	public void toggleVideo()
	{
		webBrowser.executeJavascript("toggleVideo();");
	}

	/**
	 * Stops video
	 */
	public void stopVideo()
	{
		webBrowser.executeJavascript("player.stopVideo();");
	}

	/**
	 * Switching to next video (works only with playlist)
	 */
	public void nextVideo()
	{
		webBrowser.executeJavascript("player.nextVideo();");
	}

	/**
	 * In playlist - switches to previous video
	 * In single video - jumps to beginning of video
	 */
	public void prevVideo()
	{
		if (inPlaylist)
			webBrowser.executeJavascript("prevVideo();");
		else
		{
			webBrowser.executeJavascript("player.seekTo(0, true);");
		}
	}

	public void setInPlaylist(boolean inPlaylist)
	{
		this.inPlaylist = true;
	}


	/**
	 * @return title of current video
	 */
	public String getVideoTitle()
	{
		if (videoTitle != null)
			return videoTitle;
		else
			return "Artist - Song";
	}

	public void setPlaylist(String IDs)
	{
		webBrowser.setVideo(null, "player.loadPlaylist([ " + IDs + " ]);");
	}
}
