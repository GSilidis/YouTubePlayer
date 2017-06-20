package com.asuscomm.gsilidis.youtubeplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Window that contains lyrics
 */
public class LyricsWindow extends JFrame
{
	/** Text field contains artist name */
	private JTextField artistField;

	/** Text field contains song name */
	private JTextField songField;

	/** Text area contains lyrics */
	private JTextArea textArea;

	public LyricsWindow(String title) throws IOException
	{
		super("Lyrics");
		setMinimumSize(new Dimension(380, 300));

		if (title == null) // if title is not provided - using placeholder
		{
			title = "Artist - Song";
		}

		JPanel container = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JPanel navPanel = new JPanel();
		artistField = new JTextField(title.substring(0, title.indexOf("-")).trim()); // Assuming that title looks like ARTIST - SONG
		artistField.setColumns(15);
		navPanel.add(artistField);
		JButton swap = new JButton("<->");
		swap.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				String buff;
				buff = artistField.getText();
				artistField.setText(songField.getText());
				songField.setText(buff);
			}
		});
		navPanel.add(swap);
		songField = new JTextField(title.substring(title.indexOf("-")+1).trim());
		songField.setColumns(15);
		navPanel.add(songField);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.weightx = 1;
		c.weighty = 0;
		container.add(navPanel, c);

		JButton search = new JButton("Search for lyrics");
		search.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				textArea.setText(findLyrics());
			}
		});
		search.setAlignmentX(Component.CENTER_ALIGNMENT);
		c = new GridBagConstraints();
		c.gridy = 1;
		c.weighty = 0;
		c.weightx = 1;
		container.add(search, c);

		textArea = new JTextArea(findLyrics());
		textArea.setEditable(false);
		textArea.setFont(textArea.getFont().deriveFont(12f)); // Setting font size to 12
		c = new GridBagConstraints();
		c.gridy = 2;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(7, 7, 7, 7);
		final JScrollPane textPane = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		container.add(textPane, c);

		add(container);
	}

	/**
	 * Searching for lyrics
	 * @return String contains lyrics or error description
	 */
	private String findLyrics()
	{
		String lyrics = null;
		try
		{
			lyrics = readFromUrl("http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect?artist=" +
					URLEncoder.encode(artistField.getText(), "UTF-8") +
					"&song=" + URLEncoder.encode(songField.getText(), "UTF-8"));
			String src = lyrics.substring(lyrics.indexOf("<LyricUrl>")+10, lyrics.indexOf("</LyricUrl>"));
			lyrics  = lyrics.substring(lyrics.indexOf("<Lyric>")+7, lyrics.indexOf("</Lyric>")) +
					  "\n\nSource: \n" + src;
		}
		catch (StringIndexOutOfBoundsException e) // If lyrics not found, API returns malformed XML
		{                                         // so method .substring(lyrics.indexOf("<Lyric>")) throws this exception
			lyrics = "Lyrics not found";
		}
		catch (IOException e)                     // This can be caused by connection problems
		{
			lyrics = "Error: unable to load lyrics.\n" + e.toString();
		}
		return lyrics;
	}


	/**
	 * Reads data from web resource by url
	 * @param urlString URL to resource
	 * @return String with content from resource
	 * @throws IOException In case of connection problems or too big content
	 */
	private static String readFromUrl(String urlString) throws IOException
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
}
