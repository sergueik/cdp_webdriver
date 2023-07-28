package example;

/**
 * Copyright 2020,2021,2023 Serguei Kouzmine
 */

// example usage: 
// mvn -Dtrace_id=%TRACEID% -Dversion=01 -Dparent_id=%PARENTID% test
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;
import example.utils.Utils;

public class CustomHeadersTest extends BaseTest {
	private String URL = "https://manytools.org/http-html-text/http-request-headers/";
	/* "http://127.0.0.1:8080/demo/Demo"*/

	// traceparent: 00-0af7651916cd43dd8448eb211c80319c-b9c7c989f97918e1-01
	private String customHeaderName = System.getProperty("header", "traceparent");
	private final String version = System.getProperty("version", "00");
	private final String trace_id = System.getProperty("trace_id",
			"0af7651916cd43dd8448eb211c80319c");
	private final String parent_id = System.getProperty("parent_id",
			"b9c7c989f97918e1");
	private final String trace_flags = System.getProperty("trace_flags", "01");
	private String text;
	private WebElement element;
	private List<WebElement> elements;
	public int flexibleWait = 60; // too long
	public int implicitWait = 1;
	public int pollingInterval = 500;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(false);
		super.beforeTest();

	}

	// @Ignore
	@Test
	public void test1()
			throws IOException, WebSocketException, InterruptedException {

		WebDriverWait wait = new WebDriverWait(driver, flexibleWait);
		wait.pollingEvery(Duration.ofMillis((int) pollingInterval));

		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		// https://www.w3.org/TR/trace-context/

		CDPClient.sendMessage(MessageBuilder.buildNetWorkSetExtraHTTPHeadersMessage(

				id, "traceparent", String.format("%s-%s-%s-%s", version, trace_id,
						parent_id, trace_flags)));
		// this.getClass().getSimpleName()
		driver.navigate().to(URL);
		// utils.sleep(1);
		element = wait.until(ExpectedConditions.visibilityOf(driver
				.findElement(By.cssSelector("#maincontent > div.middlecol > table"))));
		uiUtils.takeScreenShot();
		text = element.getText();
		assertThat(text, containsString(capitalize(customHeaderName)));
		// actions do not help
		Actions actions = new Actions(driver);
		actions.moveToElement(element).build().perform();
		try {
			actions.moveByOffset(0, 100).build().perform();
		} catch (MoveTargetOutOfBoundsException e) {
			System.err.println("EXception (ignored):" + e.toString());
		}
		// actions.moveToElement(element, 0, 100).build().perform();
		scroll(0, element.getLocation().getY() + 100);
		System.err.println("Headers rendered:\n" + text);
		elements = element.findElements(By.xpath(String.format(
				"tbody/tr/td[contains(text(), '%s')]/following-sibling::td",
				capitalize(customHeaderName))));
		assertThat(elements, notNullValue());
		System.err.println("Header rendered:\n" + elements.get(0).getText());
		// utils.highlight(elements.get(0));
		utils.sleep(3);

	}

	// see also: https://github.com/SeleniumHQ/selenium/issues/12162
	// https://stackoverflow.com/questions/71668952/how-to-set-user-agent-client-hint-sec-ch-ua-in-selenium-python
	@Test
	public void test2()
			throws IOException, WebSocketException, InterruptedException {

		WebDriverWait wait = new WebDriverWait(driver, flexibleWait);
		wait.pollingEvery(Duration.ofMillis((int) pollingInterval));

		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		Map<String, String> extraHeaders = new HashMap<>();
		// NOTE the case
		extraHeaders.put("Sec-Ch-Ua",
				"\"Not_A Brand\";v=\"42\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"");
		extraHeaders.put("Sec-Ch-Ua-Arch", "x86");
		extraHeaders.put("Sec-Ch-Ua-Platform", "Windows");
		String[] headers = new String[extraHeaders.keySet().size()];
		extraHeaders.keySet().toArray(headers);
		customHeaderName = headers[new Random((new Date()).getTime())
				.nextInt(extraHeaders.keySet().size())];
		CDPClient.sendMessage(MessageBuilder
				.buildNetWorkSetExtraHTTPHeadersMessage(id, extraHeaders));
		driver.navigate().to(URL);
		// utils.sleep(1);
		element = wait.until(ExpectedConditions.visibilityOf(driver
				.findElement(By.cssSelector("#maincontent > div.middlecol > table"))));
		uiUtils.takeScreenShot();
		text = element.getText();
		assertThat(text, containsString(customHeaderName));
		// actions do not help
		Actions actions = new Actions(driver);
		actions.moveToElement(element).build().perform();
		try {
			actions.moveByOffset(0, 100).build().perform();
		} catch (MoveTargetOutOfBoundsException e) {
			System.err.println("EXception (ignored):" + e.toString());
		}
		// actions.moveToElement(element, 0, 100).build().perform();
		scroll(0, element.getLocation().getY() + 100);
		System.err.println("Headers rendered:\n" + text);
		elements = element.findElements(By.xpath(String.format(
				"tbody/tr/td[contains(text(), '%s')]/following-sibling::td",
				customHeaderName)));
		utils.sleep(13);
		assertThat(elements, notNullValue());
		String text = elements.get(0).getText();
		System.err.println("Header rendered:\n" + text);
		System.err.println("Verified: " + customHeaderName);
		// bad locator - shows next property
		// assertThat(text, containsString(extraHeaders.get(customHeaderName)));
		utils.sleep(3);

	}

	// Scroll
	public void scroll(final int x, final int y) {
		final JavascriptExecutor js = (JavascriptExecutor) driver;
		for (int i = 0; i <= x; i = i + 50) {
			js.executeScript("scroll(" + i + ",0)");
		}
		for (int j = 0; j <= y; j = j + 50) {
			js.executeScript("scroll(0," + j + ")");
		}
	}

	private static String capitalize(final String data) {
		if (data == null || data.equals(""))
			throw new NullPointerException("string");
		return Character.toUpperCase(data.charAt(0)) + data.substring(1);
	}
}
