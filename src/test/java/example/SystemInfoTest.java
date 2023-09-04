package example;
/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class SystemInfoTest extends BaseTest {
	private String responseMessage = null;
	private JSONObject result = null;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(true);
		super.beforeTest();
	}

	@Test
	public void test() {
		// Arrange
		try {
			CDPClient.setDebug(true);
			// Act
			CDPClient.sendMessage(MessageBuilder.buildSystemInfoGetInfoMessage(id));
			result = new JSONObject(CDPClient.getResponseMessage(id, null));
			System.err.println("Get SystemInfo getInfo response: " + result);
			// Assert
			for (String field : Arrays.asList(
					new String[] { "gpu", "modelName", "modelVersion", "commandLine" })) {
				assertThat(result.has(field), is(true));
			}

		} catch (JSONException | InterruptedException | MessageTimeOutException
				| IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
