package example;

/**
 * Copyright 2023 Serguei Kouzmine
 */
import java.io.IOException;

import org.junit.Before;
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

public class WindowResizeTest extends BaseTest {

	private static String responseMessage = null;
	private static Long windowId;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(false);
		// too late, driver is already started ?
		// note: actually affects subsequent tests
		// TODO: have two base classes
		super.beforeTest();
	}

	@Test
	public void tabTest() {
		try {
			CDPClient.sendMessage(
					MessageBuilder.buildBrowserGetWindowForTargetMessage(id));
			utils.sleep(1);
			windowId = Long.parseLong(CDPClient.getResponseMessage(id, "windowId"));
			System.err.println("windowId: " + windowId);
			CDPClient.sendMessage(
					MessageBuilder.buildBrowserGetWindowBoundsMessage(id, windowId));
			responseMessage = CDPClient.getResponseMessage(id, null);
			System.err.println("Get Window Bounds response: " + responseMessage);
			id = utils.getDynamicID();
			System.err.println("next message id: " + id);
			CDPClient.sendMessage(MessageBuilder.buildBrowserSetWindowBoundsMessage(
					id, windowId, 0, 0, 100, 100, "minimized"));
			// this will produce no response
			id = utils.getDynamicID();
			System.err.println("next message id: " + id);
			CDPClient.sendMessage(
					MessageBuilder.buildBrowserGetWindowBoundsMessage(id, windowId));
			responseMessage = CDPClient.getResponseMessage(id, null);
			// NOTE: "minimized" apparently does not work
			System.err.println("Get Window Bounds response: " + responseMessage);
			utils.sleep(10);
			id = utils.getDynamicID();
			System.err.println("next message id: " + id);
			CDPClient.sendMessage(MessageBuilder.buildBrowserSetWindowBoundsMessage(
					id, windowId, 0, 0, 1024, 768, "normal"));
			// this will produce no response
			id = utils.getDynamicID();
			System.err.println("next message id: " + id);
			CDPClient.sendMessage(
					MessageBuilder.buildBrowserGetWindowBoundsMessage(id, windowId));
			responseMessage = CDPClient.getResponseMessage(id, null);
			System.err.println("Get Window Bounds response: " + responseMessage);
			utils.sleep(10);

			// uiUtils.takeScreenShot();
		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
