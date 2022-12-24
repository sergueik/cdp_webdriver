package example;

/**
 * Copyright 2021,2022 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchWindowException;

import example.messaging.MessageBuilder;

public class PrintPDFTest extends BaseTest {
	private String URL = null;
	private String responseMessage = null;
	private String imageName = null;
	private final String filePath = System.getProperty("user.dir") + "/target";
	private String testName = null;

	@Before
	public void beforeTest() throws IOException {
		// protected member does not work
		BaseTest.headless = true;
		// setter does not work
		super.setHeadless(true);
		super.beforeTest();

	}

	@After
	public void afterTest() {
		File f = new File(filePath + "/" + imageName);
		f.delete();
	}

	@SuppressWarnings("deprecation")
	@Test
	public void test1() throws Exception {
		testName = "Print PDF";
		URL = "https://www.wikipedia.com/";
		imageName = "cdp_img_"
				+ (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date())
				+ ".pdf";

		driver.navigate().to(URL);
		CDPClient.sendMessage(MessageBuilder.buildPrintPDFMessage(id));
		responseMessage = CDPClient.getResponseMessage(id, "data");
		System.err.println(
				"Response to " + testName + ": " + responseMessage.substring(0, 20));
		byte[] bytes = Base64.getDecoder().decode(responseMessage);
		File f = new File(filePath + "/" + imageName);
		if (f.exists())
			f.delete();
		Files.write(f.toPath(), bytes);
		PDF pdf = new PDF(f.toURL());
		assertThat(pdf.text, containsString("The Free Encyclopedia"));
		// NOTE: locale UTF8
		assertThat(pdf.text, containsString("Русский"));
		assertThat(pdf.text, containsString("Français"));
		assertThat(pdf.encrypted, is(false));
		assertThat(pdf.numberOfPages, equalTo(2));

		assertThat(pdf.encrypted, is(false));
		assertThat(pdf.producer, notNullValue());
		assertThat(pdf.subject, nullValue());
		assertThat(pdf.title, nullValue());
		assertThat(pdf.signed, is(false));

	}

	@Test
	public void test2() throws Exception {
		testName = "Print PDF";
		URL = "https://www.wikipedia.com/";
		imageName = "cdp_img_"
				+ (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date())
				+ ".pdf";

		driver.navigate().to(URL);
		CDPClient.sendMessage(MessageBuilder.buildPrintPDFMessage(id));
		responseMessage = CDPClient.getResponseDataMessage(id);
		System.err.println(
				"Response to " + testName + ": " + responseMessage.substring(0, 20));
		byte[] bytes = Base64.getDecoder().decode(responseMessage);
		File f = new File(filePath + "/" + imageName);
		if (f.exists())
			f.delete();
		Files.write(f.toPath(), bytes);
		PDF pdf = new PDF(f);
		assertThat(pdf.text, containsString("The Free Encyclopedia"));
		// NOTE: locale UTF8
		assertThat(pdf.text, containsString("Русский"));
		assertThat(pdf.text, containsString("Français"));
		assertThat(pdf.encrypted, is(false));
		assertThat(pdf.numberOfPages, equalTo(2));
	}

	// add org.apache.pdfbox.text.PDFTextStripper
	// to inspect the PDF contents
	// origin: https://github.com/codeborne/pdf-test
	public static class PDF {
		public final byte[] content;

		public final String text;
		public final int numberOfPages;
		public final String author;
		public final String creator;
		public final String keywords;
		public final String producer;
		public final String subject;
		public final String title;
		public final boolean encrypted;
		public final boolean signed;
		public final String signerName;

		private PDF(String name, byte[] content) {
			this(name, content, 1, Integer.MAX_VALUE);
		}

		private PDF(String name, byte[] content, int startPage, int endPage) {
			this.content = content;

			try (InputStream inputStream = new ByteArrayInputStream(content)) {
				try (PDDocument pdf = PDDocument.load(inputStream)) {
					PDFTextStripper pdfTextStripper = new PDFTextStripper();
					pdfTextStripper.setStartPage(startPage);
					pdfTextStripper.setEndPage(endPage);
					this.text = pdfTextStripper.getText(pdf);
					this.numberOfPages = pdf.getNumberOfPages();
					this.author = pdf.getDocumentInformation().getAuthor();
					// this.creationDate = pdf.getDocumentInformation().getCreationDate();
					this.creator = pdf.getDocumentInformation().getCreator();
					this.keywords = pdf.getDocumentInformation().getKeywords();
					this.producer = pdf.getDocumentInformation().getProducer();
					this.subject = pdf.getDocumentInformation().getSubject();
					this.title = pdf.getDocumentInformation().getTitle();
					this.encrypted = pdf.isEncrypted();

					PDSignature signature = pdf.getLastSignatureDictionary();
					this.signed = signature != null;
					this.signerName = signature == null ? null : signature.getName();
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid PDF file: " + name, e);
			}
		}

		public PDF(File pdfFile) throws IOException {
			this(pdfFile.getAbsolutePath(),
					Files.readAllBytes(Paths.get(pdfFile.getAbsolutePath())));
		}

		public PDF(URL url) throws IOException {
			this(url.toString(), readBytes(url));
		}

		public PDF(byte[] content) {
			this("", content);
		}

		public PDF(InputStream inputStream) throws IOException {
			this(readBytes(inputStream));
		}

		private static byte[] readBytes(URL url) throws IOException {
			try (InputStream inputStream = url.openStream()) {
				return readBytes(inputStream);
			}
		}

		private static byte[] readBytes(InputStream inputStream)
				throws IOException {
			ByteArrayOutputStream result = new ByteArrayOutputStream(2048);
			byte[] buffer = new byte[2048];

			int nRead;
			while ((nRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
				result.write(buffer, 0, nRead);
			}

			return result.toByteArray();
		}
	}
}
