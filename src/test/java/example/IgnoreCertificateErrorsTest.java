package example;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.MessageBuilder;

public class IgnoreCertificateErrorsTest extends BaseTest {

	private static String url = "https://untrusted-root.badssl.com/";
	private final String selector = "#footer";
	private static WebElement element = null;

	@Before
	public void beforeTest() throws IOException {
		// protected member does not work
		BaseTest.headless = true;
		// setter does not work
		super.setHeadless(true);
		super.beforeTest();
		try {
			CDPClient.sendMessage(
					MessageBuilder.buildSecurityIgnoreCertificateErrorsMessage(id, true));
		} catch (WebSocketException e) {
		}
		driver.navigate().to(url);

	}

	@After
	public void afterTest() {
		try {
			CDPClient.sendMessage(MessageBuilder
					.buildSecurityIgnoreCertificateErrorsMessage(id, false));
		} catch (WebSocketException | IOException e) {
		}
		driver.navigate().to("about:blank");
	}

	@Test
	public void test() {

		element = uiUtils.findElement(By.cssSelector(selector), 3);
		String data = element.getText();
		assertThat(data, containsString(
				"The certificate for this site is signed using an untrusted root."));
		System.err.println(data);

	}
}
