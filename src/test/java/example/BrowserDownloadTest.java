package example;
/**
 * Copyright 2022 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

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
		driver.get("about:blank");
	}

	@Test
	public void test1() {
		// Arrange
		try {
			CDPClient.sendMessage(MessageBuilder
					.buildBrowserSetDownloadBehaviorMessage(id, downloadPath));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
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
			new File(
					Paths.get(downloadPath).resolve(filename).toAbsolutePath().toString())
							.delete();
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@Test
	public void test2() {
		// Arrange
		final String tmpdir = createTempDownloadDir();
		try {
			CDPClient.sendMessage(MessageBuilder
					.buildBrowserSetDownloadBehaviorMessage(id, tmpdir, true));
		} catch (Exception e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
		try {
			// Act
			driver.get(url);
			utils.sleep(10);
			final Path tmpdirPath = Paths.get(tmpdir);
			List<Path> files = Files.list(tmpdirPath).collect(Collectors.toList());
			assertThat(files.size(), is(1));
			Path filePath = files.get(0);
			System.err.println(String.format("Verified downloaded file: %s in %s",
					filePath.getFileName().toString(), tmpdir));
			Files.delete(filePath);
			Files.delete(tmpdirPath);
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

	// http://www.java2s.com/Code/Java/JDK-7/Createtempfileanddirectory.htm
	public static String createTempDownloadDir() {

		String tempDownloadDirPath = null;

		try {
			Path tempDirectory = Files.createTempDirectory(
					FileSystems.getDefault().getPath(getTempDownloadDir()), "");
			System.err.println("Temporary Download directory created");
			tempDownloadDirPath = tempDirectory.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return tempDownloadDirPath;

	}

}
