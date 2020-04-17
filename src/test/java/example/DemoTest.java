package example;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;
import example.messaging.ServiceWorker;
import example.utils.Utils;

public class DemoTest extends BaseTest {
	private String URL = null;
	private String responseMessage = null;
	private JSONObject result = null;

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

	@Ignore
	@Test
	public void doFakeGeoLocation() throws IOException, WebSocketException, InterruptedException {
		CDPClient.sendMessage(MessageBuilder.buildGeoLocationMessage(id, 37.422290, -122.084057));
		// google HQ
		utils.waitFor(3);
		URL = "https://www.google.com.sg/maps";
		driver.navigate().to(URL);
		uiUtils.findElement(By.cssSelector("div[class *='widget-mylocation-button-icon-common']"), 120).click();
		utils.waitFor(10);
		uiUtils.takeScreenShot();
	}

	@Ignore
	@Test
	public void doResponseMocking() throws Exception {
		CDPClient.sendMessage(MessageBuilder.buildRequestInterceptorPatternMessage(id, "*", "Document"));
		CDPClient.mockResponse("This is mocked!!!");
		URL = "http://petstore.swagger.io/v2/swagger.json";
		driver.navigate().to(URL);
		utils.waitFor(3);
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
		utils.waitFor(300);
	}

	@Ignore
	@Test
	public void doClearSiteData() throws Exception {
		URL = "https://framework.realtime.co/demo/web-push";
		driver.navigate().to(URL);
		driver.manage().deleteAllCookies();
		CDPClient.sendMessage(MessageBuilder.buildClearBrowserCookiesMessage(id));
		CDPClient.sendMessage(MessageBuilder.buildClearDataForOriginMessage(id, "https://framework.realtime.co"));
		utils.waitFor(3);
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

	@Ignore
	@Test
	public void doFullPageScreenshot() throws Exception {
		URL = "https://www.meetup.com/";
		driver.navigate().to(URL);
		long docWidth = (long) uiUtils.executeJavaScript("return document.body.offsetWidth");
		long docHeight = (long) uiUtils.executeJavaScript("return document.body.offsetHeight");
		int scale = 1;
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
		utils.waitFor(5);
		ServiceWorker serviceWorker = CDPClient.getServiceWorker(URL, 5, "activated");
		int id1 = Utils.getInstance().getDynamicID();
		int id2 = Utils.getInstance().getDynamicID();

		CDPClient.sendMessage(MessageBuilder.buildEnableLogMessage(id1));
		CDPClient.sendMessage(MessageBuilder.buildEnableRuntimeMessage(id2));

		CDPClient.sendMessage(MessageBuilder.buildServiceWorkerInspectMessage(id2, serviceWorker.getVersionId()));
		WebElement elem = uiUtils.findElement(By.cssSelector("button#sendButton"), 3);
		uiUtils.scrollToElement(elem);
		elem.click();
		utils.waitFor(3);
		utils.waitFor(60);
	}
}
