package example;
/**
 * Copyright 2020,2021 Serguei Kouzmine
 */

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;

import example.utils.UINotificationService;
import example.utils.Utils;
import example.utils.TestUtils;

public class BaseNotificationServiceTest {
	protected WebDriver driver;
	protected Utils utils;
	private final static int debugPort = Integer
			.parseInt(TestUtils.getPropertyEnv("debugPort", "0"));
	// used to override the default port 9222

	private boolean debug = false;

	protected UINotificationService uiNotificationService;

	@Before
	public void beforeTest() throws IOException {
		this.utils = Utils.getInstance();
		utils.setDebug(debug);
		if (debugPort != 0) {
			utils.setDebugPort(debugPort);
		}
		this.driver = utils.launchBrowser();
		this.uiNotificationService = UINotificationService.getInstance(driver);
	}

	@After
	public void afterTest() {
		utils.stopChrome();
	}

}
