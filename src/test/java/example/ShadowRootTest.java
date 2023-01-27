package example;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;
import example.utils.Utils;

import com.google.gson.JsonSyntaxException;

import com.google.gson.Gson;

//based on:
//https://youtu.be/O76h9Hf9-Os?list=PLMd2VtYMV0OSv62KjzJ4TFGLDTVtTtQVr&t=527
//Karate UI Api Testing Framework is likely to be calling CDP under the hood

public class ShadowRootTest extends BaseTest {

	private String URL = "chrome://downloads/";
	private String responseMessage = null;
	private JSONObject result = null;
	private static WebElement element = null;
	private static WebDriverWait wait;
	private static final String expression = "document.querySelector('body > downloads-manager').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#leftSpacer > h1').textContent";
	private int id1;
	private int id2;
	private static Gson gson = new Gson();

	@Test
	public void test() throws MessageTimeOutException {
		// Arrange
		try {
			driver.navigate().to(URL);
			CDPClient.sendMessage(MessageBuilder.buildEnableRuntimeMessage(id));
			id = Utils.getInstance().getDynamicID();
			CDPClient.sendMessage(
					MessageBuilder.buildRuntimeEvaluateMessage(id, expression, false));
			CDPClient.setMaxRetry(10);
			CDPClient.setDebug(true);
			responseMessage = CDPClient.getResponseMessage(id, null);
			CDPClient.setDebug(false);
			// Assert
			result = new JSONObject(responseMessage);
			assertThat(result, notNullValue());
			System.err.println("getRuntimeEvaluateTest Response: " + result);
			assertThat(result.has("result"), is(true));
			assertThat(result.has("result"), is(true));
			JSONObject result2 = result.getJSONObject("result");
			for (String field : Arrays.asList(new String[] { "type", "value" })) {
				assertThat(result2.has(field), is(true));
			}
			assertThat(result2.getString("value"), is("Downloads"));
			@SuppressWarnings("unchecked")
			Map<String, Object> data = gson.fromJson(result2.toString(), Map.class);

			for (String field : Arrays.asList(new String[] { "type", "value" })) {
				assertThat(data, hasKey(field));
			}
			assertThat((String) data.get("value"), is("Downloads"));
			System.err.println("Result value: " + (String) data.get("value"));
		} catch (WebSocketException | IOException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
