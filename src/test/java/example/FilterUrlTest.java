package example;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class FilterUrlTest extends BaseTest {

	protected static WebDriverWait wait;
	protected static int flexibleWait = 10;
	protected static int pollingInterval = 500;

	@Before
	public void before() throws Exception {
		// Note: 4.x
		// wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));
		wait = new WebDriverWait(driver, flexibleWait);

		wait.pollingEvery(Duration.ofMillis(pollingInterval));
		// Note: 4.x
		// wait.pollingEvery(pollingInterval, TimeUnit.MILLISECONDS);

		CDPClient.sendMessage(MessageBuilder.buildNetworkClearBrowserCache(id));
		CDPClient
				.sendMessage(MessageBuilder.buildNetworkSetCacheDisabled(id, true));
	}

	@After
	public void after() throws Exception {
		// temporatily commented
		/*
		CDPClient.sendMessage(
				MessageBuilder.buildNetworkSetBlockedURLs(id, new String[] {}));
		CDPClient.sendMessage(MessageBuilder.buildNetWorkDisableMessage(id));
		*/
	}

	// see also:
	// https://groups.google.com/a/chromium.org/g/headless-dev/c/D3tUxpzmqw8/m/sV4gNeebDAAJ
	// https://securityboulevard.com/2018/09/intercepting-and-modifying-responses-with-chrome-via-the-devtools-protocol/
	@Ignore
	@Test
	public void test1() {
		List<String> urls = Arrays
				.asList(new String[] { "https://openx.software-testing.ru/*" });
		try {
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkSetBlockedURLs(id, urls));
			driver.get(
					"https://software-testing.ru/forum/index.php?/forum/129-selenium-functional-testing/");
			utils.waitFor(10);
			uiUtils.takeScreenShot();
			System.err.println("Sreenshot path: " + uiUtils.getImagePath());
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
		}
	}

	// not working
	@Ignore
	@Test
	public void test2() {
		List<String> urls = Arrays.asList(new String[] { "*.js" });
		try {
			CDPClient.setDebug(true);
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkSetBlockedURLs(id, urls));
			CDPClient.sendMessage(MessageBuilder.buildClearBrowserCacheMessage(id));
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkSetCacheDisabled(id, true));
			CDPClient.setDebug(false);
			driver.get("http://juliemr.github.io/protractor-demo/");
			System.err.println("Locating the Angular-backed element");
			WebElement element = wait.until(ExpectedConditions.visibilityOf(
					driver.findElement(By.cssSelector("body > div > div > form > h2"))));
			assertThat(element.getText(), containsString("{{latest}}"));
			System.err.println(element.getText());
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
		}
	}

	// not working
	@Ignore
	@Test
	public void test3() {
		String[] urls = new String[] { "*.js" };
		try {
			CDPClient.setDebug(true);
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkSetBlockedURLs(id, urls));
			CDPClient.sendMessage(MessageBuilder.buildClearBrowserCacheMessage(id));
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkSetCacheDisabled(id, true));
			CDPClient.setDebug(false);
			driver.get("http://juliemr.github.io/protractor-demo/");
			System.err.println("Locating the Angular-backed element");
			WebElement element = wait.until(ExpectedConditions.visibilityOf(
					driver.findElement(By.cssSelector("body > div > div > form > h2"))));
			assertThat(element.getText(), containsString("{{latest}}"));
			System.err.println(element.getText());
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
		}
	}

	@Test
	public void test4() {
		List<String> urls = Arrays.asList(
				new String[] { "*.css", "*.png", "*.jpg", "*.gif", "*favicon.ico" });
		try {
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkSetBlockedURLs(id, urls));
			driver.get("http://www.wikipedia.org");
			utils.waitFor(10);
			uiUtils.takeScreenShot();
			System.err.println("Sreenshot path: " + uiUtils.getImagePath());
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
		}
	}
}
