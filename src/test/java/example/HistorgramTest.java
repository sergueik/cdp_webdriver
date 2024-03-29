package example;
/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import example.messaging.MessageBuilder;

public class HistorgramTest extends BaseTest {

	private final static String url = "https://en.wikipedia.org/wiki/Main_Page";
	private String responseMessage = null;
	private JSONObject result = null;

	@Before
	public void beforeTest() throws IOException {
		// protected member does not work
		BaseTest.headless = true;
		// setter does not work
		super.setHeadless(true);
		super.beforeTest();
		driver.navigate().to(url);

	}

	@Test
	public void test() {
		// Arrange
		// Act
		try {
			CDPClient.sendMessage(MessageBuilder.buildBrowserHistogramsMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			// Assert
			result = new JSONObject(responseMessage);
			// NOTE: verbose
			// System.err.println("Response : " + responseMessage);

			assertThat(result.has("histograms"), is(true));
			JSONArray histograms = result.getJSONArray("histograms");
			assertThat(histograms, notNullValue());
			assertThat(histograms instanceof JSONArray, is(true));
			JSONObject histogram = histograms.getJSONObject(0);
			assertThat(histogram, notNullValue());
			assertThat(histogram instanceof JSONObject, is(true));

			// {"buckets":[{"high":2,"low":1,"count":8}],"name":"API.StorageAccess.AllowedRequests2","count":8,"sum":8}
			for (String field : Arrays
					.asList(new String[] { "buckets", "name", "count", "sum" })) {
				assertThat(histogram.has(field), is(true));
			}
			String name = histogram.getString("name");
			CDPClient
					.sendMessage(MessageBuilder.buildBrowserHistogramMessage(id, name));
			responseMessage = CDPClient.getResponseMessage(id, null);
			// Assert
			result = new JSONObject(responseMessage);
			// NOTE: verbose
			// System.err.println("Response : " + responseMessage);

			assertThat(result.has("histogram"), is(true));

		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
