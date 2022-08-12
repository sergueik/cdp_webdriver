package example;

/**
 * Copyright 2022 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;
import example.utils.Utils;

// see also: https://habr.com/ru/post/518862/

public class GeolocationOverrideTest extends BaseTest {
	private String URL = null;
	private static WebElement element = null;
	// private static By locator = By
	// .cssSelector("button[aria-label='Show Your Location']");
	private static By locator = By
			.cssSelector("div[jsaction*='mouseover:mylocation.main']");

	@Test
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

	@Test(expected = MessageTimeOutException.class)
	// "Location Unavailable" test
	// Omitting any of the parameters in Emulation.setGeolocationOverride
	// emulates position unavailable
	// NOTE: also leads to NPE in AfterClass when run alone
	public void test2() throws IOException, WebSocketException,
			MessageTimeOutException, InterruptedException {
		URL = "https://mycurrentlocation.net";
		// NOTE: the MessageBuilder.buildMessage is private
		final Map<String, Object> params = new HashMap<>();
		final String method = "Emulation.setGeolocationOverride";
		final Double latitude = 37.422290;
		final Double longitude = -122.084057;
		params.put("latitude", latitude);
		params.put("longitude", longitude);
		params.put("accuracy", 100);
		params.remove("longitude");
		try {
			CDPClient.setDebug(true);
			int id2 = Utils.getInstance().getDynamicID();
			MessageBuilder.buildCustomMessage(id2, method, params);

			String responseMessage = CDPClient.getResponseDataMessage(id2);
			assertThat(responseMessage, notNullValue());
			// unreached due to MessageTimeOutException
			System.err
					.println("Incomplete \"Emulation.setGeolocationOverride\" response: "
							+ responseMessage);
			CDPClient.setDebug(false);
		} catch (MessageTimeOutException e) {
			System.err
					.println("Exception in \"Location Unavailable\" test (ignored): "
							+ e.getMessage());

			throw e;
		}
		driver.navigate().to(URL);
	}

	@Test
	public void test3() {
		URL = "https://mycurrentlocation.net";
		driver.navigate().to(URL);
		locator = By.cssSelector(".location-intro");
		element = uiUtils.findElement(locator, 120);
		assertThat(element.getText(), containsString("Mountain View"));
		System.err.println("Location explained: " + element.getText());
	}

	@Override
	@After
	public void afterTest() {
		try {
			// org.openqa.selenium.WebDriverException:
			// unknown error: cannot determine
			// loading status
			MessageBuilder.buildClearGeoLocationMessage(id);
			driver.navigate().to("about:blank");
		} catch (WebDriverException e) {
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

	private void setLocation()
			throws IOException, WebSocketException, MessageTimeOutException {
		// google HQ
		final Double latitude = 37.422290;
		final Double longitude = -122.084057;
		final long accuracy = 100;
		setLocation(latitude, longitude, accuracy);
	}

}
