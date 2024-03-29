package example;
/**
 * Copyright 2023 Serguei Kouzmine
 */

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

public class DomElementClickTest extends BaseTest {

	private final static String url = "https://www.wikipedia.org";
	private final static String selector = "#js-link-box-de > strong";
	private boolean debug = false;
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray results = null;
	private JSONArray results2 = null;
	private static long nodeId = -1l;

	@Before
	public void beforeTest() throws IOException {
		// super.setHeadless(false);
		super.beforeTest();
		driver.navigate().to(url);
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
	public void test() {

		try {
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDOMGetDocumentMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, "root");
			// System.err.println("DOM.getDocument response: " + responseMessage);
			result = new JSONObject(responseMessage);
			assertThat(result.has("nodeId"), is(true));
			nodeId = result.getLong("nodeId");
			assertTrue(nodeId != 0);

			id = utils.getDynamicID();
			System.err.println(String.format("query Selector: \"%s\"", selector));
			CDPClient.sendMessage(
					MessageBuilder.buildQuerySelectorMessage(id, nodeId, selector));
			System.err.println("id = " + id);
			int max_retry = CDPClient.getMaxRetry();
			CDPClient.setMaxRetry(10);
			nodeId = Integer.parseInt(CDPClient.getResponseMessage(id, "nodeId"));
			System.err.println("querySelector response: " + nodeId);

			id = utils.getDynamicID();
			CDPClient.setMaxRetry(max_retry);
			CDPClient
					.sendMessage(MessageBuilder.buildGetContentQuadsMessage(id, nodeId));
			System.err.println("id = " + id);
			responseMessage = CDPClient.getResponseMessage(id, "quads");
			if (debug)
				System.err.println("getContentQuads response: " + responseMessage);
			results = new JSONArray(responseMessage);
			List<Integer> values = new ArrayList<>();
			results.forEach((Object o) -> {

				assertThat(o instanceof JSONArray, is(true));
				results2 = (JSONArray) o;
				System.err.println("results2: " + results2);
				results2.forEach((Object data) -> {
					if (debug)
						System.err.println("Quad data: " + data);
					// NOTE: observed quads to contain mix of Double and Long values which
					// appear to be fragile in deserialization to:
					// incompatible types: java.lang.Double cannot be converted to
					// java.lang.Long
					Double value = Double.parseDouble(data.toString());
					int coord = Math.round(value.longValue());
					values.add(coord);
				});
			});
			int x = values.get(0);
			int y = values.get(1);
			id = utils.getDynamicID();
			CDPClient.sendMessage(
					MessageBuilder.buildGetOuterHTMLMessage(id, (int) nodeId));
			responseMessage = CDPClient.getResponseMessage(id, "outerHTML");
			System.err.println("Target outer HTML: " + responseMessage);

			System.err.println(String.format("Click on: x=%d y=%d", x, y));
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDispatchMouseEventMessage(id, x,
					y, "mousePressed"));
			utils.sleep(2);
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDispatchMouseEventMessage(id, x,
					y, "mouseReleased"));
			utils.sleep(3);
			System.err.println("Navigated to: " + driver.getTitle());

		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
