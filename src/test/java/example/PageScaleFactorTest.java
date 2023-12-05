package example;

/**
 * Copyright 2023 Serguei Kouzmine
 */
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class PageScaleFactorTest extends BaseTest {

	private static final String URL = "https://www.wikipedia.org";
	private static String test = null;

	@BeforeClass
	public static void beforeClass() throws IOException {
		BaseTest.headless = false;
		BaseTest.beforeClass();
	}

	@Before
	public void before() {
		driver.navigate().to(URL);
		uiUtils.findElement(By.cssSelector("img.central-featured-logo"), 10);
	}

	@Test
	public void tes1() {
		// NOTE: only valus greater than 1 appear to work
		for (float scale : Arrays
				.asList(new Float[] { (float) 1.25, (float) 1.5, (float) 2 })) {
			try {
				CDPClient.sendMessage(
						MessageBuilder.buildEmulationSetPageScaleMessage(id, scale));
				utils.sleep(1);
				CDPClient.sendMessage(
						MessageBuilder.buildEmulationResetPageScaleMessage(id));

			} catch (WebDriverException | IOException | WebSocketException e) {
				System.err
						.println("Exception in " + test + " (ignored): " + e.getMessage());
			}
		}
	}

	@Test
	public void test2() {
		// NOTE: only valus greater than 1 appear to work ?
		for (float scale : Arrays
				.asList(new Float[] { (float) 0.25, (float) 0.5, (float) 0.75 })) {
			try {
				CDPClient.sendMessage(
						MessageBuilder.buildEmulationSetPageScaleMessage(id, scale));
				utils.sleep(1);
				CDPClient.sendMessage(
						MessageBuilder.buildEmulationResetPageScaleMessage(id));

			} catch (WebDriverException | IOException | WebSocketException e) {
				System.err
						.println("Exception in " + test + " (ignored): " + e.getMessage());
			}
		}
	}
}
