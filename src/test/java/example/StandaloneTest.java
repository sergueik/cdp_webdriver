package example;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.kklisura.cdt.protocol.commands.Network;
import com.github.kklisura.cdt.protocol.types.network.Response;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.ChromeService;
import com.github.kklisura.cdt.services.impl.ChromeServiceImpl;
import com.github.kklisura.cdt.services.types.ChromeTab;

import example.utils.TestUtils;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// based on: https://github.com/barancev/selenium-cdp-integration-example
public class StandaloneTest {

	protected static String osName = TestUtils.getOSName();
	private static final String browserDriver = osName.equals("windows")
			? "chromedriver.exe" : "chromedriver";

	protected static RemoteWebDriver driver;
	protected static ChromeDevToolsService cdpService;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,
				Paths.get(System.getProperty("user.home")).resolve("Downloads")
						.resolve(browserDriver).toAbsolutePath().toString());
		driver = new ChromeDriver();

		Capabilities caps = driver.getCapabilities();
		@SuppressWarnings("unchecked")
		String debuggerAddress = (String) ((Map<String, Object>) caps
				.getCapability("goog:chromeOptions")).get("debuggerAddress");
		int debuggerPort = Integer.parseInt(debuggerAddress.split(":")[1]);
		System.err.println("Exploring port: " + debuggerPort);

		//
		ChromeService chromeService = new ChromeServiceImpl(debuggerPort);
		ChromeTab pageTab = chromeService.getTabs().stream()
				.filter(tab -> tab.getType().equals("page")).findFirst().get();
		cdpService = chromeService.createDevToolsService(pageTab);
	}

	public static class ResponseInfo {
		final String url;
		final int status;

		ResponseInfo(String url, int status) {
			this.url = url;
			this.status = status;
		}

		public String toString() {
			return String.format("%s -> %s", url, status);
		}
	}

	@Test
	public void canHandleTraffic() {
		List<ResponseInfo> responses = new ArrayList<>();
		Network network = cdpService.getNetwork();
		network.onResponseReceived(event -> {
			Response res = event.getResponse();
			responses.add(new ResponseInfo(res.getUrl(), res.getStatus()));
		});
		network.enable();

		driver.get("http://stahlburg.by/");
		responses.stream().filter(res -> res.status != 200)
				.forEach(System.out::println);
	}

	@AfterClass
	public static void afterClass() {
		if (driver != null) {
			driver.quit();
			driver = null;
			cdpService = null;
		}
	}
}

