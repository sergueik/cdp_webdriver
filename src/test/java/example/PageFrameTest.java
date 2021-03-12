package example;
/**
 * Copyright 2021 Serguei Kouzmine
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class PageFrameTest extends BaseTest {
	private JSONObject result = null;
	private JSONObject result1 = null;
	private String data = null;
	private String key = null;
	private JSONObject result2 = null;
	private JSONObject result3 = null;
	private JSONArray results = null;
	Iterator<String> results1Iterator = null;
	Iterator<Object> results2Iterator = null;
	Iterator<Object> results3Iterator = null;

	// NOTE: similar with "chrome://downloads/"

	private final String baseURL = "https://cloud.google.com/products/calculator";
	private final List<String> keys = Arrays
			.asList(new String[] { "domainAndRegistry", "securityOrigin",
					"secureContextType", "id", "url", "mimeType", "parentId" });

	@Test
	public void test1() {
		driver.navigate().to(baseURL);
		try {
			CDPClient.sendMessage(MessageBuilder.buildPageGetFrameTree(id));
			// Assert
			result = new JSONObject(CDPClient.getResponseMessage(id, "frameTree"));
			System.err.println("Page.FrameTree object: " + result.toString(2));
			result1 = result.getJSONObject("frame");
			results1Iterator = result1.keys();
			while (results1Iterator.hasNext()) {
				key = results1Iterator.next();
				if (!keys.contains(key)) {
					continue;
				}
				data = result1.getString(key);
				System.err.println(key + " = " + data);
			}
			results = result.getJSONArray("childFrames");
			results2Iterator = results.iterator();
			while (results2Iterator.hasNext()) {
				result2 = (JSONObject) results2Iterator.next();
				assertThat(result2.has("frame"), is(true));
				result3 = result2.getJSONObject("frame");
				assertThat(result3.has("url"), is(true));
				assertThat(result3.has("id"), is(true));
				assertThat(result3.has("parentId"), is(true));
				results1Iterator = result3.keys();
				while (results1Iterator.hasNext()) {
					key = results1Iterator.next();
					if (!keys.contains(key)) {
						continue;
					}
					data = result3.getString(key);
					System.err.println(key + " = " + data);
				}
			}

			// NOTE : another element with "shadowRoots" children: "HISTORY-ROUTER"
		} catch (IOException | WebSocketException | InterruptedException |

				MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

}

