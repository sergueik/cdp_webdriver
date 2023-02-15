package example;
/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class ComputedStyleTest extends BaseTest {

	private final static String url = "https://getbootstrap.com/docs/4.0/components/buttons/";
	private final ArrayList<String> classes = new ArrayList<String>(
			Arrays.asList("btn-primary", "btn-secondary", "btn-success", "btn-danger",
					"btn-warning", "btn-info", "btn-light", "btn-dark", "btn-link"));

	private static String selector = null;

	private static String command = null;
	private WebElement element;

	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray result2 = null;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(false);
		super.beforeTest();
		driver.navigate().to(url);
		try {
			CDPClient.sendMessage(MessageBuilder.buildDOMEnableMessage(id));
			CDPClient.sendMessage(MessageBuilder.buildCSSEnableMessage(id));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@After
	public void after() {
		// Arrange
		try {
			CDPClient.sendMessage(MessageBuilder.buildDOMDisableMessage(id));
			CDPClient.sendMessage(MessageBuilder.buildCSSDisableMessage(id));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		driver.get("about:blank");
	}

	private static long nodeId = (long) -1;
	private static long rootNodeId = (long) -1;
	private int id2;
	private int max_retry;

	@Test
	public void test() {

		try {
			id2 = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildGetDocumentMessage(id2));
			responseMessage = CDPClient.getResponseMessage(id2, null);
			// System.err.println("getDocument: " + responseMessage);
			result = new JSONObject(responseMessage);
			assertThat(result.has("root"), is(true));

			assertThat(result.getJSONObject("root").has("nodeId"), is(true));
			rootNodeId = result.getJSONObject("root").getLong("nodeId");
			assertTrue(rootNodeId != 0);
			for (String data : classes) {

				selector = String.format("div.bd-example button.%s", data);
				System.err.println(String.format("query Selector: \"%s\"", selector));

				id2 = utils.getDynamicID();

				CDPClient.sendMessage(MessageBuilder.buildQuerySelectorMessage(id2,
						rootNodeId, selector));
				System.err.println("id = " + id2);
				max_retry = CDPClient.getMaxRetry();
				CDPClient.setMaxRetry(10);
				nodeId = Integer.parseInt(CDPClient.getResponseMessage(id2, "nodeId"));
				System.err.println("Query Selector response: " + nodeId);

				id2 = utils.getDynamicID();

				CDPClient.sendMessage(
						MessageBuilder.buildGetComputedStyleForNode(id2, nodeId));
				System.err.println("id = " + id2);
				responseMessage = CDPClient.getResponseMessage(id2, "computedStyle");
				// System.err.println("GetComputedStyleForNode response: " +
				// responseMessage);
				result2 = new JSONArray(responseMessage);
				result2.forEach((result2) -> {
					// System.err.println("result2:" + result2);
					assertThat(result2 instanceof JSONObject, is(true));
					String name = ((JSONObject) result2).getString("name");
					// System.err.println(String.format("name: \"%s\"", name));
					if (name.matches("background-color")) {
						System.err.println(String.format("computed style: %s",
								((JSONObject) result2).getString("value")));
					}
				});
				id2 = utils.getDynamicID();
				CDPClient.sendMessage(
						MessageBuilder.buildGetOuterHTMLMessage(id2, (int) nodeId));
				responseMessage = CDPClient.getResponseMessage(id2, "outerHTML");
				System.err.println("Get Outer HTML response: " + responseMessage);
			}
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
