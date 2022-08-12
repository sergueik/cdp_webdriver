package example;

/**
 * Copyright 2022 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class GeolocationOverrideTest extends BaseTest {
	private String URL = "https://www.google.com/maps";
	private static WebElement element = null;
	// private static By locator = By
	// .cssSelector("button[aria-label='Show Your Location']");
	private static By locator = By
			.cssSelector("div[jsaction*='mouseover:mylocation.main']");

	@Test
	// TODO: "Location Unavailable" test
	// Omitting any of the parameters in Emulation.setGeolocationOverride
	// emulates position unavailable
	// see also: https://habr.com/ru/post/518862/
	public void test1() throws IOException, WebSocketException,
			MessageTimeOutException, InterruptedException {
		URL = "https://www.google.com/maps";
		setLocation();
		driver.navigate().to(URL);
		element = uiUtils.findElement(locator, 120);
		element.click();
		utils.waitFor(10);
		uiUtils.takeScreenShot();
	}

	@Test
	public void test2() {
		URL = "https://mycurrentlocation.net";
		driver.navigate().to(URL);
		locator = By.cssSelector(".location-intro");
		element = uiUtils.findElement(locator, 120);
		assertThat(element.getText(), containsString("Mountain View"));
		System.err.println("text: " + element.getText());
	}

	private void setLocation()
			throws IOException, WebSocketException, MessageTimeOutException {
		// google HQ
		final Double latitude = 37.422290;
		final Double longitude = -122.084057;
		final long accuracy = 100;
		setLocation(latitude, longitude, accuracy);
	}

	@Override
	@After
	public void afterTest() {
		try {
			// org.openqa.selenium.WebDriverException:
			// unknown error: cannot determine
			// loading status
			driver.navigate().to("about:blank");
		} catch (WebDriverException e) {
			System.err.println(
					"Web Driver exception in afterTest (ignored): " + e.getMessage());
		}
	}

	private void setLocation(Double latitude, Double longitude, long accuracy)
			throws IOException, WebSocketException, MessageTimeOutException {
		// Act
		CDPClient.sendMessage(
				MessageBuilder.buildGeoLocationMessage(id, latitude, longitude));
		// the response is empty
		/*
		CDPClient.setDebug(true);
		responseMessage = CDPClient.getResponseMessage(id, null);
		CDPClient.setDebug(false);
		System.err.println("buildGeoLocationMessage Response: " + responseMessage);
		*/
	}

}
