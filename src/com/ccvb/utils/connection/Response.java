package com.ccvb.utils.connection;

import java.io.InputStream;
import java.util.HashMap;

public class Response
{
	public String url;
	public int code;
	public InputStream content;
	public HashMap<String, String> headers;
}