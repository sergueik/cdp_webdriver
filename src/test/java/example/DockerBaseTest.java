package example;

/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import example.messaging.CDPClient;

import example.utils.UIUtils;
import example.utils.Utils;
import example.utils.TestUtils;

// the Docker tests need Docker image to be deployed to and run on
// https://github.com/sergueik/springboot_study/tree/master/basic-chromium
public class DockerBaseTest {
	protected WebDriver driver;
	protected Utils utils;
	protected UIUtils uiUtils;
	protected CDPClient CDPClient;
	protected ChromeDriverService chromeDriverService;
	protected int id;
	private final static int debugPort = Integer
			.parseInt(TestUtils.getPropertyEnv("debugPort", "9222"));
	private boolean debug = true;

	public void setDebug(boolean value) {
		this.debug = value;
	}

	@Before
	public void beforeTest() throws IOException {

		utils = Utils.getInstance();
		uiUtils = UIUtils.getInstance();
		utils.setDebug(debug);
		Assume.assumeTrue(pingHost("127.0.0.1", 4444, 3));
		// optionally configure to use Docker mapped log directory
		// utils.setChromeDriverLogFile("/dev/shm/chromedriver.log");
		if (debugPort != 0) {
			utils.setDebugPort(debugPort);
		}
		if (pingHost("127.0.0.1", 4444, 3)) {
			driver = utils.launchBrowser(true,
					new URL("http://127.0.0.1:4444/wd/hub"));
			CDPClient = new CDPClient(utils.getWebSocketURL());
			id = utils.getDynamicID();
		}
	}

	@After
	public void afterTest() {
		if (CDPClient != null)
			CDPClient.disconnect();
		if (driver != null)
			utils.stopChrome();
		if (chromeDriverService != null)
			chromeDriverService.stop();
	}

	// origin:
	// https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability
	public static boolean pingHost(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			return false; // timeout or unreachable or failed DNS lookup.
		}
	}
}
