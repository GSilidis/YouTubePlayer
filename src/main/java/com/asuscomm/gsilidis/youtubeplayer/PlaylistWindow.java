package com.asuscomm.gsilidis.youtubeplayer;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Playlist editor's window
 */
public class PlaylistWindow extends JFrame
{
	/** Model that contains playlist data */
	private PlaylistListModel listModel;
	/** Pointer to calling window */
	private MainWindow parent;
	/** Used for picking up saving files */
	final private JFileChooser fileChooser;

	public PlaylistWindow(final MainWindow parent)
	{
		super(parent.getStringFromBundle("playlistEditor"));
		this.parent = parent;
		setMinimumSize(new Dimension(350, 250));
		fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				parent.getStringFromBundle("dialogs.playlistFileDesc") + " (*.ypl)", "ypl");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		JPanel container = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		listModel = new PlaylistListModel();
		final JList list = new JList(listModel);
		c.gridy = 0;
		c.gridx = 0;
		c.weighty = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.insets = new Insets(3, 3, 0, 1);
		final JScrollPane listPane = new JScrollPane(list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		container.add(listPane, c);

		JPanel actionsContainer = new JPanel(new GridLayout(4, 0));
		JButton addVideo = new JButton("+");
		addVideo.setToolTipText(parent.getStringFromBundle("playlistEditor.addButton"));
		addVideo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				final String userInput = (String) JOptionPane.showInputDialog(PlaylistWindow.this,
						PlaylistWindow.this.parent.getStringFromBundle("dialogs.inputVideo"),
						PlaylistWindow.this.parent.getStringFromBundle("dialogs.inputVideo.title"),
						JOptionPane.PLAIN_MESSAGE, null, null, null);
				if (userInput != null && !userInput.equals(""))
				{
					final String[] id = PlaylistWindow.this.parent.getVideoPlaylistID(userInput);
					if (id[0] == null && id[1] == null)
						return;
					final JOptionPane optionPane = new JOptionPane(
							PlaylistWindow.this.parent.getStringFromBundle("dialogs.fetching"),
							JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
					final JDialog dialog = new JDialog();
					dialog.setTitle(PlaylistWindow.this.parent.getStringFromBundle("dialogs.pleaseWait"));
					dialog.setModal(true);
					dialog.setContentPane(optionPane);
					dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					dialog.pack();
					dialog.setLocationRelativeTo(PlaylistWindow.this);
					// Fetching title might take some time, so we'll need to show user loading window
					SwingWorker<String, Void> worker = new SwingWorker<String, Void>()
					{
						@Override
						protected String doInBackground() throws InterruptedException, IOException
						{
							if (id[1] == null)
								return PlaylistWindow.this.parent.readFromUrl("https://www.youtube.com/watch?v="
										+ id[0]);
							else
								return PlaylistWindow.this.parent.readFromUrl("https://www.youtube.com/playlist?list="
										+ id[1]);
						}

						@Override
						protected void done()
						{
							dialog.dispose();
						}
					};

					worker.execute();
					dialog.setVisible(true);
					try
					{
						String html=worker.get();
						if (id[1] == null) // if fetching single video
						{
							html = html.substring(html.indexOf("<title>") + 7, html.indexOf(" - YouTube</title>"));
							listModel.addElement(StringEscapeUtils.unescapeHtml4(html), id[0]);
						}
						else // if its playlist
						{
							String title;
							String videoId;
							String curString;
							java.util.List<String> allMatches = new ArrayList<String>();
							// What kind of page is it?
							if (html.contains("<tbody id=\"pl-load-more-destination\">")) // Old style page
							{
								html = html.substring(html.indexOf("<tbody id=\"pl-load-more-destination\">") + 37);
								html = html.substring(0, html.indexOf("</tbody>"));

								Matcher matcher = Pattern.compile("<tr class=\"pl-video yt-uix-tile \".*?>",
										Pattern.DOTALL | Pattern.MULTILINE).matcher(html);

								while (matcher.find())
								{
									allMatches.add(matcher.group());
								}

								for (String match : allMatches)
								{
									curString = match;
									//curString = curString.substring(curString.indexOf("data-title") + 12);
									title = curString.substring(curString.indexOf("data-title") + 12);
									title = title.substring(0, title.indexOf('"'));
									title = StringEscapeUtils.unescapeHtml4(title);
									videoId = curString.substring(curString.indexOf("data-video-id") + 15);
									videoId = videoId.substring(0, videoId.indexOf('"'));
									listModel.addElement(title, videoId);
								}
							}
							else
							{
								if (html.contains("\"playlistVideoListRenderer\": {")) // New style page
								{
									html = html.substring(html.indexOf("\"playlistVideoListRenderer\": {")+30);

									Matcher matcher=Pattern.compile("\"playlistVideoRenderer\": \\{.*?simpleText.*?\\}",
											Pattern.DOTALL | Pattern.MULTILINE).matcher(html);

									while (matcher.find())
									{
										allMatches.add(matcher.group());
									}

									for (String match : allMatches)
									{
										curString = match;
										title = curString.substring(curString.indexOf("simpleText") + 14);
										title = title.substring(0, title.indexOf('"'));
										title = StringEscapeUtils.unescapeHtml4(title);
										videoId = curString.substring(curString.indexOf("videoId") + 11);
										videoId = videoId.substring(0, videoId.indexOf('"'));
										listModel.addElement(title, videoId);
									}
								}
								else // Something unknown
								{
									JOptionPane.showMessageDialog(null,
											PlaylistWindow.this.parent.getStringFromBundle("dialogs.fetchingFailedPlaylist"),
											PlaylistWindow.this.parent.getStringFromBundle("dialogs.error"),
											JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					} catch (IndexOutOfBoundsException e)
					{
						JOptionPane.showMessageDialog(null,
								PlaylistWindow.this.parent.getStringFromBundle("dialogs.fetchingFailedVideo"),
								PlaylistWindow.this.parent.getStringFromBundle("dialogs.error"),
								JOptionPane.ERROR_MESSAGE);
					} catch (InterruptedException e)
					{
						JOptionPane.showMessageDialog(null,
								PlaylistWindow.this.parent.getStringFromBundle("dialogs.interrupted"),
								PlaylistWindow.this.parent.getStringFromBundle("dialogs.error"),
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					} catch (ExecutionException e)
					{
						JOptionPane.showMessageDialog(null,
								PlaylistWindow.this.parent.getStringFromBundle("dialogs.fetchingFailedVideo") + "\n" +
										e.toString(),
								PlaylistWindow.this.parent.getStringFromBundle("dialogs.error"),
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			}
		});
		actionsContainer.add(addVideo);
		JButton upButton = new JButton("↑");
		upButton.setToolTipText(parent.getStringFromBundle("playlistEditor.upButton"));
		upButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				int [] indices = list.getSelectedIndices();
				for (int i = 0; i < indices.length; i++)
				{
					if (!listModel.moveUp(indices[i]--))
						return;
				}
				list.setSelectedIndices(indices);
			}
		});
		actionsContainer.add(upButton);
		JButton downButton = new JButton("↓");
		downButton.setToolTipText(parent.getStringFromBundle("playlistEditor.downButton"));
		downButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				int [] indices = list.getSelectedIndices();
				for (int i = indices.length-1; i >= 0; i--)
				{
					if (!listModel.moveDown(indices[i]++))
						return;
				}
				list.setSelectedIndices(indices);
			}
		});
		actionsContainer.add(downButton);
		JButton removeVideo = new JButton("-");
		removeVideo.setToolTipText(parent.getStringFromBundle("playlistEditor.removeButton"));
		removeVideo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				int [] indices = list.getSelectedIndices();
				for (int i = indices.length-1; i >= 0; i--)
					listModel.removeItem(indices[i]);
				list.clearSelection();
			}
		});
		actionsContainer.add(removeVideo);
		c.weightx = 0;
		c.weighty = 1;
		c.gridy = 0;
		c.gridx = 1;
		c.insets = new Insets(10, 1, 8, 2);
		container.add(actionsContainer, c);

		JPanel optionsContainer = new JPanel();
		JButton newButton = new JButton(parent.getStringFromBundle("playlistEditor.new"));
		newButton.setToolTipText(parent.getStringFromBundle("playlistEditor.new.toolTip"));
		newButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				listModel.clear();
			}
		});
		optionsContainer.add(newButton);

		JButton loadButton = new JButton(parent.getStringFromBundle("playlistEditor.load"));
		loadButton.setToolTipText(parent.getStringFromBundle("playlistEditor.load.toolTip"));
		loadButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				int returnVal = fileChooser.showOpenDialog(PlaylistWindow.this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();
					try
					{
						BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
						String line;
						listModel.clear();
						try
						{
							while ((line = bufferedReader.readLine()) != null)
							{
								listModel.addElement(line.substring(line.indexOf('|')+1), line.substring(0, line.indexOf('|')));
							}
						} catch (IOException e)
						{
							JOptionPane.showMessageDialog(null,
									PlaylistWindow.this.parent.getStringFromBundle("dialogs.readError") + "\n" +
											e.toString(),
									PlaylistWindow.this.parent.getStringFromBundle("dialogs.error"),
									JOptionPane.ERROR_MESSAGE);
						}
					} catch (IOException e)
					{
						JOptionPane.showMessageDialog(null,
								PlaylistWindow.this.parent.getStringFromBundle("dialogs.readError") + "\n" +
										e.toString(),
								PlaylistWindow.this.parent.getStringFromBundle("dialogs.error"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		optionsContainer.add(loadButton);
		JButton setPlaylist = new JButton(parent.getStringFromBundle("playlistEditor.play"));
		setPlaylist.setToolTipText(parent.getStringFromBundle("playlistEditor.play.toolTip"));
		setPlaylist.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				PlaylistWindow.this.parent.setNewPlaylist(listModel.getAllIds(), (listModel.getSize()>1));
			}
		});
		optionsContainer.add(setPlaylist);
		JButton saveButton = new JButton(parent.getStringFromBundle("playlistEditor.save"));
		saveButton.setToolTipText(parent.getStringFromBundle("playlistEditor.save.toolTip"));
		saveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if (listModel.getSize() > 0)
				{
					int returnVal = fileChooser.showSaveDialog(PlaylistWindow.this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						PrintWriter writer = null;
						String saveFilePath = fileChooser.getSelectedFile().getAbsolutePath();
						File saveFile;
						if (saveFilePath.contains(".") &&
								saveFilePath.substring(saveFilePath.lastIndexOf('.')).equals(".ypl"))
							saveFile = new File(saveFilePath);
						else
							saveFile = new File(saveFilePath + ".ypl");
						try
						{
							writer = new PrintWriter(saveFile, "UTF-8");
						} catch (IOException e)
						{
							JOptionPane.showMessageDialog(null,
									PlaylistWindow.this.parent.getStringFromBundle("dialogs.writeError") + "\n" +
											e.toString(),
									PlaylistWindow.this.parent.getStringFromBundle("dialogs.error"),
									JOptionPane.ERROR_MESSAGE);
						}
						if (writer != null)
						{
							for (int i = 0; i < listModel.getSize(); i++)
							{
								writer.println(listModel.getIDAt(i) + "|" + listModel.getElementAt(i));
							}
							writer.close();
						}
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null,
							PlaylistWindow.this.parent.getStringFromBundle("dialogs.nothingToSave"),
							PlaylistWindow.this.parent.getStringFromBundle("dialogs.error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		optionsContainer.add(saveButton);

		c.gridy = 1;
		c.gridx = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 2;
		c.insets = new Insets(0, 0, 0, 0);
		container.add(optionsContainer, c);
		add(container);
	}
}
