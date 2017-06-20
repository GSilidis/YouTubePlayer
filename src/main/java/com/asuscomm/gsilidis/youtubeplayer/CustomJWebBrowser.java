package com.asuscomm.gsilidis.youtubeplayer;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Web browser component used for playback
 */
public class CustomJWebBrowser extends JWebBrowser
{
	/** Web server (used only under windows) */
	private WebServer server;

	public CustomJWebBrowser()
	{
		super();
		if (System.getProperty("os.name").toLowerCase().contains("win"))
		{
			server = new WebServer();
		}
	}


	/**
	 * Under *nix - acts like setHTMLContent from JWebBrowser,
	 * Under windows - sets HTML for local web server and navigates JWebBrowser component to it
	 * @param html html content
	 * @return true if successful
	 */
	@Override
	public boolean setHTMLContent(String html)
	{
		if (server == null)
			return super.setHTMLContent(html);
		else
		{
			server.getHandler().setPage(html);
			return super.navigate("http://localhost:8080");
		}
	}

	/**
	 * Parses link and sets up web page for playback
	 * @param urlToVideo URL to youtube video or playlist
	 * @return 0 if urlToVideo points to single video, 1 - to playlist and -1 in case of error
	 */
	public int setVideoLink(String urlToVideo)
	{
		URL link = null;
		String videoHtml = "";
		String url = urlToVideo;
		int type;
		if (!url.contains("https://") && !url.contains("http://"))
			url = "http://" + url;
		try
		{
			link = new URL(url);
		} catch (MalformedURLException e)
		{
			JOptionPane.showMessageDialog(null, "URL is not recognised",
					"Error", JOptionPane.ERROR_MESSAGE);
			return -1;
		}

		if (link.getHost().toLowerCase().contains("youtube"))
		{
			String[] params = link.getQuery().split("&");
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
					videoHtml = "videoId: '" + map.get("v") + "',\n";
				}
				videoHtml += "playerVars:\n{\nlistType:'playlist',\nlist:'" + map.get("list") + "'\n}";
				type = 1;
			}
			else // If only single video
			{
				videoHtml = "videoId: '" + map.get("v") + "'";
				type = 0;
			}
		}
		else
		{
			if (link.getHost().toLowerCase().equals("youtu.be")) // short link
			{
				String query = link.getQuery();
				videoHtml = "videoId: '" + link.getPath().substring(1) + "',\n"; // Because it gets path with '/'
				if (query != null) // If playlist
				{
					videoHtml += "playerVars:\n{\nlistType:'playlist',\nlist:'" + query.substring(query.indexOf('=')+1) + "'\n}";
					type = 1;
				}
				else // If single video
				{
					type = 0;
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Resource \'" + link.getHost() +  "\' is not supported",
						"Error", JOptionPane.ERROR_MESSAGE);
				return -1;
			}
		}
		setHTMLContent(
				"<!DOCTYPE html>\r\n" +
						"<!-- saved from url=(0014)about:internet -->\r\n" +
						"<html><head> \n" +
						"   <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
						"</head>\n" +
						"<body style='background-color: #000;'>\n" +
						"   <div id=\"player\"></div>\n" +
						"   <script>\n" +
						"       var tag = document.createElement('script');\n" +
						"       tag.src = \"https://www.youtube.com/iframe_api\";\n" +
						"       var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
						"       firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
						"       var player;\n" +
						"       function onYouTubeIframeAPIReady()\n" +
						"       {\n" +
						"          player = new YT.Player('player', \n" +
						"          {\n" +
						"              height: '360',\n" +
						"              width: '640'," + videoHtml + "\n" +
						"              ,\n" +
						"              events: \n" +
						"              {\n" +
						"                  'onReady': onPlayerReady,\n" +
						"                  'onStateChange': onPlayerStateChange\n" +
						"              }\n" +
						"          });\n" +
						"       }\n" +
						"       function onPlayerReady(event)" +
						"       {\n" +
						"           event.target.playVideo();\n" +
						"       }\n" +
						"       var done = false;\n" +
						"       function onPlayerStateChange(event)\n" +
						"       {\n" +
						"           sendNSCommand(player.getVideoData().title);\n" +
						"           if (event.data == YT.PlayerState.PLAYING && !done) \n" +
						"               done = true;\n" +
						"       }\n" +
						"       function toggleVideo()\n" +
						"       {\n" +
						"           if(player.getPlayerState() != 1)" +
						"           {\n" +
						"              player.playVideo();\n" +
						"           }else{\n" +
						"               player.pauseVideo()\n" +
						"           }\n" +
						"       }\n" +
						"       function pauseVideo()\n" +
						"       {\n" +
						"           player.pauseVideo();\n" +
						"       }\n" +
						"       function prevVideo()\n" +
						"       {\n" +
						"           player.previousVideo();\n" +
						"       }\n" +
						"   </script>\n" +
						"</body> </html>");
		return type;
	}
}
