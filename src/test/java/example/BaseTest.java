package example;

/**
 * Copyright 2020,2021,2022 Serguei Kouzmine
 */
import java.io.IOException;
import java.util.Objects;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
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
	protected static boolean debug = false;
	protected static boolean headless = false; // false;
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
		// force the headless flag to be true to support Unix console execution
		if (!(Utils.getOsName().equals("windows"))
				&& !(System.getenv().containsKey("DISPLAY"))) {
			headless = true;
		}
		System.err.println("HEADLESS: " + headless);
		driver = utils.launchBrowser(headless);
		uiUtils.setDriver(driver);
		CDPClient = new CDPClient(utils.getWebSocketURL());
	}

	@Before
	public void beforeTest() throws IOException {
		id = utils.getDynamicID();
	}

	@After
	public void afterTest() {
		// org.openqa.selenium.WebDriverException:
		// unknown error: cannot determine
		// loading status
		driver.navigate().to("about:blank");
	}

	@AfterClass
	public static void afterClass() {
		if (!Objects.isNull(CDPClient))
			CDPClient.disconnect();
		try {
			utils.stopChrome();
		} catch (WebDriverException e) {
			// chrome not reachable
		}
		if (!Objects.isNull(chromeDriverService))
			chromeDriverService.stop();
	}

}
