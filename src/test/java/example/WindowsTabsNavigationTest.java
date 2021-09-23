package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

public class WindowsTabsNavigationTest extends BaseTest {

	private static List<String> urls = Arrays.asList(
			"https://en.wikipedia.org/wiki/Main_Page", "https://www.google.com");
	private static List<String> targets = new ArrayList<>();
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
		for (String url : urls) {
			try {
				CDPClient.sendMessage(
						MessageBuilder.buildCreateTargetMessage(id, url, false));
				utils.sleep(1);
				targetId = CDPClient.getResponseMessage(id, "targetId");
				System.err.println("Create Tab targetId: " + targetId);
				targets.add(targetId);
				CDPClient.sendMessage(
						MessageBuilder.buildActivateTargetMessage(id, targetId));
				responseMessage = CDPClient.getResponseMessage(id, null);
				// Nothing is returned by "Target.activateTarget"

				CDPClient.sendMessage(
						MessageBuilder.buildAttachToTargetMessage(id, targetId));
				responseMessage = CDPClient.getResponseMessage(id, "sessionId");
				System.err
						.println("Attach To Target response sessionId: " + responseMessage);
				utils.sleep(4);
				uiUtils.takeScreenShot();
			} catch (IOException | WebSocketException | InterruptedException
					| MessageTimeOutException e) {
				// ignore
				System.err.println("Exception (ignored): " + e.toString());
			}
		}
		// NOTE: local var shadow
		for (String targetId : targets) {
			try {
				CDPClient
						.sendMessage(MessageBuilder.buildCloseTargetMessage(id, targetId));
				responseMessage = CDPClient.getResponseMessage(id, null);
				System.err.println(
						"Closed Tab TargetId: " + targetId + " status: " + responseMessage);
				utils.sleep(10);
			} catch (IOException | WebSocketException | InterruptedException
					| MessageTimeOutException e) {
				// ignore
				System.err.println("Exception (ignored): " + e.toString());
			}
		}
	}

	@Test
	public void windowTest() {
		for (String url : urls) {
			try {
				CDPClient.sendMessage(
						MessageBuilder.buildCreateTargetMessage(id, url, true));
				utils.sleep(1);
				targetId = CDPClient.getResponseMessage(id, "targetId");
				System.err.println("Create window targetId: " + targetId);
				CDPClient.sendMessage(
						MessageBuilder.buildActivateTargetMessage(id, targetId));
				// Nothing is returned by "Target.activateTarget"

				CDPClient.sendMessage(
						MessageBuilder.buildAttachToTargetMessage(id, targetId));
				responseMessage = CDPClient.getResponseMessage(id, "sessionId");
				utils.sleep(4);
				uiUtils.takeScreenShot();
				System.err
						.println("Attach To Target response sessionId: " + responseMessage);
			} catch (IOException | WebSocketException | InterruptedException
					| MessageTimeOutException e) {
				System.err.println("Exception (ignored): " + e.toString());
			}
		}
		// NOTE: local var shadow
		for (String targetId : targets) {
			try {
				CDPClient.sendMessage(
						MessageBuilder.buildAttachToTargetMessage(id, targetId));
				responseMessage = CDPClient.getResponseMessage(id, "sessionId");
				System.err
						.println("Attach To Target response sessionId: " + responseMessage);
				CDPClient
						.sendMessage(MessageBuilder.buildCloseTargetMessage(id, targetId));
				responseMessage = CDPClient.getResponseMessage(id, null);
				System.err.println(
						"Closed Tab TargetId: " + targetId + " status: " + responseMessage);
				utils.sleep(10);
			} catch (IOException | WebSocketException | InterruptedException
					| MessageTimeOutException e) {
				// ignore
				System.err.println("Exception (ignored): " + e.toString());
			}
		}
	}

}
