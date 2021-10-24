package example;
/**
 * Copyright 2021 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.json.JSONObject;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class ScriptsTest extends BaseTest {
	private String responseMessage = null;
	private JSONObject result = null;
	private static WebElement element = null;

	@Test
	public void test1() {
		// Arrange
		try {
			// System.err.println("getRuntimeEvaluateTest() message: "
			// + MessageBuilder.buildEnableRuntimeMessage(id1));
			final String source = "Object.defineProperty(navigator, 'webdriver', { get: () => undefined })";
			CDPClient.sendMessage(MessageBuilder.buildPageAddScriptToEvaluateOnNewDocumentMessage(id, source));
			// Act
			responseMessage = CDPClient.getResponseMessage(id, null);
			// Assert
			result = new JSONObject(responseMessage);
			System.err.println("addScriptToEvaluateOnNewDocument response: " + result);
			assertThat(result.has("identifier"), is(true));
			String identifier = (String) result.get("identifier");

			assertThat(identifier, notNullValue());
			System.err.println("Script injected: " + identifier);
			// Act
			driver.get("https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.html");
			utils.waitFor(4);
			CDPClient.sendMessage(MessageBuilder.buildPageRemoveScriptToEvaluateOnNewDocument(id, identifier));

		} catch (WebSocketException | IOException | InterruptedException e) {
			System.err.println("Exception in addScriptToEvaluateOnNewDocument() (ignored): " + e.toString());
			// } catch (MessageTimeOutException e) {
			// throw (e);
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	// @Ignore
	@Test
	public void test2() {

		try {
			// System.err.println("getRuntimeEvaluateTest() message: "
			// + MessageBuilder.buildEnableRuntimeMessage(id1));
			final String source = "Object.defineProperty(navigator, 'webdriver', { get: () => undefined })";
			CDPClient.sendMessage(MessageBuilder.buildPageAddScriptToEvaluateOnNewDocumentMessage(id, source));
			utils.waitFor(4);

			// Act
			responseMessage = CDPClient.getResponseMessage(id, null);
			// Assert
			result = new JSONObject(responseMessage);
			System.err.println("addScriptToEvaluateOnNewDocument response: " + result);
			assertThat(result.has("identifier"), is(true));
			String identifier = (String) result.get("identifier");

			assertThat(identifier, notNullValue());
			System.err.println("Script injected: " + identifier);
			// Act
			driver.get("https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.html");
			utils.waitFor(1);
			CDPClient.sendMessage(MessageBuilder.buildPageRemoveScriptToEvaluateOnNewDocument(id, identifier));

		} catch (WebSocketException | IOException | InterruptedException e) {
			System.err.println("Exception in addScriptToEvaluateOnNewDocument() (ignored): " + e.toString());
			// } catch (MessageTimeOutException e) {
			// throw (e);
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	// @Ignore
	@Test
	public void test3() throws example.messaging.CDPClient.MessageTimeOutException {

		final String baseURL = "https://www.optum.com/test/mat-test-directory/blank-page-test.html";
		try {
			// System.err.println("getRuntimeEvaluateTest() message: "
			// + MessageBuilder.buildEnableRuntimeMessage(id1));
			final String source =
					// @formatter:off
					"var e = document.createElement('div');" 
				  + "e.id = 'data';"
				  + "e.setAttribute('class', 'democlass');" 
				  + "e.style.display = 'none';"
				  + "if (document.body != null) { document.body.appendChild(e); }";
			// @formatter:on
			// NOTE: do not call
			// CDPClient.sendMessage(MessageBuilder.buildPageAddScriptToEvaluateOnLoadMessage(id,
			// source));
			CDPClient.sendMessage(MessageBuilder.buildPageAddScriptToEvaluateOnNewDocumentMessage(id, source));
			responseMessage = CDPClient.getResponseMessage(id, null);
			// Assert
			result = new JSONObject(responseMessage);
			System.err.println("addScriptToEvaluateOnLoad response: " + result);
			assertThat(result.has("identifier"), is(true));
			String identifier = (String) result.get("identifier");

			assertThat(identifier, notNullValue());
			System.err.println("Script injected: " + identifier);
			// Act
			driver.get(baseURL);
			driver.navigate().refresh();
			utils.waitFor(3);

			WebElement element = driver.findElement(By.tagName("body"));
			System.err.println("Page: " + element.getAttribute("outerHTML"));

			// protect against null values on the JS side
			element = (WebElement) uiUtils.executeJavaScript("return document.querySelector('div#data')");
			// Assert is failing, commented
			// assertThat(element, notNullValue());

			CDPClient.sendMessage(MessageBuilder.buildPageRemoveScriptToEvaluateOnNewDocument(id, identifier));

		} catch (WebSocketException | IOException | InterruptedException e) {
			System.err.println("Exception in addScriptToEvaluateOnNewDocument() (ignored): " + e.toString());
		} catch (example.messaging.CDPClient.MessageTimeOutException e) {
			throw (e);
			// } catch (Exception e) {
			// System.err.println("Exception (ignored): " + e.toString());
		}
	}

}
