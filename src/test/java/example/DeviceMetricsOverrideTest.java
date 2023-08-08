package example;

/**
 * Copyright 2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import org.junit.BeforeClass;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

/**
 * Selected test scenarios for Selenium 3.x Chrome Developer Tools bridge inspired
 * see also:
 * https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getLayoutMetrics
 * https://chromedevtools.github.io/devtools-protocol/tot/DOM/#type-Rect
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class DeviceMetricsOverrideTest extends BaseTest {

	private static int delay = 1;
	private JSONObject result = null;
	private int clientWidth = -1;
	private static String URL = "https://www.whatismybrowser.com/detect/what-http-headers-is-my-browser-sending";
	private Actions actions;
	private WebElement element;

	private static Integer height = 640;
	private static Map<Integer, Integer> widths = new HashMap<>();
	static {
		widths.put(600, 480);
		widths.put(480, 384);
		widths.put(250, 200);
	}
	private static String responseMessage = null;

	@BeforeClass
	public static void beforeClass() throws IOException {
		BaseTest.headless = false;
		BaseTest.beforeClass();
	}

	@Test
	public void test1() throws IOException, WebSocketException,
			InterruptedException, MessageTimeOutException {

		for (Integer device_width : widths.keySet()) {
			Integer viewport_width = widths.get(device_width);
			// Arrange
			System.err.println(String.format("Set device metrics to %02d %02d",
					device_width, height));
			CDPClient.sendMessage(MessageBuilder
					.buildEmulationSetDeviceMetricsMessage(id, device_width, height, 0));
			// Act
			driver.get(URL);

			// Assert
			int id2 = utils.getDynamicID();
			CDPClient
					.sendMessage(MessageBuilder.buildPageGetLayoutMetricsMessage(id2));

			responseMessage = CDPClient.getResponseMessage(id2, "cssLayoutViewport");
			System.err.println("cssLayoutViewport: " + responseMessage);
			result = new JSONObject(responseMessage);
			clientWidth = result.getInt("clientWidth");
			// assertThat(clientWidth, is(viewport_width));
			// System.err.println(String.format("%d", clientWidth));
			utils.sleep(delay);
			element = driver.findElement(By.xpath(
					"//*[@id=\"content-base\"]//table//th[contains(text(),\"VIEWPORT-WIDTH\")]/../td"));
			assertThat(element, notNullValue());
			actions = new Actions(driver);
			actions.moveToElement(element).build().perform();

			uiUtils.highlight(element, 1000);
			utils.sleep(delay);
			System.err.println("Page shows: VIEWPORT-WIDTH = " + element.getText());
			assertThat(element.getText(),
					containsString(String.format("%d", viewport_width)));
			uiUtils.scrollToElement(element);
			CDPClient
					.sendMessage(MessageBuilder.buildEmulationResetPageScaleMessage(id));
		}
	}

	@Test
	public void test2() throws IOException, WebSocketException,
			InterruptedException, MessageTimeOutException {

		for (Integer device_width : widths.keySet()) {
			Integer viewport_width = widths.get(device_width);
			// Arrange
			System.err.println(String.format("Set device metrics to %02d %02d",
					device_width, height));
			CDPClient.sendMessage(MessageBuilder
					.buildEmulationSetDeviceMetricsMessage(id, device_width, height, 0));
			// Act
			driver.get(URL);

			// Assert
			int id2 = utils.getDynamicID();
			CDPClient
					.sendMessage(MessageBuilder.buildPageGetLayoutMetricsMessage(id2));

			responseMessage = CDPClient.getResponseMessage(id2, null);
			System.err.println("Page Layout Metrics: " + responseMessage);
			utils.sleep(delay);
			element = driver.findElement(By.xpath(
					"//*[@id=\"content-base\"]//table//th[contains(text(),\"VIEWPORT-WIDTH\")]/../td"));
			assertThat(element, notNullValue());
			actions = new Actions(driver);
			actions.moveToElement(element).build().perform();

			uiUtils.highlight(element, 1000);
			utils.sleep(delay);
			System.err.println("Page shows: VIEWPORT-WIDTH = " + element.getText());
			assertThat(element.getText(),
					containsString(String.format("%d", viewport_width)));
			uiUtils.scrollToElement(element);
			CDPClient
					.sendMessage(MessageBuilder.buildEmulationResetPageScaleMessage(id));
		}
	}

}
