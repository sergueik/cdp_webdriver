package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */

	
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class HeadlessTest extends BaseTest {
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray results = null;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(true);
		super.beforeTest();
	}

	@Test
	public void getAllCookiesTest1() {
		// Arrange
		driver.navigate().to("https://www.google.com");
		try {
			// Act
			CDPClient.sendMessage(MessageBuilder.buildGetAllCookiesMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, "cookies");
			System.err.println("Get All Cookies response : " + responseMessage);
			// Assert
			results = new JSONArray(responseMessage);
			assertThat(results, notNullValue());
			result = results.getJSONObject(0);
			for (String field : Arrays.asList(new String[] { "path", "domain",
					"expires", "name", "value", "secure", "session" })) {
				assertThat(String.format("has key %s", field), result.has(field),
						is(true));
			}
		} catch (JSONException | InterruptedException | MessageTimeOutException
				| IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test
	public void getAllCookiesTest2() {
		// Arrange
		driver.navigate().to("https://www.google.com");
		try {
			// Act
			CDPClient.sendMessage(MessageBuilder.buildGetAllCookiesMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			System.err.println("Get All Cookies response : " + responseMessage);

			// Assert
			results = new JSONObject(responseMessage).getJSONArray("cookies");

			assertThat(results, notNullValue());
			result = results.getJSONObject(0);
			for (String field : Arrays.asList(new String[] { "path", "domain",
					"expires", "name", "value", "secure", "session" })) {
				assertThat(String.format("has key %s", field), result.has(field),
						is(true));
			}
		} catch (JSONException | InterruptedException | MessageTimeOutException
				| IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
