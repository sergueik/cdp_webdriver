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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;
import example.messaging.ServiceWorker;
import example.messaging.CDPClient.MessageTimeOutException;
import example.utils.Utils;

public class HeadlessTest extends BaseTest {
	private String URL = null;
	private String responseMessage = null;
	private JSONObject result = null;
	private String imageName = null;
	private final String filePath = System.getProperty("user.dir") + "/target";

	@Before
	public void beforeTest() throws IOException {
		super.setHeadless(true);
		super.beforeTest();

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
			for (String field : Arrays.asList(new String[] { "protocolVersion",
					"product", "revision", "userAgent", "jsVersion" })) {
				assertThat(result.has(field), is(true));
			}
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	// @Ignore
	@Test
	public void doCustomHeaders()
			throws IOException, WebSocketException, InterruptedException {
		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		CDPClient.sendMessage(MessageBuilder.buildNetWorkSetExtraHTTPHeadersMessage(
				id, "customHeaderName",
				this.getClass().getSimpleName() + " " + "customHeaderValue"));
		driver.navigate().to("http://127.0.0.1:8080/demo/Demo");
	}

	@Test
	public void doNetworkTracking()
			throws IOException, WebSocketException, InterruptedException {
		CDPClient.sendMessage(MessageBuilder.buildNetWorkEnableMessage(id));
		URL = "http://petstore.swagger.io/v2/swagger.json";
		driver.navigate().to(URL);
		utils.sleep(3);
		responseMessage = CDPClient.getResponseMessage("Network.requestWillBeSent");
		result = new JSONObject(responseMessage);
		String reqId = result.getJSONObject("params").getString("requestId");
		int id2 = Utils.getInstance().getDynamicID();
		CDPClient
				.sendMessage(MessageBuilder.buildGetResponseBodyMessage(id2, reqId));
		String networkResponse = CDPClient.getResponseBodyMessage(id2);
		System.err.println("Here is the network Response: " + networkResponse);
		// utils.sleep(1);
		// uiUtils.takeScreenShot();
	}

	@Test
	public void doprintPDF() throws Exception {
		URL = "https://www.wikipedia.com/";
		imageName = "cdp_img_"
				+ (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date())
				+ ".pdf";

		driver.navigate().to(URL);
		CDPClient.sendMessage(MessageBuilder.buildPrintPDFMessage(id));
		// responseMessage = CDPClient.getResponseBodyMessage(id);
		// TODO: assertNull
		responseMessage = CDPClient.getResponseDataMessage(id);
		System.err.println("Here is the base 64 PDF response: "
				+ responseMessage.substring(0, 100));
		byte[] bytes = Base64.getDecoder().decode(responseMessage);
		File f = new File(filePath + "/" + imageName);
		if (f.exists())
			f.delete();
		Files.write(f.toPath(), bytes);
	}

	@Test
	public void getPerformanceMetricsHeadlessTest() {
		try {

			CDPClient.sendMessage(MessageBuilder.buildPerformancEnableMessage(id));
			CDPClient.sendMessage(
					MessageBuilder.buildSetTimeDomainMessage(id, "threadTicks"));
			driver.get("https://www.wikipedia.org");
			int id2 = Utils.getInstance().getDynamicID();
			CDPClient.sendMessage(MessageBuilder.buildPerformancGetMetrics(id2));
			responseMessage = CDPClient.getResponseDataMessage(id2);
			System.err.println("getPerformanceMetricsHeadlessTest response: " + responseMessage);
			// byte[] bytes = Base64.getDecoder().decode(responseMessage);
			CDPClient.sendMessage(MessageBuilder.buildPerformancDisableMessage(id2));
		} catch (WebDriverException | IOException | WebSocketException
				| MessageTimeOutException | InterruptedException e) {
			System.err.println("getPerformanceMetricsHeadlessTest Exception in ??? (ignored): "
					+ e.getMessage());
			// most likely, the
			// example.messaging.CDPClient$MessageTimeOutException:
			// No message received with this id
		}
	}
}
