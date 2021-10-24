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
import org.junit.Ignore;
import org.junit.Test;

import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;

public class PageFrameTest extends BaseTest {
	private String data = null;
	private static long nodeId = 0;
	private static long backendNodeId = (long) -1;
	private String key = null;
	private JSONObject responseMessage = null;
	private JSONObject result = null;
	private JSONObject result1 = null;
	private JSONObject result2 = null;
	private JSONObject result3 = null;
	private JSONArray results = null;
	Iterator<String> results1Iterator = null;
	Iterator<Object> results2Iterator = null;
	Iterator<Object> results3Iterator = null;

	private static String baseURL = null;
	private final List<String> keys = Arrays.asList(new String[] { "domainAndRegistry", "securityOrigin",
			"secureContextType", "id", "url", "mimeType", "parentId" });

	// @Ignore
	@Test
	public void test1() {
		baseURL = "https://cloud.google.com/products/calculator";
		driver.navigate().to(baseURL);
		try {
			CDPClient.sendMessage(MessageBuilder.buildPageGetFrameTreeMessage(id));
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

	// @Ignore
	@Test
	public void test2() {
		baseURL = "https://cloud.google.com/products/calculator";
		driver.navigate().to(baseURL);
		try {
			CDPClient.sendMessage(MessageBuilder.buildPageGetFrameTreeMessage(id));
			// Assert
			result = new JSONObject(CDPClient.getResponseMessage(id, "frameTree"));
			data = result.getJSONObject("frame").getString("id");
			results2Iterator = result.getJSONArray("childFrames").iterator();
			while (results2Iterator.hasNext()) {
				result2 = (JSONObject) results2Iterator.next();
				assertThat(result2.has("frame"), is(true));
				key = result2.getJSONObject("frame").getString("id");
				assertThat(key, notNullValue());
				CDPClient.sendMessage(MessageBuilder.buildPageGetFrameOwnerMessage(id, key));
				responseMessage = new JSONObject(CDPClient.getResponseMessage(id, null));
				if (debug)
					System.err.println("getFrameOwner response: " + responseMessage);
				assertThat(responseMessage, notNullValue());
				assertThat(responseMessage.has("backendNodeId"), is(true));
				backendNodeId = responseMessage.getLong("backendNodeId");

				System.err.println("getFrameOwner backendNodeId: " + backendNodeId);
				CDPClient.sendMessage(MessageBuilder.buildGetOuterHTMLMessage(id, 0, (int) backendNodeId));

				data = CDPClient.getResponseMessage(id, "outerHTML");

				System.err.println("Get Outer HTML of frame " + key + " : " + data);
				if (responseMessage.has("nodeId")) {
					backendNodeId = responseMessage.getLong("nodeId");

					System.err.println("getFrameOwner nodeId: " + nodeId);
					CDPClient.sendMessage(MessageBuilder.buildGetOuterHTMLMessage(id, (int) nodeId));

					data = CDPClient.getResponseMessage(id, "outerHTML");

					System.err.println("Get Outer HTML of frame " + key + " : " + data);
				}
			}
		} catch (IOException | WebSocketException | InterruptedException |

				MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

	// NOTE: this test only works well alone
	// when other tests are enabled, get the error
	// {"id":330020,"method":"Overlay.highlightFrame"}
	// Exception (ignored): example.messaging.CDPClient$MessageTimeOutException:
	// No message received with this id : '330020'
	@Test
	public void test3() {
		baseURL = "https://www.javatpoint.com/oprweb/test.jsp?filename=htmliframes";
		driver.navigate().to(baseURL);
		try {

			CDPClient.sendMessage(MessageBuilder.buildPageGetFrameTreeMessage(id));
			// Assert
			result = new JSONObject(CDPClient.getResponseMessage(id, "frameTree"));
			data = result.getJSONObject("frame").getString("id");
			results2Iterator = result.getJSONArray("childFrames").iterator();
			while (results2Iterator.hasNext()) {
				result2 = (JSONObject) results2Iterator.next();
				assertThat(result2.has("frame"), is(true));
				key = result2.getJSONObject("frame").getString("id");
				assertThat(key, notNullValue());

				System.err.println("Attempted to highlight frame " + key);
				int id2 = utils.getDynamicID();
				CDPClient.sendMessage(MessageBuilder.buildDOMEnableMessage(id2));
				responseMessage = new JSONObject(CDPClient.getResponseMessage(id2, null));
				int id3 = utils.getDynamicID();
				CDPClient.sendMessage(MessageBuilder.buildOverlayEnableMessage(id3));
				responseMessage = new JSONObject(CDPClient.getResponseMessage(id3, null));

				CDPClient.sendMessage(MessageBuilder.buildOverlayHighlightFrameMessage(id, key));
				responseMessage = new JSONObject(CDPClient.getResponseMessage(id, null));
				System.err.println("getFrameOwner response: " + responseMessage);
				utils.sleep(3);
			}
		} catch (IOException | WebSocketException | InterruptedException |

				MessageTimeOutException e) {
			System.err.println("Exception (ignored): " + e.toString());
		}
	}

}
