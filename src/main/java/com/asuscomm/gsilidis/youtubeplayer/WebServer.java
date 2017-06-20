package com.asuscomm.gsilidis.youtubeplayer;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Web server
 */

// Internet Explorer requires web server for executing external javascript files
// So this class is required only under windows
public class WebServer
{
	/** Handles responses for clients */
	private WebServerHandler handler;

	public WebServer()
	{
		HttpServer server = null;
		try
		{
			server = HttpServer.create(new InetSocketAddress(8080), 10);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		handler = new WebServerHandler();
		server.createContext("/", handler);
		server.start();
		System.out.println("Launching http server on port 8080");
	}

	public WebServerHandler getHandler()
	{
		return handler;
	}

}
