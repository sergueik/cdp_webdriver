package example;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class DomElementHighlightTest extends BaseTest {

	private final static String url = "https://www.wikipedia.org";
	private final static String selector = "*[id^='js-link-box'] > strong";
	private boolean debug = false;
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray results = null;
	private JSONArray results2 = null;
	private static long nodeId = -1l;
	private static long rootNodeId = -1l;

	@Before
	public void beforeTest() throws IOException {
		// super.setHeadless(false);
		super.beforeTest();
		driver.navigate().to(url);
		try {
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDOMEnableMessage(id));
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildOverlayEnableMessage(id));
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
			CDPClient.sendMessage(MessageBuilder.buildOverlayDisableMessage(id));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		driver.get("about:blank");
	}

	@Test
	public void test1() throws IOException, WebSocketException,
			InterruptedException, MessageTimeOutException {

		id = utils.getDynamicID();
		CDPClient.sendMessage(MessageBuilder.buildDOMGetDocumentMessage(id));
		responseMessage = CDPClient.getResponseMessage(id, "root");
		System.err.println("DOM.getDocument response: " + responseMessage);
		result = new JSONObject(responseMessage);
		assertThat(result.has("nodeId"), is(true));
		rootNodeId = result.getLong("nodeId");
		assertTrue(rootNodeId != 0);

		for (String selector : Arrays
				.asList(new String[] { "#js-link-box-en > strong:nth-of-type(1)",
						"#js-link-box-de > strong:nth-of-type(1)",
						"#js-link-box-es > strong:nth-of-type(1)",
						"#js-link-box-it > strong:nth-of-type(1)" })) {
			id = utils.getDynamicID();
			System.err.println(String.format("query Selector: \"%s\"", selector));
			CDPClient.sendMessage(
					MessageBuilder.buildQuerySelectorMessage(id, rootNodeId, selector));
			int max_retry = CDPClient.getMaxRetry();
			CDPClient.setMaxRetry(10);
			nodeId = Integer.parseInt(CDPClient.getResponseMessage(id, "nodeId"));
			System.err.println("querySelector response: " + nodeId);

			id = utils.getDynamicID();
			CDPClient.setMaxRetry(max_retry);
			CDPClient
					.sendMessage(MessageBuilder.buildDOMHighlightNodeMessage(id, nodeId));
			utils.sleep(3);
			CDPClient.sendMessage(MessageBuilder.buildDOMHideHighlightMessage(id));
			utils.sleep(3);

		}
	}

	// @Ignore
	// TODO: interfering tests
	@Test
	public void test2() throws IOException, WebSocketException,
			InterruptedException, MessageTimeOutException {
		int max_retry = CDPClient.getMaxRetry();
		id = utils.getDynamicID();
		CDPClient.setMaxRetry(10);
		CDPClient.sendMessage(MessageBuilder.buildDOMGetDocumentMessage(id));

		responseMessage = CDPClient.getResponseMessage(id, "root");
		// System.err.println("DOM.getDocument response: " + responseMessage);
		result = new JSONObject(responseMessage);
		assertThat(result.has("nodeId"), is(true));
		rootNodeId = result.getLong("nodeId");
		assertTrue(rootNodeId != 0);

		id = utils.getDynamicID();
		System.err.println(String.format("query Selector: \"%s\"", selector));
		CDPClient.sendMessage(
				MessageBuilder.buildQuerySelectorMessage(id, rootNodeId, selector));

		CDPClient.setMaxRetry(10);
		nodeId = Integer.parseInt(CDPClient.getResponseMessage(id, "nodeId"));
		System.err.println("querySelector response: " + nodeId);

		id = utils.getDynamicID();
		CDPClient.setMaxRetry(max_retry);
		CDPClient
				.sendMessage(MessageBuilder.buildDOMHighlightNodeMessage(id, nodeId));
		utils.sleep(3);
		CDPClient.sendMessage(MessageBuilder.buildDOMHideHighlightMessage(id));
		utils.sleep(3);
	}

	@Ignore("querySelectorAll response: null")
	@Test
	public void test3() throws IOException, WebSocketException,
			InterruptedException, MessageTimeOutException {
		nodeId = -1l;
		id = utils.getDynamicID();
		CDPClient.sendMessage(MessageBuilder.buildDOMGetDocumentMessage(id));
		responseMessage = CDPClient.getResponseMessage(id, "root");
		System.err.println("DOM.getDocument response: " + responseMessage);
		result = new JSONObject(responseMessage);
		assertThat(result.has("nodeId"), is(true));
		rootNodeId = result.getLong("nodeId");
		assertTrue(rootNodeId != 0);

		id = utils.getDynamicID();
		System.err.println(String.format("querySelectorAll: \"%s\"", selector));
		System.err.println("id = " + id);
		int max_retry = CDPClient.getMaxRetry();
		CDPClient.setMaxRetry(20);
		CDPClient.setDebug(true);
		CDPClient.sendMessage(
				MessageBuilder.buildQuerySelectorAllMessage(id, rootNodeId, selector));
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
					.sendMessage(MessageBuilder.buildDOMHighlightNodeMessage(id, nodeId));
			utils.sleep(1);
			CDPClient.sendMessage(MessageBuilder.buildDOMHideHighlightMessage(id));
			utils.sleep(1);
		}

	}
}
