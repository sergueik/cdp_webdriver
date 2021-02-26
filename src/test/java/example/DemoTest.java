package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
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
	private static By locator = null;
	private static WebElement element = null;
	private static WebDriverWait wait;

	@Ignore
	@Test
	public void setUserAgentOverrideTest() {
		// Arrange
		driver.get("https://www.whoishostingthis.com/tools/user-agent/");
		By locator = By.cssSelector("div.user-agent");
		WebElement element = driver.findElement(locator);
		assertThat(element.getAttribute("innerText"), containsString("Mozilla"));
		// Act
		try {
			CDPClient.sendMessage(MessageBuilder.buildSetUserAgentOverrideMessage("python 2.7", "windows"));
		} catch (IOException | WebSocketException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		}
		driver.navigate().refresh();
		utils.sleep(1);

		element = driver.findElement(locator);
		assertThat(element.isDisplayed(), is(true));
		assertThat(element.getAttribute("innerText"), is("python 2.7"));

	}

	@Test
	public void getBroswerVersionTest() {
		// Arrange
		// Act
		try {
			CDPClient.sendMessage(MessageBuilder.buildBrowserVersionMessage(id));
			responseMessage = CDPClient.getResponseDataMessage(id);
			// Assert
			result = new JSONObject(responseMessage);
			for (String field : Arrays
					.asList(new String[] { "protocolVersion", "product", "revision", "userAgent", "jsVersion" })) {
				assertThat(result.has(field), is(true));
			}
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}

	@Test(expected = example.messaging.CDPClient.MessageTimeOutException.class)
	public void getRuntimeEvaluateTest() throws MessageTimeOutException {
		// Arrange
		int id1 = Utils.getInstance().getDynamicID();
		// Act
		try {
			// System.err.println("getRuntimeEvaluateTest() message: "
			// + MessageBuilder.buildEnableRuntimeMessage(id1));
			CDPClient.sendMessage(MessageBuilder.buildEnableRuntimeMessage(id1));

			// System.err.println("getRuntimeEvaluateTest() message: " +
			// MessageBuilder
			// .buildRuntimeEvaluateMessage(id1, "var x = 42; x;", false));
			CDPClient.sendMessage(MessageBuilder.buildRuntimeEvaluateMessage(id1, "var x = 42; x;", false));
			responseMessage = CDPClient.getResponseDataMessage(id1);
			// Assert
			result = new JSONObject(responseMessage);
			System.err.println("getRuntimeEvaluateTest Response: " + result);
			for (String field : Arrays
					.asList(new String[] { "protocolVersion", "product", "revision", "userAgent", "jsVersion" })) {
				assertThat(result.has(field), is(true));
			}
		} catch (WebSocketException | IOException | InterruptedException e) {
			System.err.println("Exception in getRuntimeEvaluateTest() (ignored): " + e.toString());
		} catch (MessageTimeOutException e) {
			throw (e);
		}
	}

	// see also: https://habr.com/ru/post/185092/
	// https://github.com/dunxrion/console.image
	@Ignore
	@Test
	// TODO: "Location Unavailable" test
	// Omitting any of the parameters in Emulation.setGeolocationOverride
	// emulates position unavailable
	// see also: https://habr.com/ru/post/518862/
	public void doFakeGeoLocation() throws IOException, WebSocketException, InterruptedException {
		CDPClient.sendMessage(MessageBuilder.buildGeoLocationMessage(id, 37.422290, -122.084057));
		// google HQ
		utils.sleep(3);
		URL = "https://www.google.com.sg/maps";
		driver.navigate().to(URL);
		uiUtils.findElement(By.cssSelector("div[class *='widget-mylocation-button-icon-common']"), 120).click();
		utils.waitFor(10);
		uiUtils.takeScreenShot();
	}

	// NOTE: for doNetworkTracking, need to switch to headless, e.g.
	// via setting BaseTest property and invoking super.beforeTest() explicitly
	// @Before
	// public void beforeTest() {
	// }

	@Ignore
	@Test
	public void doNetworkTracking() throws IOException, WebSocketException, InterruptedException {
		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		URL = "http://petstore.swagger.io/v2/swagger.json";
		driver.navigate().to(URL);
		utils.waitFor(3);
		responseMessage = CDPClient.getResponseMessage("Network.requestWillBeSent");
		result = new JSONObject(responseMessage);
		String reqId = result.getJSONObject("params").getString("requestId");
		int id2 = Utils.getInstance().getDynamicID();
		CDPClient.sendMessage(MessageBuilder.buildGetResponseBodyMessage(id2, reqId));
		String networkResponse = CDPClient.getResponseBodyMessage(id2);
		System.err.println("Here is the network Response: " + networkResponse);
		utils.waitFor(1);
		uiUtils.takeScreenShot();
	}

	@Ignore
	@Test
	public void doResponseMocking() throws Exception {
		CDPClient.sendMessage(MessageBuilder.buildRequestInterceptorPatternMessage(id, "*", "Document"));
		CDPClient.mockResponse("This is mocked!!!");
		URL = "http://petstore.swagger.io/v2/swagger.json";
		driver.navigate().to(URL);
		utils.sleep(3);
	}

	@Ignore
	@Test
	public void doFunMocking() throws IOException, WebSocketException {
		byte[] fileContent = FileUtils
				.readFileToByteArray(new File(System.getProperty("user.dir") + "/data/durian.png"));
		String encodedString = Base64.getEncoder().encodeToString(fileContent);
		CDPClient.sendMessage(MessageBuilder.buildRequestInterceptorPatternMessage(id, "*", "Image"));
		CDPClient.mockFunResponse(encodedString);
		URL = "https://sg.carousell.com/";
		driver.navigate().to(URL);
		utils.sleep(300);
	}

	@Ignore
	@Test
	public void doClearSiteData() throws Exception {
		URL = "https://framework.realtime.co/demo/web-push";
		driver.navigate().to(URL);
		driver.manage().deleteAllCookies();
		CDPClient.sendMessage(MessageBuilder.buildClearBrowserCookiesMessage(id));
		CDPClient.sendMessage(MessageBuilder.buildClearDataForOriginMessage(id, "https://framework.realtime.co"));
		utils.sleep(3);
	}

	// Page.handleJavaScriptDialog

	@Ignore
	@Test
	public void doElementScreenshot() throws Exception {
		URL = "https://www.google.com/";
		driver.navigate().to(URL);
		WebElement logo = uiUtils.findElement(By.cssSelector("img#hplogo"), 5);
		int x = logo.getLocation().getX();
		int y = logo.getLocation().getY();
		int width = logo.getSize().getWidth();
		int height = logo.getSize().getHeight();
		int scale = 1;
		CDPClient.sendMessage(MessageBuilder.buildTakeElementScreenShotMessage(id, x, y, height, width, scale));
		responseMessage = CDPClient.getResponseDataMessage(id);
		byte[] bytes = Base64.getDecoder().decode(responseMessage);
		File f = new File(System.getProperty("user.dir") + "/target/img.png");
		if (f.exists())
			f.delete();
		System.err.println("Saving screenshot.");
		Files.write(f.toPath(), bytes);
		// uiUtils.takeScreenShot();
	}

	// @Ignore
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
			responseMessage = CDPClient.getResponseBodyMessage(id);
			// TODO: assertNull
		} catch (RuntimeException e) {
			System.err.println("Exception (ignored): " + e.toString());
			// No message received
			// TODO: discover
			// {"error":{"code":-32000,"message":"PrintToPDF is not implemented"}}
			assertThat(e.toString(), containsString("No message received"));
		}
	}

	// @Ignore
	@Test(expected = example.messaging.CDPClient.MessageTimeOutException.class)
	public void doFullPageScreenshot() throws Exception {
		URL = "https://www.meetup.com/";
		driver.navigate().to(URL);
		long docWidth = (long) uiUtils.executeJavaScript("return document.body.offsetWidth");
		long docHeight = (long) uiUtils.executeJavaScript("return document.body.offsetHeight");
		int scale = 1;
		System.err.println("doFullPageScreenshot() message: "
				+ MessageBuilder.buildTakeElementScreenShotMessage(id, 0, 0, docHeight, docWidth, scale));
		CDPClient.sendMessage(MessageBuilder.buildTakeElementScreenShotMessage(id, 0, 0, docHeight, docWidth, scale));
		responseMessage = CDPClient.getResponseDataMessage(id);
		byte[] bytes = Base64.getDecoder().decode(responseMessage);
		String start_time = (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date());
		String imageName = "cdp_img_" + start_time + ".png";
		File f = new File(System.getProperty("user.dir") + "/target/" + imageName);
		if (f.exists())
			f.delete();
		Files.write(f.toPath(), bytes);
		uiUtils.takeScreenShot();
	}

	@Ignore
	@Test
	public void doServiceWorkerTesting() throws Exception {
		URL = "https://www.meetup.com/";
		CDPClient.sendMessage(MessageBuilder.buildServiceWorkerEnableMessage(id));
		driver.navigate().to(URL);
		ServiceWorker serviceWorker = CDPClient.getServiceWorker(URL, 10, "activated");
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
		ServiceWorker serviceWorker = CDPClient.getServiceWorker(URL, 5, "activated");
		int id1 = Utils.getInstance().getDynamicID();
		int id2 = Utils.getInstance().getDynamicID();

		CDPClient.sendMessage(MessageBuilder.buildEnableLogMessage(id1));
		CDPClient.sendMessage(MessageBuilder.buildEnableRuntimeMessage(id2));

		CDPClient.sendMessage(MessageBuilder.buildServiceWorkerInspectMessage(id2, serviceWorker.getVersionId()));
		WebElement elem = uiUtils.findElement(By.cssSelector("button#sendButton"), 3);
		uiUtils.scrollToElement(elem);
		elem.click();
		utils.sleep(3);
	}

	@Test
	public void getPerformanceMetricsTest() {
		try {
			CDPClient.sendMessage(MessageBuilder.buildSetTimeDomainMessage(id, "threadTicks"));
			System.err.println("SetTimeDomain called");
			CDPClient.sendMessage(MessageBuilder.buildPerformanceEnableMessage(id));
			System.err.println("PerformanceEnable called");
			driver.get("https://www.wikipedia.org");
			CDPClient.sendMessage(MessageBuilder.buildPerformanceGetMetrics(id));
			responseMessage = CDPClient.getResponseMessage(id, "metrics");
			System.err.println("performanceMetricsTest response: " + responseMessage);
			// byte[] bytes = Base64.getDecoder().decode(responseMessage);
			CDPClient.sendMessage(MessageBuilder.buildPerformanceDisableMessage(id));
		} catch (WebDriverException | IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("performanceMetricsTest Exception in ??? (ignored): " + e.getMessage());
		}
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
			headers.put("Authorization", "Basic " + new String(
					Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes())));
			CDPClient.sendMessage(MessageBuilder.buildNetWorkSetExtraHTTPHeadersMessage(id, headers));
			// Declare a wait time
			final int flexibleWait = 60;
			final int pollingInterval = 500;
			final int scriptTimeout = 5;
			driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);

			wait = new WebDriverWait(driver, flexibleWait);

			// NOTE: constructor WebDriverWait(WebDriver, Duration) is undefined
			// with Selenium 3.x ?
			// wait = new WebDriverWait(driver, Duration.ofSeconds(flexibleWait));

			// Selenium Driver version sensitive code: 3.13.0 vs. 3.8.0 and older
			wait.pollingEvery(Duration.ofMillis(pollingInterval));

			// Act
			element = wait.until(ExpectedConditions
					.visibilityOf(driver.findElement(By.cssSelector("table td> a[href=\"Basic/\"]"))));
			element.click();
			wait.until(ExpectedConditions.urlToBe("https://jigsaw.w3.org/HTTP/Basic/"));

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
		List<String> urls = Arrays.asList(new String[] { "https://openx.software-testing.ru/*" });
		try {
			CDPClient.sendMessage(MessageBuilder.buildNetworkClearBrowserCache(id));

			CDPClient.sendMessage(MessageBuilder.buildNetworkSetBlockedURLs(id, urls));
			driver.get("https://software-testing.ru/forum/index.php?/forum/129-selenium-functional-testing/");
			utils.waitFor(10);
			uiUtils.takeScreenShot();
			System.err.println("Sreenshot path: " + uiUtils.getImagePath());
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
		}
	}
}
