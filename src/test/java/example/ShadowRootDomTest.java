package example;
/**
 * Copyright 2021 Serguei Kouzmine
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
	private JSONArray results1 = null;
	private JSONArray results2 = null;
	private JSONArray results3 = null;
	Iterator<Object> results1Iterator = null;
	Iterator<Object> results2Iterator = null;
	Iterator<Object> results3Iterator = null;

	// NOTE: similar with "chrome://downloads/"

	@Test
	public void test1() {
		driver.navigate().to("https://www.wikipedia.org");
		// will be empty without visitng some place?
		// Act
		driver.navigate().to("chrome://history/");
		try {
			CDPClient
					.sendMessage(MessageBuilder.buildGetDocumentMessage(id, 3, true));
			// Assert
			result = new JSONObject(CDPClient.getResponseMessage(id, "root"));
			System.err.println("DOM.getDocument object: " + result.toString(2));
			results1 = result.getJSONArray("children");
			results1Iterator = results1.iterator();
			while (results1Iterator.hasNext()) {
				result1 = (JSONObject) results1Iterator.next();
				if (result1.getString("nodeName").equals("HTML")) {
					results2 = result1.getJSONArray("children");
					results2Iterator = results2.iterator();
					while (results2Iterator.hasNext()) {
						result2 = (JSONObject) results2Iterator.next();
						if (result2.getString("nodeName").equals("BODY")) {
							results3 = result2.getJSONArray("children");
							results3Iterator = results3.iterator();
							while (results3Iterator.hasNext()) {
								result3 = (JSONObject) results3Iterator.next();
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
			// NOTE :  another element with "shadowRoots" children: "HISTORY-ROUTER"
		} catch (IOException | WebSocketException | InterruptedException
				| MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

}
