package example;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class FilterUrlTest extends BaseTest {

	@Before
	public void before() throws Exception {
		CDPClient.sendMessage(MessageBuilder.buildNetworkClearBrowserCache(id));
		CDPClient
				.sendMessage(MessageBuilder.buildNetworkSetCacheDisabled(id, true));
	}

	@After
	public void after() throws Exception {
		CDPClient.sendMessage(
				MessageBuilder.buildNetworkSetBlockedURLs(id, new String[] {}));
		CDPClient.sendMessage(MessageBuilder.buildNetWorkDisableMessage(id));
	}

	// see also:
	// https://groups.google.com/a/chromium.org/g/headless-dev/c/D3tUxpzmqw8/m/sV4gNeebDAAJ
	// https://securityboulevard.com/2018/09/intercepting-and-modifying-responses-with-chrome-via-the-devtools-protocol/
	@Test
	public void test1() {
		List<String> urls = Arrays
				.asList(new String[] { "https://openx.software-testing.ru/*" });
		try {
			CDPClient
					.sendMessage(MessageBuilder.buildNetworkSetBlockedURLs(id, urls));
			driver.get(
					"https://software-testing.ru/forum/index.php?/forum/129-selenium-functional-testing/");
			utils.waitFor(10);
			uiUtils.takeScreenShot();
			System.err.println("Sreenshot path: " + uiUtils.getImagePath());
		} catch (WebDriverException | IOException | WebSocketException e) {
			System.err.println("Exception (ignored): " + e.getMessage());
		}
	}
}

