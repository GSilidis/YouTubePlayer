package com.asuscomm.gsilidis.youtubeplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URLEncoder;

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

	/** Pointer to calling window */
	private MainWindow parent;

	public LyricsWindow(String title, MainWindow parent)
	{
		super("Lyrics");
		this.parent = parent;
		setMinimumSize(new Dimension(420, 300));

		JPanel container = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JPanel navPanel = new JPanel();
		String[] parsedTitle = getArtistAndSong(title);
		artistField = new JTextField(parsedTitle[0]);
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
		songField = new JTextField(parsedTitle[1]);
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
	 * Used for recycling current window and for showing lyrics for new song
	 * @param title title of the new video
	 */
	public void setNewTitle(String title)
	{
		String[] parsedTitle = getArtistAndSong(title);
		artistField.setText(parsedTitle[0]);
		songField.setText(parsedTitle[1]);
		textArea.setText(findLyrics());
	}

	/**
	 * Parses video's title and searching for artist and song title
	 * @param title Video title
	 * @return Array: [0] - Artist name; [1] - Song title. Returns placeholder if not found anything
	 */
	private String[] getArtistAndSong(String title)
	{
		String[] results = new String[2];
		if (title.toLowerCase().contains(" by ")) // Song by Artist
		{
			results[0] = title.substring(title.toLowerCase().indexOf(" by ") + 4).trim();
			results[1] = title.substring(0, title.toLowerCase().indexOf(" by ")).trim();
		}
		else if (title.contains("-")) // Artist - Song
		{
			results[0] = title.substring(0, title.indexOf("-")).trim();
			results[1] = title.substring(title.indexOf("-") + 1).trim();
		}
		else if (title.contains("\"")) // Artist "Song"
		{
			results[0] = title.substring(0, title.indexOf("\"")).trim();
			results[1] = title.substring(title.indexOf("\"")).trim();
		}
		else
		{
			results[0] = "Artist";
			results[1] = "Song";
		}
		return results;
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
			lyrics = parent.readFromUrl("http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect?artist=" +
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


}
