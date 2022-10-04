package example;

/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.utils.Utils;
import example.messaging.MessageBuilder;

public class DOMSnapshotTest extends BaseTest {
	private static String baseUrl = "http://www.java2s.com";
	private JSONObject result = null;
	private JSONObject result1 = null;
	private JSONObject result2 = null;
	private JSONObject result3 = null;
	Iterator<Object> results1Iterator = null;
	Iterator<Object> results2Iterator = null;
	Iterator<Object> results3Iterator = null;
	private static boolean debug = false;

	// Junit does not
	@Before
	public void beforeTest() throws IOException {
		// protected member does not work
		BaseTest.headless = true;
		// setter does not work
		super.setHeadless(true);
		super.beforeTest();
		// Arrange
		try {
			CDPClient.sendMessage(MessageBuilder.buildDOMSnapshotEnableMessage(id));
		} catch (IOException | WebSocketException e) {
		}
		driver.navigate().to(baseUrl);

	}

	@After
	public void afterTest() {
		try {
			CDPClient.sendMessage(MessageBuilder.buildDOMSnapshotDisableMessage(id));
		} catch (IOException | WebSocketException e) {
		}
		super.afterTest();
	}

	@Test
	public void test1() {
		try {
			// Act
			int id2 = Utils.getInstance().getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDOMSnapshotCaptureSnapshotMessage(id2));
			result = new JSONObject(CDPClient.getResponseMessage(id2, null));
			if (debug)
				System.err.println("result: " + result);
			// Assert
			for (String field : Arrays.asList(new String[] { "documents", "strings" })) {
				assertThat(result.has(field), is(true));
				// https://www.tabnine.com/code/java/methods/org.json.JSONObject/optJSONArray
				assertThat(result.optJSONArray(field), notNullValue());
				assertThat(result.optJSONArray(field) instanceof JSONArray, is(true));
				assertThat(result.getJSONArray(field).toList().size(), greaterThan(0));
			}
			int index = 1;
			result1 = result.getJSONArray("documents").getJSONObject(index);
			for (String field : Arrays.asList(new String[] { "documentURL", "title", "baseURL", "contentLanguage",
					"encodingName", "publicId", "systemId", "frameId", "nodes", "layout", "textBoxes", "scrollOffsetX",
					"scrollOffsetY", "contentWidth", "contentHeight" })) {
				assertThat(result1.has(field), is(true));
			}
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}

}
