package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.time.Duration;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class CustomHeadersTest extends BaseTest {
	private String URL = "https://manytools.org/http-html-text/http-request-headers/";
	/* "http://127.0.0.1:8080/demo/Demo"*/

	private String text;
	private WebElement element;
	public int flexibleWait = 60; // too long
	public int implicitWait = 1;
	public int pollingInterval = 500;

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(false);
		super.beforeTest();

	}

	@Test
	public void test1()
			throws IOException, WebSocketException, InterruptedException {

		WebDriverWait wait = new WebDriverWait(driver, flexibleWait);
		wait.pollingEvery(Duration.ofMillis((int) pollingInterval));

		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		CDPClient.sendMessage(MessageBuilder.buildNetWorkSetExtraHTTPHeadersMessage(
				id, "customHeaderName",
				this.getClass().getSimpleName() + " " + "customHeaderValue"));
		driver.navigate().to(URL);
		// utils.sleep(1);
		element = wait.until(ExpectedConditions.visibilityOf(driver
				.findElement(By.cssSelector("#maincontent > div.middlecol > table"))));
		uiUtils.takeScreenShot();
		text = element.getText();
		assertThat(text, containsString("Customheadername"));
		System.err.println(text);
	}

}

