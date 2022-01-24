package example;
/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import example.messaging.MessageBuilder;

public class BroswerDownloadTest extends BaseTest {

	private final static String url = "https://scholar.harvard.edu/files/torman_personal/files/samplepptx.pptx";
	private final static String filename = url.replaceAll("^.*/", "");
	private static String downloadPath = getTempDownloadDir();

	@After
	public void after() {
		// Arrange
		try {
			CDPClient.sendMessage(
					MessageBuilder.buildBrowserResetDownloadBehaviorMessage(id));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		driver.get("about:blank");
	}

	@Before
	public void before() {
		// Arrange
		try {
			CDPClient.sendMessage(MessageBuilder
					.buildBrowserSetDownloadBehaviorMessage(id, downloadPath));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		driver.get("about:blank");
	}

	@Test
	public void test() {
		try {
			// Act
			driver.get(url);
			utils.sleep(10);
			assertThat(new File(
					Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
							.exists(),
					is(true));
			System.err.println(String.format("Verified downloaded file: %s in %s",
					filename, downloadPath));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	// TODO: shadow DOM test
	// https://stackoverflow.com/questions/57780426/selenium-headless-chrome-how-to-query-status-of-downloads

	// http://www.java2s.com/example/java-utility-method/temp-directory-get/gettempdir-466ee.html
	public static String getTempDownloadDir() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		return (tmpdir != null && new File(tmpdir).exists()) ? tmpdir
				: Paths.get(System.getProperty("user.home")).resolve("Downloads")
						.toAbsolutePath().toString();
	}

}
