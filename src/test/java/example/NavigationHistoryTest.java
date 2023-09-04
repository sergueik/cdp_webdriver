package example;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import java.time.Duration;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import example.messaging.MessageBuilder;

public class NavigationHistoryTest extends BaseTest {

	private final static List<String> urls = Arrays.asList(
			"https://www.wikipedia.org",
			"https://chromedevtools.github.io/devtools-protocol/",
			"https://www.selenium.dev");
	private String responseMessage = null;
	private JSONObject result = null;

	// NOTE: Cucumber supports "order" attribute in @Before.
	// Junit does not
	@Before
	public void beforeTest() throws IOException {
		// protected member does not work
		BaseTest.headless = true;
		// setter does not work
		super.setHeadless(true);
		super.beforeTest();
		// Arrange
		for (String url : urls)
			driver.get(url);

	}

	@After
	public void afterTest() {
		try {
			CDPClient.sendMessage(
					MessageBuilder.buildPageResetNavigationHistoryMessage(id));
		} catch (IOException | WebSocketException e) {
		}
		super.afterTest();
	}

	@Test
	public void test1() {
		// Act
		try {
			CDPClient
					.sendMessage(MessageBuilder.buildPageGetNavigationHistoryMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			// Assert
			result = new JSONObject(responseMessage);
			System.err.println("Response : " + responseMessage);

			for (String key : Arrays.asList("currentIndex", "entries")) {
				assertThat(result.has(key), is(true));
			}
			// https://www.tabnine.com/code/java/methods/org.json.JSONObject/optJSONArray
			assertThat(result.optJSONArray("entries"), notNullValue());
			assertThat(result.optJSONArray("entries") instanceof JSONArray, is(true));
			assertThat(result.getJSONArray("entries").toList().size(),
					greaterThan(1));
			int index = 1;
			JSONObject entry = result.getJSONArray("entries").getJSONObject(index);
			for (String key : Arrays.asList("id", "url", "userTypedURL", "title",
					"transitionType")) {
				assertThat(String.format("expect the key %s", key), entry.has(key),
						is(true));
			}
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test(/* expected = org.openqa.selenium.WebDriverException.class */)
	public void test2() {
		// Act
		try {
			CDPClient
					.sendMessage(MessageBuilder.buildPageGetNavigationHistoryMessage(id));
			responseMessage = CDPClient.getResponseMessage(id, null);
			// Assert
			result = new JSONObject(responseMessage);

			int index = 2;
			JSONObject entry = ((JSONArray) result.getJSONArray("entries"))
					.getJSONObject(index);
			long entryId = entry.getLong("id");
			String url = entry.getString("url");
			// https://stackoverflow.com/questions/4355303/how-can-i-convert-a-long-to-int-in-java
			System.err
					.println(String.format("Navigate to id: %d url: %s", entryId, url));

			final long implicitWait = 10;
			final int flexibleWait = 30;
			final long polling = 1000;

			WebDriverWait wait = new WebDriverWait(driver, flexibleWait);
			wait.pollingEvery(Duration.ofMillis(polling));

			CDPClient
					.sendMessage(MessageBuilder.buildPageNavigateToHistoryEntryMessage(id,
							Math.toIntExact(entryId)));
			wait.until(ExpectedConditions.urlContains(url));
			System.err.println(
					String.format("Navigated to url: %s", driver.getCurrentUrl()));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}
}
