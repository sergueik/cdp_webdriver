package example;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class BasicAuthHeadersFailigTest extends BaseTest {
	private String URL = "https://jigsaw.w3.org/HTTP/Basic/";
	private String authString = null;

	private final String username = "guest";
	private final String password = "guest";

	// NOTE: Default constructor cannot handle exception type
	// UnsupportedEncodingException thrown by implicit super constructor.
	// Must define an explicit constructor
	// private byte[] input = String.format("%s:%s", username,
	// password).getBytes("UTF-8");

	@Before
	public void beforeTest() throws IOException, UnsupportedEncodingException {
		super.setHeadless(true);
		super.beforeTest();
		driver.navigate().to("https://jigsaw.w3.org/HTTP");
		byte[] input = String.format("%s:%s", username, password).getBytes("UTF-8");
		authString = new String(Base64.encodeBase64(input));

	}

	@After
	public void afterTest() {
		// Exception IOException is not compatible with throws clause in
		// BaseTest.afterTest()
		try {
			CDPClient.sendMessage(MessageBuilder.buildNetWorkDisableMessage(id));
		} catch (IOException | WebSocketException e) {
		}
		super.afterTest();

	}

	@Test
	public void test1() throws IOException, WebSocketException {

		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));

		CDPClient.sendMessage(MessageBuilder.buildNetWorkSetExtraHTTPHeadersMessage(
				id, "authorization", "Basic " + authString));
		driver.navigate().to(URL);
		assertThat(driver.getPageSource(), containsString("Your browser made it!"));
		// System.err.println(driver.getPageSource());
	}

	public void test2() throws IOException, WebSocketException {
		// NOTE: simply disabling network does not
		// stop the browser sending the headers
		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		CDPClient.sendMessage(MessageBuilder
				.buildNetWorkSetExtraHTTPHeadersMessage(id, "authorization", ""));
		driver.navigate().to(URL);
		for (String message : Arrays.asList("Unauthorized access",
				"You are denied access to this resource.")) {
			assertThat(driver.getPageSource(), containsString(message));
		}
		// System.err.println(driver.getPageSource());
	}

}
