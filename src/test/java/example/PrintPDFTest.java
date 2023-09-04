package example;

/**
 * Copyright 2021,2022,2023 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import example.messaging.MessageBuilder;

public class PrintPDFTest extends BaseTest {

	private String URL = null;
	private String responseMessage = null;
	private String testName = null;
	private static Map<String, Object> params = new HashMap<>();
	private static boolean keepFile = true;
	private File pdfFile;
	private String fileName = null;
	private final String filePath = System.getProperty("user.dir") + "/target";
	private PDF pdf;
	private byte[] bytes;

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
		if (!keepFile) {
			pdfFile = new File(filePath + "/" + fileName);
			pdfFile.delete();
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void test1() throws Exception {
		testName = "Print PDF";
		URL = "https://www.wikipedia.com/";
		fileName = "test1_"
				+ (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date())
				+ ".pdf";

		driver.navigate().to(URL);
		CDPClient.sendMessage(MessageBuilder.buildPrintPDFMessage(id));
		responseMessage = CDPClient.getResponseMessage(id, "data");
		System.err.println(
				"Response to " + testName + ": " + responseMessage.substring(0, 20));
		bytes = Base64.getDecoder().decode(responseMessage);
		pdfFile = new File(filePath + "/" + fileName);
		if (pdfFile.exists())
			pdfFile.delete();
		Files.write(pdfFile.toPath(), bytes);
		pdf = new PDF(pdfFile.toURL());
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
		fileName = "test2_"
				+ (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date())
				+ ".pdf";

		driver.navigate().to(URL);
		CDPClient.sendMessage(MessageBuilder.buildPrintPDFMessage(id));
		responseMessage = CDPClient.getResponseDataMessage(id);
		System.err.println(
				"Response to " + testName + ": " + responseMessage.substring(0, 20));
		bytes = Base64.getDecoder().decode(responseMessage);
		pdfFile = new File(filePath + "/" + fileName);
		if (pdfFile.exists())
			pdfFile.delete();
		Files.write(pdfFile.toPath(), bytes);
		pdf = new PDF(pdfFile);
		assertThat(pdf.text, containsString("The Free Encyclopedia"));
		// NOTE: locale UTF8
		assertThat(pdf.text, containsString("Русский"));
		assertThat(pdf.text, containsString("Français"));
		assertThat(pdf.encrypted, is(false));
		// assertThat(pdf.creator, is("Chromium"));
		// Expected: is "Chromium"
		// but: was "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36
		// (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
		assertThat(pdf.numberOfPages, equalTo(2));
		assertThat(pdf.getHeight(), equalTo(11.0));
		assertThat(pdf.getWidth(), equalTo(8.5));

	}

	@Test
	public void test3() throws Exception {
		testName = "Print PDF";
		URL = "https://www.wikipedia.com/";
		fileName = "test3_"
				+ (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date())
				+ ".pdf";

		driver.navigate().to(URL);
		params = new HashMap<>();
		params.put("landscape", false);
		params.put("displayHeaderFooter", false);
		params.put("printBackground", true);
		params.put("preferCSSPageSize", false);
		params.put("paperHeight", 11.0);
		params.put("paperWidth", 8.5);
		params.put("marginTop", 1.0);
		params.put("marginBottom", 0.75);
		params.put("marginLeft", 0.75);
		params.put("marginRight", 0.75);

		CDPClient.sendMessage(MessageBuilder.buildPrintPDFMessage(id, params));
		responseMessage = CDPClient.getResponseDataMessage(id);
		System.err.println(
				"Response to " + testName + ": " + responseMessage.substring(0, 20));
		bytes = Base64.getDecoder().decode(responseMessage);
		pdfFile = new File(filePath + "/" + fileName);
		if (pdfFile.exists())
			pdfFile.delete();
		Files.write(pdfFile.toPath(), bytes);
		System.err.println("Saved: " + pdfFile.toPath());
		pdf = new PDF(pdfFile);
		assertThat(pdf.text, containsString("The Free Encyclopedia"));
		// NOTE: locale UTF8
		assertThat(pdf.text, containsString("Русский"));
		assertThat(pdf.text, containsString("Français"));
		assertThat(pdf.encrypted, is(false));
		// assertThat(pdf.creator, is("Chromium"));
		// Expected: is "Chromium"
		// but: was "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36
		// (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
		assertThat(pdf.numberOfPages, equalTo(2));
		assertThat(pdf.getHeight(), equalTo(11.0));
		assertThat(pdf.getWidth(), equalTo(8.5));
	}

	@Test
	public void test4() throws Exception {
		testName = "Print PDF (A4)";
		URL = "https://www.wikipedia.com/";
		fileName = "test3_"
				+ (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")).format(new Date())
				+ ".pdf";

		driver.navigate().to(URL);
		params = new HashMap<>();
		params.put("landscape", false);
		params.put("displayHeaderFooter", false);
		params.put("printBackground", true);
		params.put("preferCSSPageSize", false);
		params.put("paperHeight", 11.69);
		params.put("paperWidth", 8.27);
		params.put("marginTop", 1.0);
		params.put("marginBottom", 1.44);
		params.put("marginLeft", 0.75);
		params.put("marginRight", 0.52);

		CDPClient.sendMessage(MessageBuilder.buildPrintPDFMessage(id, params));
		responseMessage = CDPClient.getResponseDataMessage(id);
		System.err.println(
				"Response to " + testName + ": " + responseMessage.substring(0, 20));
		bytes = Base64.getDecoder().decode(responseMessage);
		pdfFile = new File(filePath + "/" + fileName);
		if (pdfFile.exists())
			pdfFile.delete();
		Files.write(pdfFile.toPath(), bytes);
		System.err.println("Saved: " + pdfFile.toPath());
		pdf = new PDF(pdfFile);
		assertThat(pdf.text, containsString("The Free Encyclopedia"));
		// NOTE: locale UTF8
		assertThat(pdf.text, containsString("Русский"));
		assertThat(pdf.text, containsString("Français"));
		assertThat(pdf.encrypted, is(false));
		// assertThat(pdf.creator, is("Chromium"));
		// Expected: is "Chromium"
		// but: was "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36
		// (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
		assertThat(pdf.numberOfPages, equalTo(2));
		assertThat(pdf.getHeight(), equalTo(11.69));
		assertThat(pdf.getWidth(), equalTo(8.26));
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
		private double height;
		private double width;

		public double getHeight() {
			return height;
		}

		public double getWidth() {
			return width;
		}

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
					// find pdf page dimensions
					// https://stackoverflow.com/questions/20904191/pdfbox-find-page-dimensions
					float pageHeight = pdf.getPage(0).getMediaBox().getHeight() / 72;
					float pageWidth = pdf.getPage(0).getMediaBox().getWidth() / 72;

					// round down to 2 decimal places
					// http://www.java2s.com/example/java-utility-method/decimal-round-index-1.html
					NumberFormat format = NumberFormat.getInstance();
					format.setGroupingUsed(false);
					format.setMaximumFractionDigits(2);
					try {
						this.height = format.parse(format.format(pageHeight)).doubleValue();
					} catch (ParseException e) {
						this.height = pageHeight;
					}
					try {
						this.width = format.parse(format.format(pageWidth)).doubleValue();
					} catch (ParseException e) {
						this.width = pageWidth;
					}

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
