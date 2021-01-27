package example;
/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import example.messaging.CDPClient;

import example.utils.UIUtils;
import example.utils.Utils;
import example.utils.TestUtils;

public class DockerBaseTest {
	protected WebDriver driver;
	protected Utils utils;
	protected UIUtils uiUtils;
	protected CDPClient CDPClient;
	protected ChromeDriverService chromeDriverService;
	protected int id;
	private final static int debugPort = Integer.parseInt(TestUtils.getPropertyEnv("debugPort", "9222"));
	private boolean debug = true;

	public void setDebug(boolean value) {
		this.debug = value;
	}

	@Before
	public void beforeTest() throws IOException {
		utils = Utils.getInstance();
		uiUtils = UIUtils.getInstance();
		utils.setDebug(debug);
		// optionally configure to use Docker mapped log directory
		// utils.setChromeDriverLogFile("/dev/shm/chromedriver.log");
		if (debugPort != 0) {
			utils.setDebugPort(debugPort);
		}
		driver = utils.launchBrowser(true, new URL("http://127.0.0.1:4444/wd/hub"));
		CDPClient = new CDPClient(utils.getWebSocketURL());
		id = Utils.getInstance().getDynamicID();
	}

	@After
	public void afterTest() {
		if (!Objects.isNull(CDPClient))
			CDPClient.disconnect();
		utils.stopChrome();
		if (!Objects.isNull(chromeDriverService))
			chromeDriverService.stop();
	}

}
