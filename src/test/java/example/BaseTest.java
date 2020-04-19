package example;

import java.io.IOException;
import java.util.Objects;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import example.messaging.CDPClient;

import example.utils.UIUtils;
import example.utils.Utils;
import example.utils.TestUtils;

public class BaseTest {
	protected static WebDriver driver;
	protected static Utils utils;
	protected static UIUtils uiUtils;
	protected static CDPClient CDPClient;
	protected int id;
	private final static int debugPort = Integer
			.parseInt(TestUtils.getPropertyEnv("debugPort", "0"));
	// used to override the default port 9222
	@SuppressWarnings("unused")
	private static boolean debug = false;
	private static boolean headless = false;
	protected static ChromeDriverService chromeDriverService;

	public void setDebug(boolean value) {
		BaseTest.debug = value;
	}

	public void setHeadless(boolean value) {
		BaseTest.headless = value;
	}


	@BeforeClass
	public static void beforeClass() throws IOException {
		utils = Utils.getInstance();
		uiUtils = UIUtils.getInstance();
		utils.setDebug(debug);
		if (debugPort != 0) {
			utils.setDebugPort(debugPort);
		}
		driver = utils.launchBrowser(headless);
		CDPClient = new CDPClient(utils.getWebSocketURL());
	}

	@Before
	public void beforeTest() throws IOException {
		id = Utils.getInstance().getDynamicID();
	}

	@After
	public void afterTest() {
		driver.navigate().to("about:blank");
	}

	@AfterClass
	public static void afterClass() {
		if (!Objects.isNull(CDPClient))
			CDPClient.disconnect();
		utils.stopChrome();
		if (!Objects.isNull(chromeDriverService))
			chromeDriverService.stop();
	}

}
