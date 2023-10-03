package example;
/**
 * Copyright 2021-2023 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.gson.Gson;
// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;
import example.utils.Utils;

// NOTE: Karate UI Api Testing Framework is likely to be calling CDP under the hood

@SuppressWarnings("unchecked")
public class ShadowRootTest extends BaseTest {

	private String URL = null;
	private String responseMessage = null;
	private JSONObject result = null;
	private JSONObject result2 = null;
	private Map<String, Object> data = new HashMap<>();
	private WebElement element;
	private static List<WebElement> elements = new ArrayList<>();
	private static String expression = null;
	private static Gson gson = new Gson();
	protected static int flexibleWait = 10;
	// based on:
	// https://youtu.be/O76h9Hf9-Os?list=PLMd2VtYMV0OSv62KjzJ4TFGLDTVtTtQVr&t=527

	@Test
	public void test1() throws MessageTimeOutException {
		// Arrange
		try {
			URL = "chrome://downloads/";
			driver.navigate().to(URL);
			CDPClient.sendMessage(MessageBuilder.buildEnableRuntimeMessage());
			id = Utils.getInstance().getDynamicID();
			expression = "document.querySelector('body > downloads-manager').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#toolbar').shadowRoot.querySelector('#leftSpacer > h1').textContent";
			CDPClient.sendMessage(
					MessageBuilder.buildRuntimeEvaluateMessage(id, expression, false));
			CDPClient.setMaxRetry(10);
			// CDPClient.setDebug(true);
			responseMessage = CDPClient.getResponseMessage(id, null);
			CDPClient.setDebug(false);
			// Assert
			result = new JSONObject(responseMessage);
			assertThat(result, notNullValue());
			System.err.println("test1 Response: " + result);
			assertThat(result.has("result"), is(true));
			result2 = result.getJSONObject("result");
			// NOTE: test needs to be run in visible browser
			for (String field : Arrays.asList(new String[] { "type", "value" })) {
				assertThat(result2.has(field), is(true));
			}
			assertThat(result2.getString("value"), is("Downloads"));
			data = gson.fromJson(result2.toString(), Map.class);

			for (String field : Arrays.asList(new String[] { "type", "value" })) {
				assertThat(data, hasKey(field));
			}
			assertThat((String) data.get("value"), is("Downloads"));
			System.err.println("Result value: " + (String) data.get("value"));
		} catch (WebSocketException | IOException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test
	public void test2() throws MessageTimeOutException {
		// Arrange
		try {
			URL = "http://watir.com/examples/shadow_dom.html";
			driver.navigate().to(URL);
			CDPClient.sendMessage(MessageBuilder.buildEnableRuntimeMessage());
			expression = "document.querySelector('#shadow_host').shadowRoot.children";
			id = Utils.getInstance().getDynamicID();
			CDPClient.sendMessage(
					MessageBuilder.buildRuntimeEvaluateMessage(id, expression, false));
			CDPClient.setMaxRetry(10);
			// CDPClient.setDebug(true);
			responseMessage = CDPClient.getResponseMessage(id, null);
			CDPClient.setDebug(false);
			// Assert
			result = new JSONObject(responseMessage);
			assertThat(result, notNullValue());
			System.err.println("test2 Response: " + result);
			assertThat(result.has("result"), is(true));
			result2 = result.getJSONObject("result");
			data = gson.fromJson(result2.toString(), Map.class);
			assertThat(data, hasKey("className"));
			assertThat((String) data.get("className"), is("HTMLCollection"));
			System.err.println("Result class: " + (String) data.get("className"));

			expression = "document.querySelector('#shadow_host').shadowRoot.querySelector('#shadow_content').textContent";
			id = Utils.getInstance().getDynamicID();
			CDPClient.sendMessage(
					MessageBuilder.buildRuntimeEvaluateMessage(id, expression, false));
			CDPClient.setMaxRetry(10);
			// CDPClient.setDebug(true);
			responseMessage = CDPClient.getResponseMessage(id, null);
			CDPClient.setDebug(false);
			// Assert
			result = new JSONObject(responseMessage);
			assertThat(result, notNullValue());
			System.err.println("test2 Response: " + result);
			assertThat(result.has("result"), is(true));
			result2 = result.getJSONObject("result");
			data = gson.fromJson(result2.toString(), Map.class);
			for (String field : Arrays.asList(new String[] { "type", "value" })) {
				assertThat(data, hasKey(field));
			}
			assertThat((String) data.get("value"), is("some text"));
			System.err.println("Result value: " + (String) data.get("value"));

		} catch (WebSocketException | IOException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	// @Ignore
	@Test
	public void test3() throws MessageTimeOutException {
		URL = "https://podoq.ru";
		driver.get(URL);
		element = uiUtils.findElement(
				By.cssSelector("#header > a > div.logo__title"), flexibleWait);
		assertThat(element, notNullValue());
		System.err.println(driver.getPageSource());
		elements = driver.findElements(By.xpath("//div[contains(@id, 'yandex')]"));
		elements.stream().forEach(o -> System.err
				.println(String.format("id:" + "\"%s\"", o.getAttribute("id"))));

		element = driver
				.findElement(By.xpath("//div[@id='yandex_rtb_R-A-2602770-3']"));
		// assertThat(element, notNullValue());
		// assertThat(element.isDisplayed(), is(true));
		System.err.println("test3 (1): " + element.getAttribute("outerHTML"));

		element = driver.findElement(By.cssSelector("#yandex_rtb_R-A-2602770-3"));
		assertThat(element, notNullValue());
		// element is NOT dislayed
		// assertThat(element.isDisplayed(), is(true));
		System.err.println("test3 (2): " + element.getAttribute("outerHTML"));
		try {
			expression = "document.querySelector('#yandex_rtb_R-A-2602770-3').shadowRoot";
			id = Utils.getInstance().getDynamicID();
			CDPClient.sendMessage(
					MessageBuilder.buildRuntimeEvaluateMessage(id, expression, false));
			CDPClient.setMaxRetry(10);
			CDPClient.setDebug(true);
			responseMessage = CDPClient.getResponseMessage(id, null);
			CDPClient.setDebug(false);
			// Assert
			result = new JSONObject(responseMessage);
			assertThat(result, notNullValue());
			System.err.println("test3 Runtime Evaluate Response: " + result);
			expression = "return document.querySelector('#yandex_rtb_R-A-2602770-3').shadowRoot";
			responseMessage = (String) uiUtils.executeJavaScript(expression);
			System.err.println("test3 Script execution Response: " + responseMessage);
		} catch (WebSocketException | IOException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}
}

