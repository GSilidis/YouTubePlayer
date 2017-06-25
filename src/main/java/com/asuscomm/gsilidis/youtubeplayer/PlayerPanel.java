package com.asuscomm.gsilidis.youtubeplayer;

import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserCommandEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
		JPanel inputPanel = new JPanel();
		JLabel labelIn = new JLabel("Video URL:");
		final JTextField idIn = new JTextField("");
		idIn.setColumns(30);
		Action navigateToAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				switch(webBrowser.setVideoLink(idIn.getText()))
				{
					case 0:
						inPlaylist = false;
						break;
					case 1:
						inPlaylist = true;
						break;
					case -1:
					default:
						break;
				}
			}
		};
		idIn.addActionListener(navigateToAction);
		JButton inButton = new JButton("GO");
		inButton.addActionListener(navigateToAction);
		inputPanel.add(labelIn);
		inputPanel.add(idIn);
		inputPanel.add(inButton);
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
		JButton prevButton = new JButton("⏮");
		prevButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				prevVideo();
			}
		});
		JButton startButton = new JButton("⏯");
		startButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				toggleVideo();
			}
		});
		JButton stopButton = new JButton("■");
		stopButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				stopVideo();
			}
		});
		JButton nextButton = new JButton("⏭");
		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				nextVideo();
			}
		});
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

	/**
	 * This function is called by JS from JWebBrowser component, when state of YouTube player changes
	 * @param newTitle title of currently played video
	 */
	public void setVideoTitle(String newTitle)
	{
		videoTitle = newTitle;
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
}
