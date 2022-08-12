package example;

/**
 * Copyright 2020-2022 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;
import example.messaging.ServiceWorker;
import example.utils.Utils;

public class DemoTest extends BaseTest {
	private String URL = null;
	private String responseMessage = null;
	private JSONObject result = null;
	private static WebElement element = null;
	private static WebDriverWait wait;
	private int id1;
	private int id2;

	@Test
	// @Test(expected = example.messaging.CDPClient.MessageTimeOutException.class)
	public void getRuntimeEvaluateTest() throws MessageTimeOutException {
		// Arrange
		try {
			CDPClient.sendMessage(MessageBuilder.buildEnableRuntimeMessage(id));

			CDPClient.sendMessage(MessageBuilder.buildRuntimeEvaluateMessage(id,
					"var x = 42; x;", false));
			CDPClient.setMaxRetry(10);
			CDPClient.setDebug(true);
			responseMessage = CDPClient.getResponseMessage(id, null);
			CDPClient.setDebug(false);
			// Assert
			result = new JSONObject(responseMessage);
			System.err.println("getRuntimeEvaluateTest Response: " + result);
		} catch (WebSocketException | IOException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	// NOTE: for doNetworkTracking, need to switch to headless, e.g.
	// via setting BaseTest property and invoking super.beforeTest() explicitly
	// @Before
	// public void beforeTest() {
	// }

	@Ignore
	@Test
	public void doNetworkTracking()
			throws IOException, WebSocketException, InterruptedException {
		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		URL = "http://petstore.swagger.io/v2/swagger.json";
		driver.navigate().to(URL);
		utils.waitFor(3);
		responseMessage = CDPClient.getResponseMessage("Network.requestWillBeSent");
		result = new JSONObject(responseMessage);
		String reqId = result.getJSONObject("params").getString("requestId");
		int id2 = Utils.getInstance().getDynamicID();
		CDPClient
				.sendMessage(MessageBuilder.buildGetResponseBodyMessage(id2, reqId));
		String networkResponse = CDPClient.getResponseBodyMessage(id2);
		System.err.println("Here is the network Response: " + networkResponse);
		utils.waitFor(1);
		uiUtils.takeScreenShot();
	}

	@Ignore
	@Test
	public void doResponseMocking() throws Exception {
		CDPClient.sendMessage(MessageBuilder
				.buildRequestInterceptorPatternMessage(id, "*", "Document"));
		CDPClient.mockResponse("This is mocked!!!");
		URL = "http://petstore.swagger.io/v2/swagger.json";
		driver.navigate().to(URL);
		utils.sleep(3);
	}

	@Ignore
	@Test
	public void doFunMocking() throws IOException, WebSocketException {
		byte[] fileContent = FileUtils.readFileToByteArray(
				new File(System.getProperty("user.dir") + "/data/durian.png"));
		String encodedString = Base64.getEncoder().encodeToString(fileContent);
		CDPClient.sendMessage(
				MessageBuilder.buildRequestInterceptorPatternMessage(id, "*", "Image"));
		CDPClient.mockFunResponse(encodedString);
		URL = "https://sg.carousell.com/";
		driver.navigate().to(URL);
		utils.sleep(3);
	}

	@Ignore
	@Test
	public void doClearSiteData() throws Exception {
		URL = "https://framework.realtime.co/demo/web-push";
		driver.navigate().to(URL);
		driver.manage().deleteAllCookies();
		CDPClient.sendMessage(MessageBuilder.buildClearBrowserCookiesMessage(id));
		CDPClient.sendMessage(MessageBuilder.buildClearDataForOriginMessage(id,
				"https://framework.realtime.co"));
		utils.sleep(3);
	}

	// Page.handleJavaScriptDialog

	@Ignore
	// need to run CDPClient in debug mode for this test
	// No message received
	// {"error":{"code":-32000,"message":"PrintToPDF is not implemented"}}
	// @Test(expected = example.messaging.CDPClient.MessageTimeOutException.class)
	@Test
	public void doprintPDF() throws Exception {
		URL = "https://www.wikipedia.com/";
		driver.navigate().to(URL);
		try {
			CDPClient.sendMessage(MessageBuilder.buildPrintPDFMessage(id));
			System.err.println("Reading response of PrintPDF");
			//
			responseMessage = CDPClient.getResponseMessage(id, null);
			// TODO: assertNull
		} catch (RuntimeException e) {
			System.err.println("Exception (ignored): " + e.toString());
			// No message received
			// TODO: discover
			// {"error":{"code":-32000,"message":"PrintToPDF is not implemented"}}
			assertThat(e.toString(), containsString("No message received"));
		}
	}

	@Ignore
	@Test
	public void doServiceWorkerTesting() throws Exception {
		URL = "https://www.meetup.com/";
		CDPClient.sendMessage(MessageBuilder.buildServiceWorkerEnableMessage(id));
		driver.navigate().to(URL);
		ServiceWorker serviceWorker = CDPClient.getServiceWorker(URL, 10,
				"activated");
		System.out.println(serviceWorker.toString());
		Assert.assertEquals(serviceWorker.getStatus(), "activated");
	}

	@Ignore
	@Test
	public void doPushNotificationTesting() throws Exception {
		URL = "https://framework.realtime.co/demo/web-push";
		CDPClient.sendMessage(MessageBuilder.buildServiceWorkerEnableMessage(id));
		driver.navigate().to(URL);
		utils.sleep(5);
		ServiceWorker serviceWorker = CDPClient.getServiceWorker(URL, 5,
				"activated");
		id1 = Utils.getInstance().getDynamicID();
		id2 = Utils.getInstance().getDynamicID();

		CDPClient.sendMessage(MessageBuilder.buildEnableLogMessage(id1));
		CDPClient.sendMessage(MessageBuilder.buildEnableRuntimeMessage(id2));

		CDPClient.sendMessage(MessageBuilder.buildServiceWorkerInspectMessage(id2,
				serviceWorker.getVersionId()));
		WebElement elem = uiUtils.findElement(By.cssSelector("button#sendButton"),
				3);
		uiUtils.scrollToElement(elem);
		elem.click();
		utils.sleep(3);
	}

	// https://en.wikipedia.org/wiki/Basic_access_authentication
	// https://examples.javacodegeeks.com/core-java/apache/commons/codec/binary/base64-binary/org-apache-commons-codec-binary-base64-example/
	@Test
	public void basicAuthenticationTest() {

		final String username = "guest";
		final String password = "guest";
		driver.get("https://jigsaw.w3.org/HTTP/");
		try {
			CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));

			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization",
					"Basic " + new String(Base64.getEncoder().encodeToString(
							String.format("%s:%s", username, password).getBytes())));
			CDPClient.sendMessage(
					MessageBuilder.buildNetWorkSetExtraHTTPHeadersMessage(id, headers));
			// Declare a wait time
			final int flexibleWait = 60;
			final int pollingInterval = 500;
			final int scriptTimeout = 5;
			driver.manage().timeouts().setScriptTimeout(scriptTimeout,
					TimeUnit.SECONDS);

			wait = new WebDriverWait(driver, flexibleWait);

			// NOTE: constructor WebDriverWait(WebDriver, Duration) is undefined
			// with Selenium 3.x ?
			// wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));

			// Selenium Driver version sensitive code: 3.13.0 vs. 3.8.0 and older
			wait.pollingEvery(Duration.ofMillis(pollingInterval));

			// Act
			element = wait.until(ExpectedConditions.visibilityOf(
					driver.findElement(By.cssSelector("table td> a[href=\"Basic/\"]"))));
			element.click();
			wait.until(
					ExpectedConditions.urlToBe("https://jigsaw.w3.org/HTTP/Basic/"));

			element = driver.findElement(By.tagName("body"));
			assertThat("get past authentication", element.getAttribute("innerHTML"),
					containsString("Your browser made it!"));
			utils.sleep(3);
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
		}
	}

	// see also:
	// https://groups.google.com/a/chromium.org/g/headless-dev/c/D3tUxpzmqw8/m/sV4gNeebDAAJ
	// https://securityboulevard.com/2018/09/intercepting-and-modifying-responses-with-chrome-via-the-devtools-protocol/
	@Test
	public void getBlockUrlsTest() {
		List<String> urls = Arrays
				.asList(new String[] { "https://openx.software-testing.ru/*" });
		try {
			CDPClient.sendMessage(MessageBuilder.buildNetworkClearBrowserCache(id));

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
}
