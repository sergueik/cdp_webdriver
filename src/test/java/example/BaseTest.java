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
		this.utils = Utils.getInstance();
		this.uiUtils = UIUtils.getInstance();

		this.driver = utils.launchBrowser(headless);
		this.CDPClient = new CDPClient(utils.getWebSocketURL());
		this.id = Utils.getInstance().getDynamicID();

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
