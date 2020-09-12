package example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

/**
 * Selected test scenarios for Selenium 4 Chrome Developer Tools bridge inspired
 * origin: https://github.com/sachinguptait/SeleniumAutomation
 *
 * @author: Serguei Kouzmine (kouzmine_serguei@yahoo.com)
 */

public class WindowsTabsTest extends BaseTest {

	private static String url1 = "https://en.wikipedia.org/wiki/Main_Page";
	private static String url2 = "https://www.google.com";
	private static String url3 = "http://newtours.demoaut.com/";
	private static Map<Integer, String> data = new HashMap<>();
	private static String baseURL = "about:blank";

	// https://github.com/qtacore/chrome_master/blob/master/chrome_master/input_handler.py#L32
	@Ignore
	@Test
	public void tabTest() {
		try {
			System.err.println("tabTest creating new browser tab for id " + id);
			CDPClient
					.sendMessage(MessageBuilder.buildCreateTargetMessage(id, url1, true));
			utils.sleep(1000);
			String targetId = CDPClient.getResponseDataMessage(id);
			System.err.println("targetId: " + targetId);

		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	@Ignore
	@Test
	public void windowTest() {
		try {
			System.err
					.println("windowTest crearting new browser window for id " + id);
			CDPClient.sendMessage(
					MessageBuilder.buildCreateTargetMessage(id, url2, false));
			utils.sleep(1000);
			String targetId = CDPClient.getResponseDataMessage(id);
			System.err.println("targetId: " + targetId);

		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			// ignore
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

}
