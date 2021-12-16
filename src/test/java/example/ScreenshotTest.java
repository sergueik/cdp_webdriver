package example;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import example.messaging.MessageBuilder;
import example.utils.Utils;

public class ScreenshotTest extends BaseTest {
	private String URL = null;
	private String responseMessage = null;
	private int x;
	private int y;
	private long width;
	private long height;
	int scale = 1;

	// clipped element screenshot
	@Test
	public void test1() throws Exception {
		// CDPClient.setDebug(true);
		URL = "https://www.google.com/";
		driver.navigate().to(URL);
		WebElement logo = uiUtils.findElement(By.cssSelector("img#hplogo"), 5);
		x = logo.getLocation().getX();
		y = logo.getLocation().getY();
		width = (long) logo.getSize().getWidth();
		height = (long) logo.getSize().getHeight();
		scale = 1;
		CDPClient.sendMessage(MessageBuilder.buildTakeElementScreenShotMessage(id,
				x, y, width, height, scale));
		responseMessage = CDPClient.getResponseDataMessage(id);
		assertThat(responseMessage, notNullValue());
		byte[] bytes = Base64.getDecoder().decode(responseMessage);
		File f = new File(
				System.getProperty("user.dir") + "/target/element_screenshot.jpg");
		if (f.exists())
			f.delete();
		System.err.println("Saving screenshot: " + f.getAbsolutePath());
		Files.write(f.toPath(), bytes);
		// uiUtils.takeScreenShot();
	}

	// full page screeenshort
	@Test
	public void test2() throws Exception {
		// CDPClient.setDebug(true);
		URL = "https://www.meetup.com/";
		driver.navigate().to(URL);
		width = (long) uiUtils
				.executeJavaScript("return document.body.offsetWidth");
		height = (long) uiUtils
				.executeJavaScript("return document.body.offsetHeight");
		scale = 1;
		System.err.println("doFullPageScreenshot() message: " + MessageBuilder
				.buildTakeElementScreenShotMessage(id, 0, 0, width, height, scale));
		int id2 = Utils.getInstance().getDynamicID();
		CDPClient.sendMessage(MessageBuilder.buildTakeElementScreenShotMessage(id2,
				0, 0, width, height, scale));
		responseMessage = CDPClient.getResponseDataMessage(id2);
		assertThat(responseMessage, notNullValue());
		byte[] bytes = Base64.getDecoder().decode(responseMessage);
		String start_time = (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss"))
				.format(new Date());
		String imageName = "full_page_screeenshort_" + start_time + ".png";
		File f = new File(System.getProperty("user.dir") + "/target/" + imageName);
		if (f.exists())
			f.delete();
		System.err.println("Saving screenshot: " + f.getAbsolutePath());
		Files.write(f.toPath(), bytes);
		uiUtils.takeScreenShot();
	}
}
