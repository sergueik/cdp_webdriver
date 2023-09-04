package example;

/**
 * Copyright 2021 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class UserAgentOverrideTest extends BaseTest {

	private static final By locator = By.xpath(
			"//*[@id=\"content-base\"]//table//th[contains(text(),\"USER-AGENT\")]/../td");

	private static String baseURL = "https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending";

	private static WebElement element = null;

	@Ignore
	@Test
	public void setUserAgentOverrideTest() {
		// Arrange
		driver.get(baseURL);
		element = driver.findElement(locator);
		// TODO:
		// utils.highlight(element);
		utils.sleep(100);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		// Act
		try {
			CDPClient.sendMessage(MessageBuilder
					.buildSetUserAgentOverrideMessage("python 2.7", "windows"));
		} catch (IOException | WebSocketException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		}
		driver.navigate().refresh();
		utils.sleep(1);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));
	}
}
