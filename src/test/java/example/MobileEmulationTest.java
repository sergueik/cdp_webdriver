package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class MobileEmulationTest extends BaseTest {
	private static final String URL = "https://testproject.io/";
	private static String test = null;

	@BeforeClass
	public static void beforeClass() throws IOException {
		BaseTest.headless = false;
		BaseTest.beforeClass();
	}

	// based on:
	// https://github.com/SrinivasanTarget/selenium4CDPsamples/blob/master/src/test/java/DevToolsTest.java#L149
	@Test
	public void emulateDeviceTest() {
		test = "emulateDeviceTest";
		try {
			CDPClient.sendMessage(MessageBuilder.buildEmulationSetUserAgentMessage(id,
					"Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1"));
			CDPClient.sendMessage(MessageBuilder.buildEmulationSetDeviceMetricsMessage(id, 375, 812, 3));
			driver.get(URL);
			CDPClient.sendMessage(MessageBuilder.buildEmulationResetPageScaleMessage(id));
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println("Exception in " + test + " (ignored): " + e.getMessage());
		}

	}
}
