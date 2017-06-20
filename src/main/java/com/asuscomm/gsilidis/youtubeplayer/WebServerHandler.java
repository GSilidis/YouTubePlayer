package com.asuscomm.gsilidis.youtubeplayer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
/**
 * This class is used for sending responses to clients (used only under windows)
 */
public class WebServerHandler implements HttpHandler
{
	/** HTML for web page */
	private String webPage;

	public WebServerHandler()
	{
		super();
		webPage = "...";
	}

	/**
	 * Sets html that will be shown to clients
	 * @param html html to show
	 */
	public void setPage(String html)
	{
		webPage = html;
	}

	/**
	 * Responsible for showing html content to client
	 * @param exchange request from the client
	 * @throws IOException if exchange is null
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		String requestMethod = exchange.getRequestMethod();
		if (requestMethod.equalsIgnoreCase("GET"))
		{
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.set("Content-Type", "text/html");
			exchange.sendResponseHeaders(200, 0);
			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write(webPage.getBytes());
			responseBody.close();
		}
	}
}
