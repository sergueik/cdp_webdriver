package example;
/**
 * Copyright 2021 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class PerformanceMetricsTest extends BaseTest {
	private String URL = "https://www.wikipedia.org";
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray results = null;
	private Iterator<Object> resultsIterator = null;
	private StringBuffer processResults = new StringBuffer();
	private int id2;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(true);
		super.beforeTest();

	}

	@Test
	public void test1() {
		try {

			CDPClient.sendMessage(
					MessageBuilder.buildSetTimeDomainMessage(id, "threadTicks"));
			CDPClient.sendMessage(MessageBuilder.buildPerformanceEnableMessage(id));
			driver.get(URL);
			// need a new id here
			id2 = utils.getDynamicID();
			CDPClient
					.sendMessage(MessageBuilder.buildPerformanceGetMetricsMessage(id2));
			responseMessage = CDPClient.getResponseMessage(id2, "metrics");
			System.err
					.println("Get Performance Metrics response: " + responseMessage);

			results = new JSONArray(responseMessage);
			assertThat(results, notNullValue());
			// JSONArray has no method to know it size
			result = results.getJSONObject(0);
			for (String field : Arrays.asList(new String[] { "name", "value" })) {
				assertThat(String.format("has key %s", field), result.has(field),
						is(true));
			}

			resultsIterator = results.iterator();
			while (resultsIterator.hasNext()) {
				result = (JSONObject) resultsIterator.next();
				processResults.append(result.getString("name")).append(" ");
			}
			System.err.println("Performance Metrics " + processResults);
			CDPClient.sendMessage(MessageBuilder.buildPerformanceDisableMessage(id));
		} catch (WebDriverException | IOException | WebSocketException
				| MessageTimeOutException | InterruptedException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
		}
	}
}
