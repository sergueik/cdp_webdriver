package example;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.Arrays;

import org.json.JSONObject;
import org.junit.Test;

import example.messaging.MessageBuilder;

public class RuntimeTest extends BaseTest {
	private String URL = "https://www.wikipedia.org";
	private JSONObject responseMessage = null;
	private JSONObject result = null;

	@Test
	public void test1() {
		try {
			// Act
			driver.navigate().to(URL);

			final String selector = "input#searchInput";
			CDPClient.setDebug(true);
			CDPClient.sendMessage(MessageBuilder.buildCustomRuntimeEvaluateMessage(id,
					selector, false));
			// Assert
			CDPClient.setDebug(false);
			responseMessage = new JSONObject(CDPClient.getResponseMessage(id, null));
			assertThat(responseMessage, notNullValue());
			assertThat(responseMessage.has("result"), is(true));
			result = responseMessage.getJSONObject("result");
			System.err.println("getRuntimeEvaluateTest result: " + result);
			// NOTE:
			// http://hamcrest.org/JavaHamcrest/javadoc/1.3/org/hamcrest/CoreMatchers.html#allOf(java.lang.Iterable)
			// is not handy
			for (String field : Arrays
					.asList(new String[] { "type", "className", "description" })) {
				assertThat(result.has(field), is(true));
			}
			assertThat(result.getString("className"), is("HTMLInputElement"));
		} catch (Exception e) {
			System.err.println("Web Driver exception (ignored): " + e.getMessage());
		}
	}

	@Test
	// invalid css selector
	public void test2() {
		try {
			// Act
			driver.navigate().to(URL);

			final String selector = "input//searchInput";
			CDPClient.setDebug(true);
			CDPClient.sendMessage(MessageBuilder.buildCustomRuntimeEvaluateMessage(id,
					selector, false));
			// Assert
			CDPClient.setDebug(false);
			responseMessage = new JSONObject(CDPClient.getResponseMessage(id, null));
			assertThat(responseMessage, notNullValue());
			assertThat(responseMessage.has("result"), is(true));
			result = responseMessage.getJSONObject("result");
			System.err.println("getRuntimeEvaluateTest result: " + result);
			for (String field : Arrays
					.asList(new String[] { "type", "className", "description" })) {
				assertThat(result.has(field), is(true));
			}
			assertThat(result.getString("className"), is("DOMException"));
			assertThat(result.getString("description"),
					containsString("is not a valid selector"));
		} catch (Exception e) {
			System.err.println("Web Driver exception (ignored): " + e.getMessage());
		}
	}

	@Test
	public void test3() {
		try {
			// Act

			driver.navigate().to(URL);

			final String selector = "//input";
			CDPClient.setDebug(true);
			CDPClient.sendMessage(MessageBuilder.buildCustomRuntimeEvaluateMessage(id,
					selector, false));
			// Assert
			CDPClient.setDebug(false);
			responseMessage = new JSONObject(CDPClient.getResponseMessage(id, null));
			assertThat(responseMessage, notNullValue());
			assertThat(responseMessage.has("result"), is(true));
			result = responseMessage.getJSONObject("result");
			System.err.println("getRuntimeEvaluateTest result: " + result);
			for (String field : Arrays
					.asList(new String[] { "type", "className", "description" })) {
				assertThat(result.has(field), is(true));
			}
			assertThat(result.getString("className"), is("XPathResult"));
		} catch (Exception e) {
			System.err.println("Web Driver exception (ignored): " + e.getMessage());
		}
	}

	@Test
	// invalid xpath
	public void test4() {
		try {
			// Act

			driver.navigate().to(URL);

			final String selector = "//input[";
			CDPClient.setDebug(true);
			CDPClient.sendMessage(MessageBuilder.buildCustomRuntimeEvaluateMessage(id,
					selector, false));
			// Assert
			CDPClient.setDebug(false);
			responseMessage = new JSONObject(CDPClient.getResponseMessage(id, null));
			assertThat(responseMessage, notNullValue());
			assertThat(responseMessage.has("result"), is(true));
			result = responseMessage.getJSONObject("result");
			System.err.println("getRuntimeEvaluateTest result: " + result);
			for (String field : Arrays
					.asList(new String[] { "type", "className", "description" })) {
				assertThat(result.has(field), is(true));
			}
			assertThat(result.getString("className"), is("DOMException"));
			assertThat(result.getString("description"),
					containsString("is not a valid XPath expression"));

		} catch (Exception e) {
			System.err.println("Web Driver exception (ignored): " + e.getMessage());
		}
	}

}
