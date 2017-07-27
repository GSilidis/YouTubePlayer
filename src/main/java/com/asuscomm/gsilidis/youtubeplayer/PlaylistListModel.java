package com.asuscomm.gsilidis.youtubeplayer;

import javax.swing.*;
import javax.swing.text.html.ObjectView;
import java.util.Observable;
import java.util.Vector;

/**
 * Model used for storing and displaying playlist data
 */
public class PlaylistListModel extends AbstractListModel
{
	/**	List of videos that will be displayed to user */
	private Vector<String> titles;
	/** List of videos' ids that will be send to player */
	private Vector<String> ids;

	public PlaylistListModel()
	{
		super();
		titles = new Vector<String>();
		ids = new Vector<String>();
	}


	/**
	 * Adds video to playlist
	 * @param title title of the video
	 * @param id id of the video
	 */
	public void addElement(String title, String id)
	{
		titles.add(title);
		ids.add(id);
		fireIntervalAdded(this, 0, titles.size());
	}

	/**
	 * Swapping positions of current video and previous one
	 * @param i position of video
	 * @return true if item was moved
	 */
	public boolean moveUp(int i)
	{
		if (i > 0)
		{
			String buffer;
			buffer = titles.get(i-1);
			titles.set(i-1, titles.get(i));
			titles.set(i, buffer);
			buffer = ids.get(i-1);
			ids.set(i-1, ids.get(i));
			ids.set(i, buffer);
			fireIntervalAdded(this, i-1, i);
			return true;
		}
		return false;
	}

	/**
	 * Swapping positions of current video and next one
	 * @param i position of video
	 * @return true if item was moved
	 */
	public boolean moveDown(int i)
	{
		if (i != -1 && i < titles.size()-1)
		{
			String buffer;
			buffer = titles.get(i+1);
			titles.set(i+1, titles.get(i));
			titles.set(i, buffer);
			buffer = ids.get(i+1);
			ids.set(i+1, ids.get(i));
			ids.set(i, buffer);
			fireIntervalAdded(this, i, i+1);
			return true;
		}
		return false;
	}

	/**
	 * Removes video from playlist
	 * @param i position of video
	 */
	public void removeItem(int i)
	{
		if (i != -1)
		{
			titles.remove(i);
			ids.remove(i);
			fireIntervalAdded(this, i, i);
		}
	}

	/**
	 * Removes all elements from list
	 */
	public void clear()
	{
		int size = titles.size();
		titles.clear();
		ids.clear();
		fireIntervalRemoved(this, 0, size);
	}

	/**
	 * Used for sending string that contains all videos' ids
	 * @return List of all ids, divided with ',' symbol
	 */
	public String getAllIds()
	{
		StringBuilder result = new StringBuilder();
		int i;
		for (i = 0; i < ids.size()-1; i++)
		{
			result.append('\'' + ids.get(i) + "', ");
		}
		result.append('\'' + ids.get(i) + '\'');
		return result.toString();
	}

	@Override
	public int getSize()
	{
		return titles.size();
	}

	@Override
	public Object getElementAt(int i)
	{
		return titles.get(i);
	}

	public Object getIDAt(int i)
	{
		return ids.get(i);
	}
}
