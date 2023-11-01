package example;
/**
 * Copyright 2021,2023 Serguei Kouzmine
 */

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class ShadowRootDomTest extends BaseTest {
	private JSONObject result = null;
	private JSONObject result1 = null;
	private JSONObject result2 = null;
	private JSONObject result3 = null;
	Iterator<Object> iterator1 = null;
	Iterator<Object> iterator2 = null;
	Iterator<Object> iterator3 = null;

	// NOTE: similar with "chrome://downloads/"

	@Test
	public void test1() {
		driver.navigate().to("https://www.wikipedia.org");
		// will be empty without visitng some place?
		// Act
		driver.navigate().to("chrome://history/");
		try {
			CDPClient
					.sendMessage(MessageBuilder.buildDOMGetDocumentMessage(id, 3, true));
			// Assert
			result = new JSONObject(CDPClient.getResponseMessage(id, "root"));
			System.err.println("DOM.getDocument object: " + result.toString(2));
			iterator1 = result.getJSONArray("children").iterator();
			while (iterator1.hasNext()) {
				result1 = (JSONObject) iterator1.next();
				if (result1.getString("nodeName").equals("HTML")) {
					iterator2 = result1.getJSONArray("children").iterator();
					while (iterator2.hasNext()) {
						result2 = (JSONObject) iterator2.next();
						if (result2.getString("nodeName").equals("BODY")) {
							iterator3 = result2.getJSONArray("children").iterator();
							while (iterator3.hasNext()) {
								result3 = (JSONObject) iterator3.next();
								if (result3.getString("nodeName").equals("HISTORY-APP")) {
									System.err.println(
											"Shadow Roots in element <history-app id=\"history-app\">:\n"
													+ result3.getJSONArray("shadowRoots").toString(2));
								}
							}
						}
					}
				}
			}
			// NOTE : another element with "shadowRoots" children: "HISTORY-ROUTER"
		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

}
