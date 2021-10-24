package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;

import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;
import example.messaging.ServiceWorker;
import example.messaging.CDPClient.MessageTimeOutException;
import example.utils.Utils;

public class VersionTest extends BaseTest {
	private String URL = null;
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray results = null;
	private Iterator<Object> resultsIterator = null;
	private StringBuffer processResults = new StringBuffer();
	private String imageName = null;
	private final String filePath = System.getProperty("user.dir") + "/target";
	private int id2;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(true);
		super.beforeTest();
	}

	@Test
	public void getBroswerVersionTest() {
		// Arrange
		// Act
		try {
			CDPClient.setDebug(true);
			CDPClient.sendMessage("version");
			
			responseMessage = CDPClient.getResponseMessage(id, null);
			System.err.println("Get Broswer Version response: " + responseMessage);
		} catch (JSONException | InterruptedException | MessageTimeOutException
				| IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
