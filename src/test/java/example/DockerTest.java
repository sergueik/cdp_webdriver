package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;
import example.messaging.ServiceWorker;
import example.utils.Utils;

public class DockerTest extends DockerBaseTest {
	private String URL = null;
	private String responseMessage = null;
	private JSONObject result = null;
	private String imageName = null;
	private final String filePath = System.getProperty("user.dir") + "/target";

	@Before
	public void beforeTest() throws IOException {
		super.beforeTest();
	}

	// NOTE: this test is identical to DemoTest.getBroswerVersionTest
	@Test
	public void getBroswerVersionTest() {

		// Arrange
		// Act
		try {
			CDPClient.setDebug(true);
			CDPClient.sendMessage(MessageBuilder.buildBrowserVersionMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			// Assert
			result = new JSONObject(responseMessage);
			for (String field : Arrays.asList(new String[] { "protocolVersion",
					"product", "revision", "userAgent", "jsVersion" })) {
				assertThat(result.has(field), is(true));
			}
			// ServiceWorker serviceWorker = CDPClient.getServiceWorker(URL, 10,
			// "activated");
			// System.out.println(serviceWorker.toString());
			// Assert.assertEquals(serviceWorker.getStatus(), "activated");
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
