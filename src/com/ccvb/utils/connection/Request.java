package com.ccvb.utils.connection;

import java.util.HashMap;

/**
 * Abstraction Object for HTTP Requests
 */
public class Request
{
	/** Default connection timeout */
	public static final int DEFAULT_CONNECT_TIMEOUT = 3500;
	public static final int DEFAULT_READ_TIMEOUT = 6000;
	
	/** HTTP Method */
	public static enum METHOD
	{
		GET,
		POST,
		PUT,
		DELETE
	};
	
	/** PROTOCOL */
	public static enum PROTOCOL
	{
		HTTP,
		HTTPS
	};
	
	public METHOD method;
	public PROTOCOL protocol;
	public int connectTimeout;
	public int readTimeout;
	public HashMap<String, String> parameters;
	public HashMap<String, String> headers;
	
	/**
	 * Constructor
	 * 
	 * @param method
	 * @param protocol
	 * @param connectTimeout
	 * @param parameters
	 * @param headers
	 */
	public Request(METHOD method, PROTOCOL protocol, int connectTimeout, int readTimeout, HashMap<String, String> parameters, HashMap<String, String> headers)
	{
		this.method = method;
		this.protocol = protocol;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		this.parameters = parameters != null ? parameters : new HashMap<String, String>();
		this.headers = headers != null ? headers : new HashMap<String, String>();
	}
	
	/**
	 * Constructor
	 * 
	 * @param method
	 * @param protocol
	 * @param connectTimeout
	 * @param parameters
	 */
	public Request(METHOD method, PROTOCOL protocol, int connectTimeout, int readTimeout, HashMap<String, String> parameters)
	{
		this(method, protocol, connectTimeout, readTimeout, parameters, null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param method
	 * @param protocol
	 * @param connectTimeout in ms
	 */
	public Request(METHOD method, PROTOCOL protocol, int connectTimeout, int readTimeout)
	{
		this(method, protocol, connectTimeout, readTimeout, null, null);
	}
	
	/**
	 * Constructor with Default Connect TimeOut
	 * 
	 * @param method
	 * @param protocol
	 */
	public Request(METHOD method, PROTOCOL protocol)
	{
		this(method, protocol, Request.DEFAULT_CONNECT_TIMEOUT, Request.DEFAULT_READ_TIMEOUT);
	}
}