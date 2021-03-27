
package example;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.json.JSONObject;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import example.messaging.MessageBuilder;

public class BroswerVersionTest extends BaseTest {
	private JSONObject result = null;

	@Test
	public void getBroswerVersionTest() {
		// Arrange
		// Act
		try {
			CDPClient.sendMessage(MessageBuilder.buildBrowserVersionMessage(id));
			// Assert
			result = new JSONObject(CDPClient.getResponseMessage(id, null));
			for (String field : Arrays
					.asList(new String[] { "protocolVersion", "product", "revision", "userAgent", "jsVersion" })) {
				assertThat(result.has(field), is(true));
			}
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}

	}
}
