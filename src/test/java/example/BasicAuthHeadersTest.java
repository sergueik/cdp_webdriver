package example;

/**
 * Copyright 2022 Serguei Kouzmine
 */

// example usage: 
// mvn -Dtrace_id=%TRACEID% -Dversion=01 -Dparent_id=%PARENTID% test
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class BasicAuthHeadersTest extends BaseTest {
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
		super.setHeadless(false);
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
		System.err.println(driver.getPageSource());
	}

	@Test(expected = AssertionError.class)
	public void test2() throws IOException, WebSocketException {
		// NOTE: simply disabling network does not stop the browser sending headers
		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		CDPClient.sendMessage(MessageBuilder
				.buildNetWorkSetExtraHTTPHeadersMessage(id, "authorization", ""));
		driver.navigate().to(URL);
		assertThat(driver.getPageSource(),
				not(containsString("Your browser made it!")));
		System.err.println(driver.getPageSource());
	}

}
