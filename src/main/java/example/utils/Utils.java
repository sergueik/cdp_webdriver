package example.utils;

import com.neovisionaries.ws.client.WebSocketException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Utils {

	private Utils utils;
	private boolean debug = false;
	protected static String osName = getOSName();
	private static final String browserDriver = osName.equals("windows")
			? "chromedriver.exe" : "chromedriver";

	private ChromeDriverService chromeDriverService;
	private WebDriver driver;
	private String wsURL;
	private static ThreadLocal<Utils> instance = new ThreadLocal<Utils>();
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);
	private static final String chromeDriverLogFile = System
			.getProperty("user.dir") + "/target/chromedriver.log";

	public void setDebug(boolean value) {
		this.debug = value;
	}

	public static Utils getInstance() {
		if (instance.get() == null) {
			instance.set(new Utils());
		}
		return instance.get();
	}

	public WebDriver launchBrowser() throws IOException {
		return launchBrowser(false);
	}

	public WebDriver launchBrowser(boolean isHeadless) throws IOException {
		logger.info("Launching browser");

		Map<String, Object> prefs = new HashMap<>();
		// 1-Allow, 2-Block, 0-default
		prefs.put("profile.default_content_setting_values.notifications", 1);
		LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable(LogType.BROWSER, Level.ALL);
		ChromeOptions options = new ChromeOptions();
		options.addArguments(Arrays.asList("--start-maximized",
				"--ssl-protocol=any", "--ignore-ssl-errors=true",
				"--disable-extensions", "--ignore-certificate-errors"));
		options.setExperimentalOption("useAutomationExtension", false);
		// options.addArguments("enable-automation");
		// options.addArguments("start-maximized");
		if (isHeadless) {
			options.addArguments(Arrays.asList("--headless", "--disable-gpu"));
		}
		// options.addArguments(Arrays.asList("--start-maximized","--remote-debugging-port=9222"));
		// options.setBinary("<chromebinary path>");
		options.setExperimentalOption("prefs", prefs);

		DesiredCapabilities crcapabilities = DesiredCapabilities.chrome();
		crcapabilities.setCapability(ChromeOptions.CAPABILITY, options);
		crcapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		crcapabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

		// the chrome driver log is where the session will be extracted from
		System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY,
				chromeDriverLogFile);

		System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,
				Paths.get(System.getProperty("user.home")).resolve("Downloads")
						.resolve(browserDriver).toAbsolutePath().toString());

		chromeDriverService = new ChromeDriverService.Builder().usingAnyFreePort()
				.withVerbose(true).build();
		chromeDriverService.start();

		try {
			driver = new RemoteWebDriver(chromeDriverService.getUrl(),
					crcapabilities);
		} catch (Exception e) {
			throw e;
		}

		// wsURL =
		// String.format("ws://localhost:9222/devtools/page/%s",driver.getWindowHandle().replace("CDwindow-",""));
		// NOTE: need to try
		// wsURL =
		// String.format("ws://localhost:9222/devtools/session/%s/chromium/send_command_and_get_result",
		// driver.getWindowHandle().replace("CDwindow-",""));
		wsURL = extractWebSocketDebuggerUrl();
		UIUtils.getInstance().setDriver(driver);
		return driver;
	}

	public String getWsURL() {
		return wsURL;
	}

	public void stopChrome() {
		driver.close();
		driver.quit();
		chromeDriverService.stop();
	}

	public void waitFor(long interval) {
		try {
			TimeUnit.SECONDS.sleep(interval);
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sleep(long interval) {
		waitFor(interval);
	}

	public int getDynamicID() {
		int min = 100000;
		int max = 999999;
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public String getSWURL(String wsURL, String targetID) {
		String[] arr = wsURL.split("page/");
		String id = arr[1];
		return wsURL.replace(id, targetID);
	}

	public String extractWebSocketDebuggerUrl() throws IOException {
		String webSocketDebuggerUrl = "";
		if (debug) {
			System.err.println("Reading the logfile: " + chromeDriverLogFile);
		}

		File file = new File(chromeDriverLogFile);
		try {

			Scanner scanner = new Scanner(file);
			String urlString = "";
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("DevTools HTTP Request: http://localhost")) {
					urlString = line.substring(line.indexOf("http"), line.length())
							.replace("/version", "");
					if (debug) {
						System.err.println("Extracted url: " + urlString);
					}
					break;
				}
			}
			scanner.close();

			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String json = org.apache.commons.io.IOUtils.toString(reader);
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				System.err.println("inspecting json: " + jsonObject.toString());
				if (jsonObject.getString("type").equals("page")) {
					webSocketDebuggerUrl = jsonObject.getString("webSocketDebuggerUrl");
					break;
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		}
		if (webSocketDebuggerUrl.equals(""))
			throw new RuntimeException("webSocketDebuggerUrl not found");
		return webSocketDebuggerUrl;
	}

	// Utilities
	public static String getOSName() {
		if (osName == null) {
			osName = System.getProperty("os.name").toLowerCase();
			if (osName.startsWith("windows")) {
				osName = "windows";
			}
		}
		return osName;
	}
}
