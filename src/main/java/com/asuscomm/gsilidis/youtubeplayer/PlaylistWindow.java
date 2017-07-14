package com.asuscomm.gsilidis.youtubeplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Playlist editor's window
 */
public class PlaylistWindow extends JFrame
{
	/** Model that contains playlist data */
	private PlaylistListModel listModel;
	/** Pointer to calling window */
	private MainWindow parent;

	public PlaylistWindow(final MainWindow parent)
	{
		super("Playlist editor");
		this.parent = parent;
		setMinimumSize(new Dimension(200, 250));

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
		container.add(list, c);

		JPanel actionsContainer = new JPanel();
		actionsContainer.setLayout(new GridLayout(4, 0));
		JButton addVideo = new JButton("+");
		addVideo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				final String userInput = (String) JOptionPane.showInputDialog(PlaylistWindow.this,
						"Enter video URL", "Add video",	JOptionPane.PLAIN_MESSAGE, null, null, null);
				if (userInput != null && !userInput.equals(""))
				{
					final String[] id = PlaylistWindow.this.parent.getVideoPlaylistID(userInput);

					final JOptionPane optionPane = new JOptionPane("Please wait", JOptionPane.INFORMATION_MESSAGE,
							JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
					final JDialog dialog = new JDialog();
					dialog.setTitle("Fetching video information");
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

							return PlaylistWindow.this.parent.readFromUrl("https://www.youtube.com/watch?v=" + id[0]);
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
						String title=worker.get();
						title = title.substring(title.indexOf("<title>") + 7, title.indexOf(" - YouTube</title>"));
						listModel.addElement(title, id[0]);
					} catch (IndexOutOfBoundsException e)
					{
						JOptionPane.showMessageDialog(null, "Unable to fetch video information",
								"Error", JOptionPane.ERROR_MESSAGE);
					} catch (InterruptedException e)
					{
						JOptionPane.showMessageDialog(null, "Operation was interrupted",
								"Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					} catch (ExecutionException e)
					{
						JOptionPane.showMessageDialog(null, "Unable to fetch video information:\n" +
										e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			}
		});
		actionsContainer.add(addVideo);
		JButton upButton = new JButton("↑");
		upButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				listModel.moveUp(list.getSelectedIndex());
				list.clearSelection();
			}
		});
		actionsContainer.add(upButton);
		JButton downButton = new JButton("↓");
		downButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				listModel.moveDown(list.getSelectedIndex());
				list.clearSelection();
			}
		});
		actionsContainer.add(downButton);
		JButton removeVideo = new JButton("-");
		removeVideo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				listModel.removeItem(list.getSelectedIndex());
				list.clearSelection();
			}
		});
		actionsContainer.add(removeVideo);
		c.weightx = 0;
		c.weighty = 1;
		c.gridy = 0;
		c.gridx = 1;
		c.insets = new Insets(10, 1, 8, 0);
		container.add(actionsContainer, c);

		JPanel optionsContainer = new JPanel();
		JButton setPlaylist = new JButton("Set playlist");
		setPlaylist.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				PlaylistWindow.this.parent.setNewPlaylist(listModel.getAllIds(), (listModel.getSize()>1));
			}
		});
		optionsContainer.add(setPlaylist);
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
