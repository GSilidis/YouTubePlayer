package com.asuscomm.gsilidis.youtubeplayer;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import com.sun.istack.internal.Nullable;

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
	 * Sets content of video player
	 * @param videoPlaylist ID of video or playlist; null creates empty player
	 * @param onPlayerReadyFunction code to execute on player load; null for default
	 */
	public void setVideo(@Nullable String[] videoPlaylist, @Nullable String onPlayerReadyFunction)
	{
		StringBuilder videoHtml = new StringBuilder("");
		String videoList = "";
		if (videoPlaylist != null)
		{
			if (videoPlaylist[0] != null) videoHtml.append(" videoId: '" + videoPlaylist[0] + "',");
			if (videoPlaylist[1] != null) videoHtml.append(" playerVars: { listType:'playlist', list:'" + videoPlaylist[1] + "' },");
		}
		if (onPlayerReadyFunction == null)
		{
			onPlayerReadyFunction = "event.target.playVideo();"; // If not provided - setting default behaviour
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
						"              events: \n" +
						"              {\n" +
						"                  'onReady': onPlayerReady,\n" +
						"                  'onStateChange': onPlayerStateChange\n" +
						"              }\n" +
						"          });\n" +
						"       }\n" +
						"       function onPlayerReady(event)\n" +
						"       {\n" +
						"           " + onPlayerReadyFunction + "\n" +
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
						"           if(player.getPlayerState() != 1)\n" +
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
	}
}
