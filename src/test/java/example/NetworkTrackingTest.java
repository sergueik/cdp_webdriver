package example;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class NetworkTrackingTest extends BaseTest {
	private String URL = "http://www.wikipedia.org";
	private String responseMessage = null;
	private String responseBodyMessage = null;
	private int id2;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(true);
		super.beforeTest();
	}

	@Test
	// NOTE: fragile
	public void test1() throws IOException, WebSocketException,
			InterruptedException, MessageTimeOutException {
		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		driver.navigate().to(URL);
		responseMessage = CDPClient.getResponseMessage("Network.requestWillBeSent");
		System.err.println("Network enabled Response: " + responseMessage);
		String reqId = (new JSONObject(responseMessage)).getJSONObject("params")
				.getString("requestId");
		CDPClient.setDebug(true);
		System.err.println("Request id: " + reqId);
		id2 = utils.getDynamicID();
		CDPClient
				.sendMessage(MessageBuilder.buildGetResponseBodyMessage(id2, reqId));
		CDPClient.setDebug(true);
		responseBodyMessage = CDPClient.getResponseBodyMessage(id2);
		CDPClient.setDebug(false);
		assertThat(responseBodyMessage, notNullValue());
		System.err.println("Get Response Body response: " + responseBodyMessage);
	}

}

