package example;

import java.io.IOException;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import example.messaging.CDPClient;
import example.utils.UIUtils;
import example.utils.Utils;

public class BaseTest {
	protected WebDriver driver;
	protected Utils utils;
	protected UIUtils uiUtils;
	protected CDPClient CDPClient;
	protected int id;
	private final static int debugPort = Integer
			.parseInt(getPropertyEnv("debugPort", "0"));
	@SuppressWarnings("unused")
	private boolean debug = false;
	private boolean headless = false;

	public void setDebug(boolean value) {
		this.debug = value;
	}

	public void setHeadless(boolean value) {
		this.headless = value;
	}

	protected ChromeDriverService chromeDriverService;

	@Before
	public void beforeTest() throws IOException {
		utils = Utils.getInstance();
		uiUtils = UIUtils.getInstance();
		utils.setDebug(debug);
		if (debugPort != 0) {
			utils.setDebugPort(debugPort);
		}
		driver = utils.launchBrowser(headless);
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

	public static String getPropertyEnv(String name, String defaultValue) {
		String value = System.getProperty(name);
		if (value == null || value.length() == 0) {
			value = System.getenv(name);
			if (value == null || value.length() == 0) {
				value = defaultValue;
			}
		}
		return value;
	}

}
