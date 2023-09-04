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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import example.messaging.MessageBuilder;

public class ComputedStyleTest extends BaseTest {

	private final static String url = "https://getbootstrap.com/docs/4.0/components/buttons/";
	private final ArrayList<String> classes = new ArrayList<String>(
			Arrays.asList("btn-primary", "btn-secondary", "btn-success", "btn-danger",
					"btn-warning", "btn-info", "btn-light", "btn-dark", "btn-link"));

	private static String selector = null;

	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray result2 = null;
	private static final String propertyName = "background-color";
	private static final String value = "rgb(10,10,10)";

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(false);
		super.beforeTest();
		driver.navigate().to(url);
		try {
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildDOMEnableMessage(id));
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildCSSEnableMessage(id));
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
			CDPClient.sendMessage(MessageBuilder.buildCSSDisableMessage(id));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		driver.get("about:blank");
	}

	private static long nodeId = (long) -1;
	private static long rootNodeId = (long) -1;
	private int max_retry;

	@Test
	public void test() {

		try {
			id = utils.getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildGetDocumentMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			// System.err.println("getDocument: " + responseMessage);
			result = new JSONObject(responseMessage);
			assertThat(result.has("root"), is(true));

			assertThat(result.getJSONObject("root").has("nodeId"), is(true));
			rootNodeId = result.getJSONObject("root").getLong("nodeId");
			assertTrue(rootNodeId != 0);
			for (String data : classes) {

				selector = String.format("div.bd-example button.%s", data);
				System.err.println(String.format("query Selector: \"%s\"", selector));

				id = utils.getDynamicID();

				CDPClient.sendMessage(
						MessageBuilder.buildQuerySelectorMessage(id, rootNodeId, selector));
				System.err.println("id = " + id);
				max_retry = CDPClient.getMaxRetry();
				CDPClient.setMaxRetry(10);
				nodeId = Integer.parseInt(CDPClient.getResponseMessage(id, "nodeId"));
				System.err.println("Query Selector response: " + nodeId);

				id = utils.getDynamicID();

				CDPClient.sendMessage(
						MessageBuilder.buildGetComputedStyleForNode(id, nodeId));
				System.err.println("id = " + id);
				responseMessage = CDPClient.getResponseMessage(id, "computedStyle");
				// System.err.println("GetComputedStyleForNode response: " +
				// responseMessage);
				result2 = new JSONArray(responseMessage);
				result2.forEach((result2) -> {
					// System.err.println("result2:" + result2);
					assertThat(result2 instanceof JSONObject, is(true));
					String name = ((JSONObject) result2).getString("name");
					// System.err.println(String.format("name: \"%s\"", name));
					if (name.matches(propertyName)) {
						System.err.println(
								String.format("computed style: " + propertyName + ": %s",
										((JSONObject) result2).getString("value")));
					}
				});
				id = utils.getDynamicID();
				CDPClient.sendMessage(
						MessageBuilder.buildGetOuterHTMLMessage(id, (int) nodeId));
				responseMessage = CDPClient.getResponseMessage(id, "outerHTML");
				System.err.println("Get Outer HTML response: " + responseMessage);

				id = utils.getDynamicID();
				CDPClient.sendMessage(
						MessageBuilder.buildSetEffectivePropertyValueForNode(id,
								(int) nodeId, propertyName, value));
				utils.waitFor(1);
			}
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
