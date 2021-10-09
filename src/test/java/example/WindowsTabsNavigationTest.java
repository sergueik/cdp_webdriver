package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	private static String sessionID;

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
				sessionID = CDPClient.getResponseMessage(id, "sessionId");
				System.err.println("Attach To Target response sessionId: " + sessionID);
				// generate a new id to get Target Info
				int id2 = utils.getDynamicID();
				CDPClient
						.sendMessage(MessageBuilder.buildTargetInfoMessage(id2, targetId));
				responseMessage = CDPClient.getResponseMessage(id2, null);
				System.err.println("Target Info response: " + responseMessage);
				utils.sleep(4);
				uiUtils.takeScreenShot();
			} catch (IOException | WebSocketException | InterruptedException
					| MessageTimeOutException e) {
				// ignore
				System.err.println("Exception (ignored): " + e.toString());
			}
		}

		try {
			CDPClient.sendMessage(MessageBuilder.buildGetTargetsMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, "targetInfos");
			System.err
					.println("Get Targets response[\"targetInfos\"]: " + responseMessage);
			JSONArray targetInfos = new JSONArray(responseMessage);
			int l = targetInfos.length();
			for (int i = 0; i < l; i++) {
				JSONObject targetInfo = targetInfos.getJSONObject(i);
				System.err.println("Can process object: " + targetInfo.toString());

				@SuppressWarnings("unchecked")
				Iterator<String> targetInfoKeysIterator = targetInfo.keys();

				while (targetInfoKeysIterator.hasNext()) {
					String key = targetInfoKeysIterator.next();
					System.err.println("observed row key: " + key);
				}
				for (String key : Arrays.asList("url", "title", "targetId")) {
					String val = targetInfo.getString(key);
					System.err.println("Observed key: " + key + ": " + val);
				}
			}
		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
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
				sessionID = CDPClient.getResponseMessage(id, "sessionId");
				System.err.println("Attach To Target response sessionId: " + sessionID);

				// generate a new id to get Target Info
				int id2 = utils.getDynamicID();

				CDPClient
						.sendMessage(MessageBuilder.buildTargetInfoMessage(id2, targetId));
				responseMessage = CDPClient.getResponseMessage(id2, null);
				System.err.println("Target Info response: " + responseMessage);

				utils.sleep(4);
				uiUtils.takeScreenShot();
			} catch (IOException | WebSocketException | InterruptedException
					| MessageTimeOutException e) {
				System.err.println("Exception (ignored): " + e.toString());
			}
		}
		try {
			CDPClient.sendMessage(MessageBuilder.buildGetTargetsMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			System.err.println("Get Targets response: " + responseMessage);
		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		}
		// NOTE: local var shadow
		for (String targetId : targets) {
			try {
				CDPClient.sendMessage(
						MessageBuilder.buildAttachToTargetMessage(id, targetId));
				sessionID = CDPClient.getResponseMessage(id, "sessionId");
				System.err.println("Attach To Target response sessionId: " + sessionID);

				CDPClient.sendMessage(MessageBuilder.buildDetachFromTargetMessage(id,
						sessionID, targetId));

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
