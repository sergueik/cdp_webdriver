package example;

/**
 * Copyright 2023 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;
import example.messaging.ServiceWorker;
import example.utils.Utils;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import example.messaging.MessageBuilder;

public class FocusTest extends BaseTest {

	private static final String selector = "input";

	private String URL = "https://formy-project.herokuapp.com/form";

	private boolean debug = false;
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray results = null;
	private static long nodeId = (long) -1;

	@Before
	public void beforeTest() throws IOException {
		super.beforeTest();
		driver.navigate().to(URL);
		try {
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDOMEnableMessage(id));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@After
	public void after() {
		// Arrange
		try {
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDOMDisableMessage(id));
			id = utils.getDynamicID();
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		driver.get("about:blank");
	}

	@Test
	public void test1() throws Exception {

		try {
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDOMGetDocumentMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, "root");
			// System.err.println("DOM.getDocument response: " + responseMessage);
			result = new JSONObject(responseMessage);
			assertThat(result.has("nodeId"), is(true));
			nodeId = result.getLong("nodeId");
			assertThat(nodeId != 0, is(true));

			id = utils.getDynamicID();
			System.err.println(String.format("query Selector: \"%s\"", selector));
			int max_retry = CDPClient.getMaxRetry();
			// CDPClient.setDebug(true);
			CDPClient.setMaxRetry(10);
			CDPClient.sendMessage(
					MessageBuilder.buildQuerySelectorMessage(id, nodeId, selector));

			nodeId = Integer.parseInt(CDPClient.getResponseMessage(id, "nodeId"));
			System.err.println("querySelector response: " + nodeId);

			id = utils.getDynamicID();
			CDPClient.setMaxRetry(max_retry);
			CDPClient.setDebug(false);
			CDPClient
					.sendMessage(MessageBuilder.buildDOMFocusMessage(id, (int) nodeId));
			utils.sleep(3);

		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test
	public void test2() throws Exception {

		try {
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDOMGetDocumentMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, "root");
			result = new JSONObject(responseMessage);
			assertThat(result.has("nodeId"), is(true));
			nodeId = result.getLong("nodeId");
			assertThat(nodeId != 0, is(true));

			id = utils.getDynamicID();
			System.err.println(String.format("query Selector: \"%s\"", selector));
			// System.err.println("id = " + id);
			int max_retry = CDPClient.getMaxRetry();
			CDPClient.setMaxRetry(20);
			CDPClient.sendMessage(
					MessageBuilder.buildQuerySelectorAllMessage(id, nodeId, selector));
			responseMessage = CDPClient.getResponseMessage(id, "nodeIds");
			CDPClient.setMaxRetry(max_retry);
			CDPClient.setDebug(false);
			System.err.println("querySelectorAll response: " + responseMessage);

			results = new JSONArray(responseMessage);
			assertThat(results.length(), greaterThan(1));
			for (int cnt = 0; cnt != results.length(); cnt++) {
				nodeId = results.getInt(cnt);
				id = utils.getDynamicID();
				CDPClient
						.sendMessage(MessageBuilder.buildDOMFocusMessage(id, (int) nodeId));
				utils.sleep(1);
			}

		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
