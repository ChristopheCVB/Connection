package com.ccvb.utils.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.ccvb.utils.javaxmlquery.XML_Element;
import com.ccvb.utils.javaxmlquery.XML_Parser;
import com.ccvb.utils.log.Log;

/**
 * Low level Connector for HTTP requests
 */
public class Connector
{
	/**
	 * Perform a {@link Request} and return the {@link Response} (null if there is no baseUrl)
	 * 
	 * @param request - Request
	 * @param baseUrl - String url to address
	 * @return Response
	 * @throws Exception
	 */
	public static Response performRequest(Request request, String baseUrl) throws Exception
	{
		if (TextUtils.isEmpty(baseUrl))
		{
			throw new Exception("Base URL is Null");
		}
		
		String toBeCalled = baseUrl.replaceFirst("^https?", request.protocol.name());
		String urlParams = Connector.constructURLParams(request.parameters);
		
		if (request.method == Request.METHOD.GET)
		{
			toBeCalled = Connector.constructURL(toBeCalled, urlParams);
		}
		Log.log(Log.LOG.VERBOSE, "Calling [" + toBeCalled + "]");
		
		long startTime = new Date().getTime();
		URL url = new URL(toBeCalled);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setConnectTimeout(request.connectTimeout);
		connection.setReadTimeout(request.readTimeout);
		Connector.addHeaders(connection, request.headers);
		connection.addRequestProperty("Accept-Encoding", "gzip");
		
		connection.setRequestMethod(request.method.name());
		if ((request.method == Request.METHOD.POST) || (request.method == Request.METHOD.PUT))
		{
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			Log.log(Log.LOG.VERBOSE, request.method.name() + " Params : " + urlParams);
			writer.write(urlParams);
			writer.flush();
		}
		
		connection.connect();
		
		Response response = new Response();
		response.url = toBeCalled;
		response.code = connection.getResponseCode();
		response.headers = new HashMap<String, String>();
		Connector.getHeaders(connection, response.headers);
		Log.log(Log.LOG.DEBUG, "Response code [" + response.code + "]");
		
		if (response.code != 200)
		{
			InputStream errorStream = connection.getErrorStream();
			if ("gzip".equals(connection.getContentEncoding()))
			{
				errorStream = new GZIPInputStream(errorStream);
			}
			String errorMessage = Connector.inputStreamToString(errorStream);
			if (!TextUtils.isEmpty(errorMessage))
			{
				throw new Exception(errorMessage);
			}
		}
		
		response.content = connection.getInputStream();
		
		if ("gzip".equals(connection.getContentEncoding()))
		{
			Log.log(Log.LOG.VERBOSE, "Using GZIP Decoding");
			response.content = new GZIPInputStream(response.content);
		}
		else
		{
			Log.log(Log.LOG.VERBOSE, "Using No Decoding");
		}
		
		long requestTime = new Date().getTime();
		Log.log(Log.LOG.VERBOSE, "Request time : " + (requestTime - startTime));
		
		return response;
	}
	
	/**
	 * perform a {@link Request} and return the response in {@link XML_Element} format (null if there is no baseUrl)
	 * 
	 * @param request
	 * @param baseUrl
	 * @return
	 * @throws Exception
	 */
	public static XML_Element performXMLRequest(Request request, String baseUrl) throws Exception
	{
		InputStream input = Connector.performRequest(request, baseUrl).content;
		long beforeParser = new Date().getTime();
		XML_Parser parser = new XML_Parser(input);
		Log.log(Log.LOG.VERBOSE, "Parsing time : " + (new Date().getTime() - beforeParser));
		
		return parser.getData();
	}
	
	/**
	 * perform a {@link Request} and return the response in {@link JSONArray} format (null if there is no baseUrl)
	 * 
	 * @param request
	 * @param baseUrl
	 * @return JSONArray
	 * @throws Exception
	 */
	public static JSONArray performJSONArrayRequest(Request request, String baseUrl) throws Exception
	{
		return new JSONArray(Connector.inputStreamToString(Connector.performRequest(request, baseUrl).content));
	}
	
	/**
	 * Perform a {@link Request} and return the response in {@link JSONObject} format (null if there is no baseUrl)
	 * 
	 * @param request
	 * @param baseUrl
	 * @return JSONObject
	 * @throws Exception
	 */
	public static JSONObject performJSONObjectRequest(Request request, String baseUrl) throws Exception
	{
		Response response = Connector.performRequest(request, baseUrl);
		String rContent = Connector.inputStreamToString(response.content);
		return new JSONObject(rContent);
	}
	
	/**
	 * Construct the full GET URL
	 * 
	 * @param baseUrl - String url to address
	 * @param params - HashMap, key value params to add to the request
	 * @return String
	 */
	private static String constructURL(String baseUrl, String params)
	{
		String toBeCalled = baseUrl;
		if (params != null)
		{
			String sep = "";
			if (!toBeCalled.contains("?"))
			{
				sep = "?";
			}
			else if (!toBeCalled.endsWith("?"))
			{
				sep = "&";
			}
			toBeCalled += sep + params;
		}
		return toBeCalled;
	}
	
	/**
	 * Construct a chain of params (key=value)
	 * 
	 * @param params
	 * @return string
	 */
	private static String constructURLParams(HashMap<String, String> params)
	{
		StringBuilder builder = new StringBuilder("");
		if ((params != null) && !params.isEmpty())
		{
			String sep = "";
			
			for (String key : params.keySet())
			{
				String value = params.get(key);
				
				if (value != null)
				{
					value = URLEncoder.encode(value);
					builder.append(sep).append(key).append("=").append(value);
					sep = "&";
				}
				else
				{
					Log.log(Log.LOG.WARNING, "Param " + key + " value is null");
				}
			}
		}
		return builder.toString();
	}
	
	/**
	 * Add headers to the urlConnection
	 * 
	 * @param urlConnection
	 * @param headers
	 */
	private static void addHeaders(URLConnection urlConnection, HashMap<String, String> headers)
	{
		if ((headers != null) && !headers.isEmpty())
		{
			Set<String> keys = headers.keySet();
			for (String key : keys)
			{
				urlConnection.addRequestProperty(key, headers.get(key));
			}
		}
	}
	
	private static void getHeaders(URLConnection urlConnection, HashMap<String, String> headers)
	{
		int idx = (urlConnection.getHeaderFieldKey(0) == null) ? 1 : 0;
		while (true)
		{
			String key = urlConnection.getHeaderFieldKey(idx);
			if (key == null)
			{
				break;
			}
			String value = urlConnection.getHeaderField(idx);
			headers.put(key, value);
			idx++;
		}
	}
	
	/**
	 * Convert an InputStream to a String
	 * 
	 * @param inputStream
	 * @return string
	 */
	protected static String inputStreamToString(InputStream inputStream)
	{
		StringBuilder total = new StringBuilder();
		try
		{
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while ((line = r.readLine()) != null)
			{
				total.append(line);
				if (!r.ready())
				{
					break;
				}
			}
			r.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return total.toString();
	}
}