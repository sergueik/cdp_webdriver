package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import example.messaging.CDPClient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import java.util.Arrays;

import org.json.JSONObject;
import org.json.JSONException;

import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class NavigationTest extends BaseTest {

	private String responseMessage = null;
	private JSONObject result = null;
	private int id2;
	private int max_retry = 0;

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocuments
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM#type-Node
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelector
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-describeNode
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-focus
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-highlightNode
	// https://chromedevtools.github.io/devtools-protocol/tot/Runtime#type-RemoteObjectId
	//
	@Test
	public void getDocumentTest() {
		// Arrange
		long nodeId = (long) -1;
		driver.get("https://www.wikipedia.org");
		try {
			CDPClient.sendMessage(MessageBuilder.buildGetDocumentMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			result = new JSONObject(responseMessage);
			assertThat(result.has("root"), is(true));

			assertThat(result.getJSONObject("root").has("nodeId"), is(true));
			nodeId = result.getJSONObject("root").getLong("nodeId");
			assertTrue(nodeId != 0);
		} catch (IOException | WebSocketException | InterruptedException
				| JSONException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		} catch (example.messaging.CDPClient.MessageTimeOutException e) {
			throw new RuntimeException(e.toString());
		}

		try {
			CDPClient
					.sendMessage(MessageBuilder.buildDescribeNodeMessage(id, nodeId));
			responseMessage = CDPClient.getResponseMessage(id, "node");
			JSONObject data = new JSONObject(responseMessage);
			for (String field : Arrays.asList(
					new String[] { "nodeType", "nodeName", "localName", "nodeValue" })) {
				assertThat(data.has(field), is(true));
			}

			nodeId = data.getInt("nodeId");
			System.err.println("Command returned node: " + data.toString(2));
			System.err.println("Command returned nodeid: " + nodeId);
		} catch (IOException | WebSocketException | InterruptedException
				| JSONException | CDPClient.MessageTimeOutException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		}
		if (nodeId == 0) {
			nodeId = 1;
		}
		try {
			CDPClient.sendMessage(
					MessageBuilder.buildGetOuterHTMLMessage(id, (int) nodeId));
			responseMessage = CDPClient.getResponseMessage(id, "outerHTML");
			System.err.println("Get Outer HTML response: " + responseMessage);
		} catch (IOException | WebSocketException | InterruptedException
				| JSONException | CDPClient.MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

		try {
			id2 = utils.getDynamicID();
			String selector = "input";
			CDPClient.sendMessage(
					MessageBuilder.buildQuerySelectorMessage(id2, nodeId, selector));
			System.err.println("id = " + id2);
			// CDPClient.setDebug(true);
			max_retry = CDPClient.getMaxRetry();
			CDPClient.setMaxRetry(10);
			nodeId = Integer.parseInt(CDPClient.getResponseMessage(id2, "nodeId"));
			System.err.println("Query Selector response: " + nodeId);
			CDPClient.sendMessage(
					MessageBuilder.buildGetOuterHTMLMessage(id, (int) nodeId));
			responseMessage = CDPClient.getResponseMessage(id, "outerHTML");
			System.err.println("Found Node Outer HTML response: " + responseMessage);
			CDPClient.setMaxRetry(max_retry);
			// result = new JSONObject(responseMessage);
			// assertThat(result.has("nodeId"), is(true));
			// nodeId = result.getLong("nodeId");
			// assertTrue(nodeId != 0);
			// System.err.println("Command returned nodeId: " + nodeId);
		} catch (IOException | WebSocketException | InterruptedException
				| JSONException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		} catch (CDPClient.MessageTimeOutException e) {
			throw new RuntimeException(e.toString());
		}
		/*
		command = "DOM.resolveNode";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		
		try {
			result = driver.executeCdpCommand(command, params);
			// depth, 1
			// Assert
			assertThat(result, hasKey("object"));
			// object
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) result.get("object");
			for (String field : Arrays.asList(
					new String[] { "type", "subtype", "className", "objectId" })) {
				assertThat(data, hasKey(field));
			}
			String objectId = (String) data.get("objectId");
			assertThat(objectId, notNullValue());
			System.err
					.println("Command " + command + " returned objectId: " + objectId);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}
		
		command = "DOM.something not defined";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
			// wasn't found
		}
		// DOM.removeNode
		command = "DOM.focus";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		try {
			// Act
			result = driver.executeCdpCommand(command, params);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
			// : unknown error: unhandled inspector error:
			// {"code":-32000,"message":"Element is not focusable"}
		}
		command = "DOM.highlightNode";
		try {
			// Act
			result = driver.executeCdpCommand(command, new HashMap<>());
			Utils.sleep(10000);
		} catch (org.openqa.selenium.WebDriverException e) {
			err.println(
					"Exception in command " + command + " (ignored): " + e.toString());
		}
		// TODO: command = "Runtime.callFunctionOn";
		 
		 */
	}
}
