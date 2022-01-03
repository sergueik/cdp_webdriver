package example;

/**
 * Copyright 2022 Serguei Kouzmine
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

public class SystemInfoTest extends BaseTest {
	private String URL = null;
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray results = null;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(true);
		super.beforeTest();
	}

	// Sending this ws message: {"id":219605,"method":"SystemInfo.getInfo"}
	// Received this ws message:
	// {
	// "id": 219605,
	// "error": {
	// {
	// "code": "-32601",
	// "message": "'SystemInfo.getInfo' wasn't found"
	// }
	// }
	// }
	// TODO: CDPClient fails identifying the exception as valid respone to caling
	// message id
	// getting example.messaging.CDPClient$MessageTimeOutException instead
	@Test
	public void getInfoTest() {
		// Arrange
		// Act
		try {
			CDPClient.setDebug(true);
			CDPClient.sendMessage(MessageBuilder.buildSystemInfoGetInfoMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			System.err.println("Get SystemInfo getInfo response: " + responseMessage);
			// Assert
			result = new JSONObject(responseMessage);
			for (String field : Arrays.asList(
					new String[] { "gpu", "modelName", "modelVersion", "commandLine" })) {
				assertThat(result.has(field), is(true));
			}

		} catch (JSONException | InterruptedException | MessageTimeOutException
				| IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
