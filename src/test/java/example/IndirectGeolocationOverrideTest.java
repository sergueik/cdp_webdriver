package example;

/**
 * Copyright 2023 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

// based on: 
// https://github.com/Sushmada/Selenium_CDP/blob/master/src/ChromeDevToolsSeleniumIntegration/LocalizationTesting_Geolocation.java

// testing the browser locale guess based on calculated client location
// Madrid location will make browser switch the Results to Spainish as there local language
public class IndirectGeolocationOverrideTest extends BaseTest {

	private String URL = null;
	private static WebElement element = null;
	// private static By locator = By
	// .cssSelector("button[aria-label='Show Your Location']");
	private static By locator = By
			.cssSelector("div[jsaction*='mouseover:mylocation.main']");

	@Before
	public void before() throws IOException, WebSocketException,
			MessageTimeOutException, InterruptedException {
		this.setDebug(true);
		System.err.println("WebSocketURL: " + GetWebSocketURL());
		setLocation();
	}

	@Test
	public void test1() throws IOException, WebSocketException,
			MessageTimeOutException, InterruptedException {
		URL = "https://www.google.com/maps";

		driver.navigate().to(URL);
		element = uiUtils.findElement(locator, 120);
		element.click();
		utils.waitFor(10);
		uiUtils.takeScreenShot();
	}

	@Test
	public void test2() throws IOException, WebSocketException,
			MessageTimeOutException, InterruptedException {
		URL = "https://www.google.com";

		driver.get("https://www.google.com/");
		element = uiUtils.findElement(By.name("q"), 120);

		element.sendKeys("Netflix", Keys.ENTER);
		element = uiUtils.findElement(By.cssSelector(".LC20lb"), 120);
		setLocation();
		element.click();

		element = uiUtils.findElement(By.cssSelector(".our-story-card-title"), 120);

		String text = element.getText();
		assertThat(text, containsString("series ilimitadas"));
		System.err.println(text);
		utils.waitFor(10);
		uiUtils.takeScreenShot();
	}

	@Override
	@After
	public void afterTest() {
		try {
			// org.openqa.selenium.WebDriverException:
			// unknown error: cannot determine loading status
			CDPClient.sendMessage(MessageBuilder.buildClearGeoLocationMessage(id));
			driver.navigate().to("about:blank");
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println(
					"Web Driver exception in afterTest (ignored): " + e.getMessage());
		}
	}

	private void setLocation(Double latitude, Double longitude, long accuracy)
			throws IOException, WebSocketException, MessageTimeOutException {
		// Act
		CDPClient.setDebug(true);
		CDPClient.sendMessage(
				MessageBuilder.buildGeoLocationMessage(id, latitude, longitude));
		CDPClient.setDebug(false);
		// NOTE: the response is empty
	}

	// Right-click the place or area on the map. This will open a pop-up window.
	// You can find your latitude and longitude in decimal format at the top.
	private void setLocation()
			throws IOException, WebSocketException, MessageTimeOutException {
		// Mexico City MDX
		final Double latitude = 19.44;
		final Double longitude = -99.14;
		final long accuracy = 1;
		setLocation(latitude, longitude, accuracy);
	}

}
