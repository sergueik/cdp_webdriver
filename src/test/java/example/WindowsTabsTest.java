package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

/**
 * Selected test scenarios for Selenium 3.x Chrome Developer Tools bridge inspired
 * origin: https://github.com/sachinguptait/SeleniumAutomation
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WindowsTabsTest extends BaseTest {

	private static String url = "https://en.wikipedia.org/wiki/Main_Page";
	private static Map<Integer, String> data = new HashMap<>();
	private static String responseMessage = null;
	private static String targetId;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(false);
		// too late, driver is already started ?
		// note: actually affects subsequent tests
		// TODO: have two base classes
		super.beforeTest();
	}

	// https://github.com/qtacore/chrome_master/blob/master/chrome_master/input_handler.py#L32
	@Test
	public void tabTest() {
		try {
			CDPClient.sendMessage(MessageBuilder.buildCreateTargetMessage(id, url,
					640, 480, null, false, true, false));
			utils.sleep(1);
			targetId = CDPClient.getResponseMessage(id, "targetId");
			System.err.println("Create Target targetId: " + targetId);
			CDPClient
					.sendMessage(MessageBuilder.buildActivateTargetMessage(id, targetId));
			responseMessage = CDPClient.getResponseMessage(id, null);
			System.err.println("Activate Target  Response: " + responseMessage);
			utils.sleep(10);
			uiUtils.takeScreenShot();
		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test
	public void windowTest() {
		try {
			CDPClient
					.sendMessage(MessageBuilder.buildCreateTargetMessage(id, url, false));
			utils.sleep(1);
			targetId = CDPClient.getResponseMessage(id, "targetId");
			System.err.println("Create window targetId: " + targetId);
			CDPClient
					.sendMessage(MessageBuilder.buildActivateTargetMessage(id, targetId));

		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

}

