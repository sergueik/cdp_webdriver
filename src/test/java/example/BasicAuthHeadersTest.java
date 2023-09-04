package example;
/**
 * Copyright 2022,2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
		try {
			CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		} catch (WebSocketException e) {
			// ignore
		}

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

	private static WebElement element;

	@Test
	public void test1() throws IOException, WebSocketException {
		driver.navigate().to(URL);
		// System.err.println("test 1:" + driver.getPageSource());
		element = driver.findElement(By.xpath("//body"));
		assertThat(element, notNullValue());
		// NOTE: interplay with headless test, for which the body is
		// "Unauthorized access\nYou are denied access to this resource."
		assertThat(element.getText(), is(""));
	}

	@Test
	// NOTE: simply disabling network
	// does not stop the browser sending the headers ??
	public void test2() throws IOException, WebSocketException {
		CDPClient.sendMessage(MessageBuilder
				.buildNetWorkSetExtraHTTPHeadersMessage(id, "authorization", ""));
		driver.navigate().to(URL);
		// System.err.println("test 2:" + driver.getPageSource());

		element = driver.findElement(By.xpath("//body"));
		assertThat(element, notNullValue());
		assertThat(element.getText(),
				is("The server was not able to understand this request"));
		assertThat(driver.getTitle(), is("Bad Request"));
	}

	@Test
	public void test3() throws IOException, WebSocketException {

		CDPClient.sendMessage(MessageBuilder.buildNetWorkSetExtraHTTPHeadersMessage(
				id, "authorization", "Basic " + authString));
		driver.navigate().to(URL);
		// System.err.println("test 3:" + driver.getPageSource());
		element = driver.findElement(By.xpath("//body"));
		assertThat(element, notNullValue());
		assertThat(element.getText(), is("Your browser made it!"));

	}

}
