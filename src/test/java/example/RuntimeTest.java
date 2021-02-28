package example;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.json.JSONObject;
import org.junit.Test;

import example.messaging.MessageBuilder;

public class RuntimeTest extends BaseTest {
	private String URL = "https://www.wikipedia.org";
	private JSONObject responseMessage = null;
	private JSONObject result = null;

	@Test
	public void test4() {
		try {
			// Act

			driver.navigate().to(URL);

			final String selector = "input";
			CDPClient.sendMessage(
					MessageBuilder.buildRuntimeEvaluateMessage(id, selector, false));
			// Assert
			responseMessage = new JSONObject(CDPClient.getResponseMessage(id, null));
			assertThat(responseMessage, notNullValue());
			assertThat(responseMessage.has("result"), is(true));
			result = responseMessage.getJSONObject("result");
			System.err.println("getRuntimeEvaluateTest result: " + result);
			for (String field : Arrays
					.asList(new String[] { "type", "className", "description" })) {
				assertThat(result.has(field), is(true));
			}

		} catch (Exception e) {
			System.err.println("Web Driver exception (ignored): " + e.getMessage());
		}
	}

}

