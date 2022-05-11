package example;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import example.messaging.MessageBuilder;

public class CookieTest extends BaseTest {

	private final static String url = "https://www.google.com";
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONArray results = null;

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
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkGetAllCookiesMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, "cookies");
			// NOTE: verbose
			// System.err.println("Response : " + responseMessage);
			results = new JSONArray(responseMessage);
			// Assert
			assertThat(results instanceof JSONArray, is(true));
			result = results.getJSONObject(0);
			// NOTE: verbose
			System.err.println("Cookie : " + result);
			assertThat(result, notNullValue());

			//
			for (String field : Arrays.asList(
					new String[] { "name", "value", "domain", "path", "expires", "size",
							"httpOnly", "secure", "session", "sameSite", "sameParty" })) {
				assertThat(result.has(field), is(true));
			}
			List<String> urls = Arrays.asList(driver.getCurrentUrl());
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkGetCookiesMessage(id, urls));
			responseMessage = CDPClient.getResponseMessage(id, "cookies");
			// Assert
			results = new JSONArray(responseMessage);
			System.err
					.println(String.format("Cookies for urls %s: %s", urls, results));
			assertThat(results instanceof JSONArray, is(true));
			result = results.getJSONObject(0);

		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
