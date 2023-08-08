package example.messaging;

/**
 * Copyright 2020-2023 Serguei Kouzmine
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;

import example.utils.Utils;

public class MessageBuilder {

	private static String method = null;
	private static Message message = null;
	private static final boolean debug = true; // false;
	private static Map<String, Object> params = new HashMap<>();
	private static Map<String, Object> data = new HashMap<>();

	private static class Message {
		// no need for getters or setters in the static nested class
		@SuppressWarnings("unused")
		private int id;
		@SuppressWarnings("unused")
		private String method;
		private Map<String, Object> params;
		private static final Gson gson = new Gson();

		public Message(int id, String method) {
			this.id = id;
			this.method = method;
		}

		public void addParam(String key, Object value) {
			if (Objects.isNull(params))
				params = new HashMap<>();
			params.put(key, value);
		}

		public String toJson() {
			return gson.toJson(this);
		}
	}

	private static String buildMessage(int id, String method) {
		return (new Message(id, method)).toJson();
	}

	private static String buildMessage(int id, String method,
			Map<String, Object> params) {
		message = new Message(id, method);
		for (String key : params.keySet()) {
			message.addParam(key, params.get(key));
		}
		return message.toJson();
	}

	public static String buildCustomMessage(int id, String method,
			Map<String, Object> params) {
		if (debug) {
			StringBuffer paramArg = new StringBuffer();
			for (String key : params.keySet()) {
				paramArg.append(String.format("\"%s\":%s, ", key, params.get(key)));
			}
			System.err.println(String.format(
					"Sending:\n" + "{\"id\":%d,\"method\":\"%s\"," + "\"params\":{ %s }}",
					id, method, paramArg.toString()));

		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-clearGeolocationOverride
	public static String buildClearGeoLocationMessage(int id) {
		return buildMessage(id, "Emulation.clearGeolocationOverride");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-disable
	public static String buildDOMSnapshotDisableMessage(int id) {
		return buildMessage(id, "DOMSnapshot.disable");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-enable
	public static String buildDOMSnapshotEnableMessage(int id) {
		return buildMessage(id, "DOMSnapshot.enable");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOMSnapshot/#method-captureSnapshot
	public static String buildDOMSnapshotCaptureSnapshotMessage(int id) {
		method = "DOMSnapshot.captureSnapshot";
		params = new HashMap<String, Object>();
		params.put("computedStyles", new ArrayList<String>());
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setGeolocationOverride
	// see also:
	// https://developers.google.com/web/tools/chrome-devtools/device-mode/geolocation
	// https://dev.to/coffeestain/emulate-geolocation-for-automated-testing-with-webdriverio-5e2e
	public static String buildGeoLocationMessage(int id, double latitude,
			double longitude) {
		method = "Emulation.setGeolocationOverride";
		params = new HashMap<>();
		params.put("latitude", latitude);
		params.put("longitude", longitude);
		params.put("accuracy", 100);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Emulation.setGeolocationOverride\","
							+ "\"params\":{\"latitude\":%s,\"longitude\":%s,\"accuracy\":100}}",
					id, latitude, longitude));

		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setTimezoneOverride
	public static String buildTimezoneOverrideMessage(int id, String timezoneId) {
		method = "Emulation.setTimezoneOverride";
		params = new HashMap<>();
		params.put("timezoneId", timezoneId);
		if (debug) {
			System.err.println(String.format("Sending:\n"
					+ "{\"id\":%d,\"method\":\"Emulation.setTimezoneOverride\","
					+ "\"params\":{\"timezoneId\":\"%s\"}}", id, timezoneId));
		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
	public static String buildNetWorkSetExtraHTTPHeadersMessage(int id,
			Map<String, String> headers) {
		method = "Network.setExtraHTTPHeaders";
		params = new HashMap<>();
		params.put("headers", headers);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Network.setExtraHTTPHeaders\",\"params\":{\"headers\":{\"header key\":\"header value\"}}}",
					id));
		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setExtraHTTPHeaders
	public static String buildNetWorkSetExtraHTTPHeadersMessage(int id,
			String headerKey, String headerValue) {
		method = "Network.setExtraHTTPHeaders";
		params = new HashMap<>();
		Map<String, String> headers = new HashMap<>();
		headers.put(headerKey, headerValue);
		params.put("headers", headers);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Network.setExtraHTTPHeaders\",\"params\":{\"headers\":{\"%s\":\"%s\"}}}",
					id, headerKey, headerValue));
		}
		return buildMessage(id, method, params);
	}

	public static String buildGetResponseBodyMessage(int id, String requestId) {
		method = "Network.getResponseBody";
		params = new HashMap<>();
		params.put("requestId", requestId);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Network.getResponseBody\",\"params\":{\"requestId\":\"%s\"}}",
					id, requestId));
		}
		return buildMessage(id, method, params);

	}

	public static String buildNetWorkDisableMessage(int id) {
		return buildMessage(id, "Network.disable");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-enable
	public static String buildNetWorkEnableMessage(int id,
			final long maxTotalBufferSize, final long maxResourceBufferSize,
			final long maxPostDataSize) {
		method = "Network.enable";
		params = new HashMap<>();
		params.put("maxTotalBufferSize", maxTotalBufferSize);
		params.put("maxResourceBufferSize", maxResourceBufferSize);
		params.put("maxPostDataSize", maxPostDataSize);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Network.enable\",\"params\":{\"maxTotalBufferSize\":%d,\"maxResourceBufferSize\":%d, \"maxPostDataSize\":%d}}",
					id, maxTotalBufferSize, maxResourceBufferSize, maxPostDataSize));
		}
		return buildMessage(id, method, params);
	}

	public static String buildNetWorkEnableMessage(int id) {
		if (debug) {
			System.err.println(String.format(
					"sending:\n"
							+ "{\"id\":%d,\"method\":\"Network.enable\",\"params\":{\"maxTotalBufferSize\":10000000,\"maxResourceBufferSize\":5000000, \"maxPostDataSize\":5000000}}",
					id));
		}

		return buildNetWorkEnableMessage(id, 10000000, 5000000, 5000000);
	}

	public static String buildRequestInterceptorPatternMessage(int id,
			String urlPattern, String resourceType) {
		method = "Network.setRequestInterception";
		params = new HashMap<>();
		data = new HashMap<>();
		data.put("urlPattern", urlPattern);
		data.put("resourceType", resourceType);
		data.put("interceptionStage", "HeadersReceived");

		List<Map<String, Object>> patterns = new ArrayList<>();
		patterns.add(data);
		params.put("patterns", patterns);
		if (debug) {
			System.err.println(String.format(
					"sending:\n" + "{\"id\":%d,"
							+ "\"method\":\"Network.setRequestInterception\","
							+ "\"params\":{\"patterns\":[{\"urlPattern\":\"%s\",\"resourceType\":\"%s\",\"interceptionStage\":\"HeadersReceived\"}]}}",
					id, urlPattern, resourceType));
		}

		return buildMessage(id, method, params);
	}

	public static String buildGetResponseBodyForInterceptionMessage(int id,
			String interceptionId) {
		method = "Network.getResponseBodyForInterception";
		params = new HashMap<>();
		params.put("interceptionId", interceptionId);
		if (debug) {
			System.err
					.println(String.format(
							"sending:\n"
									+ "{\"id\":%d,\"method\":\"Network.getResponseBodyForInterception\","
									+ "\"params\":{\"interceptionId\":\"%s\"}}",
							id, interceptionId));
		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network#method-continueInterceptedRequest
	public static String buildGetContinueInterceptedRequestMessage(int id,
			String interceptionId, String rawResponse) {
		method = "Network.getResponseBodyForInterception";
		params = new HashMap<>();
		params.put("interceptionId", interceptionId);
		params.put("rawResponse",
				new String(Base64.encodeBase64(rawResponse.getBytes())));
		return buildMessage(id, method, params);
		/*
		 * return String.format(
		 * "{\"id\":%d,\"method\":\"Network.continueInterceptedRequest\","+
		 * "\"params\":{\"interceptionId\":\"%s\",\"rawResponse\":\"%s\"}}", id,
		 * interceptionId, new String(Base64.encodeBase64(rawResponse.getBytes())));
		 */
	}

	// TODO: debug the role of encoded
	public static String buildGetContinueInterceptedRequestEncodedMessage(int id,
			String interceptionId, String encodedResponse) {
		method = "Network.getResponseBodyForInterception";
		params = new HashMap<>();
		params.put("interceptionId", interceptionId);
		params.put("rawResponse", encodedResponse);
		return buildMessage(id, method, params);
		/*
		 * return String.format(
		 * "{\"id\":%d,\"method\":\"Network.continueInterceptedRequest\",\"params\":{\"interceptionId\":\"%s\",\"rawResponse\":\"%s\"}}",
		 * id, interceptionId, encodedResponse);
		 */
	}

	// NOTE: the nodeId is ignored
	public static String buildGetOuterHTMLMessage(int id, int nodeId,
			int backendNodeId) {

		method = "DOM.getOuterHTML";
		params = new HashMap<>();
		params.put("backendNodeId", backendNodeId);

		final String message = buildMessage(id, method, params);
		return message;
		/*
		 * return String.
		 * format("{\"id\":%d,\"method\":\"DOM.getOuterHTMLt\", \"params\":{\"backendNodeId\":\"%d\"}}"
		 * , id, backendNodeId);
		 */
	}

	public static String buildGetOuterHTMLMessage(int id, int nodeId) {

		method = "DOM.getOuterHTML";
		params = new HashMap<>();
		params.put("nodeId", nodeId);

		final String message = buildMessage(id, method, params);
		// System.err.println("message: " + message);
		return message;
		/*
		 * return String.
		 * format("{\"id\":%d,\"method\":\"DOM.getOuterHTMLt\", \"params\":{\"nodeId\":\"%d\"}}"
		 * , id, nodeId);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/1-3/DOM/#method-getDocument
	public static String buildGetDocumentMessage(int id) {
		final String message = buildMessage(id, "DOM.getDocument");
		// System.err.println("message: " + message);
		return message;
		/*
		 * return String.format("{\"id\":%d,\"method\":\"DOM.getDocument\"}", id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/SystemInfo/#method-getInfo
	public static String buildSystemInfoGetInfoMessage(int id) {
		final String message = buildMessage(id, "SystemInfo.getInfo");
		// System.err.println("message: " + message);
		return message;
		/*
		 * return String.format("{\"id\":%d,\"method\":\"SystemInfo.getInfo\"}", id);
		 */
	}

	public static String buildGetDocumentMessage(int id, Boolean pierce) {
		return buildGetDocumentMessage(id, -1, pierce);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getDocument
	public static String buildGetDocumentMessage(int id, int depth,
			Boolean pierce) {
		method = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", pierce);
		params.put("depth", depth);
		return buildMessage(id, method, params);
		/*
		 * return String.
		 * format("{\"id\":%d,\"method\":\"DOM.getDocument\", \"params\":{\"pierce\":\"%b\"}}"
		 * , id, pierce);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-describeNode
	public static String buildDescribeNodeMessage(int id, long nodeId) {
		method = "DOM.describeNode";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		// params.put("backendNodeId", backendNodeId);
		params.put("depth", 1);
		// params.put("pierce", pierce);
		return buildMessage(id, method, params);
		/*
		 * return String.format(
		 * "{\"id\":%d,\"method\":\"DOM.describeNode\",\"params\":{\"nodeId\":\"%d\",\"depth\":\"%d\"}}",
		 * id, nodeId, 1);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/CSS/#method-setEffectivePropertyValueForNode
	public static String buildSetEffectivePropertyValueForNode(int id,
			long nodeId, String propertyName, String value) {
		method = "CSS.setEffectivePropertyValueForNode";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		params.put("propertyName", propertyName);
		params.put("value", value);

		return buildMessage(id, method, params);
		/*
		 * return String.format(
		 * "{\"id\":%d,\"method\":\"CSS.setEffectivePropertyValueForNode\",\"params\":{\"nodeId\":\"%d\",\"propertyName\":\"%s\",\"value\":\"%s\"}}", id, nodeId, propertyName, value);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelector
	public static String buildQuerySelectorMessage(int id, long nodeId,
			String selector) {
		method = "DOM.querySelector";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		params.put("selector", selector);
		return buildMessage(id, method, params);
		/*
		 * return String.
		 * format("{\"id\":%d,\"method\":\"DOM.querySelector\", \"params\":{\"nodeId\":\"%d\", \"selector\":\"%s\"}}"
		 * , id, nodeId, selector);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-querySelector
	public static String buildQuerySelectorAllMessage(int id, long nodeId,
			String selector) {
		method = "DOM.querySelectorAll";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		params.put("selector", selector);
		return buildMessage(id, method, params);
		/*
		 * return String.
		 * format("{\"id\":%d,\"method\":\"DOM.querySelectorAll\", \"params\":{\"nodeId\":\"%d\", \"selector\":\"%s\"}}"
		 * , id, nodeId, selector);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/CSS/#method-getComputedStyleForNode
	public static String buildGetComputedStyleForNode(int id, long nodeId) {
		method = "CSS.getComputedStyleForNode";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		return buildMessage(id, method, params);
		/*
		 * return String.
		 * format("{\"id\":%d,\"method\":\"CSS.getComputedStyleForNode\", \"params\":{\"nodeId\":\"%d\"}}"
		 * , id, nodeId);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser#method-getVersion
	public static String buildBrowserVersionMessage(int id) {
		return buildMessage(id, "Browser.getVersion");
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Browser.getVersion\"}", id);
		 */
	}

	public static String buildBrowserVersionMessage() {
		return buildMessage(Utils.getInstance().getDynamicID(),
				"Browser.getVersion");
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Browser.getVersion\"}", id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setVisibleSize
	public static String buildEmulationSetVisibleSizeMessage(int id, int width,
			int height) {
		method = "Emulation.setVisibleSize";
		params = new HashMap<>();
		params.put("width", width);
		params.put("height", height);
		return

		buildMessage(id, method, params);
		/*
		 * return String.format( "{\"id\":%d,\"method\":\"Emulation.setVisibleSize\","+
		 * "\"params\":{\"origin\":\"%s\",\"width\":%d,\"height\":%d}}", id, width,
		 * height);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/ServiceWorker/#method-enable
	public static String buildServiceWorkerEnableMessage(int id) {
		return buildMessage(id, "ServiceWorker.enable");
		/*
		 * return String.format("{\"id\":%s,\"method\":\"ServiceWorker.enable\"}", id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/ServiceWorker/#method-inspectWorker
	public static String buildServiceWorkerInspectMessage(int id,
			String versionId) {
		method = "ServiceWorker.inspectWorker";
		params = new HashMap<>();
		params.put("versionId", versionId);
		return buildMessage(id, method, params);
		/*
		 * return String.format(
		 * "{\"id\":%s,\"method\":\"ServiceWorker.inspectWorker\"," +
		 * "\"params\":{\"versionId\":\"%s\"}}", id, "versionId");
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/ServiceWorker/#method-deliverPushMessage
	public static String buildSendPushNotificationMessage(int id, String origin,
			String registrationId, String data) {
		method = "ServiceWorker.deliverPushMessage";
		params = new HashMap<>();
		params.put("origin", origin);
		params.put("registrationId", registrationId);
		params.put("data", data);
		return

		buildMessage(id, method, params);
		/*
		 * return String.format(
		 * "{\"id\":%s,\"method\":\"ServiceWorker.deliverPushMessage\","+
		 * "\"params\":{\"origin\":\"%s\",\"registrationId\":\"%s\",\"data\":\"%s\"}}",
		 * id, origin, registrationId, data);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Log/#method-enable
	public static String buildEnableLogMessage(int id) {
		return buildMessage(id, "Log.enable");
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Log.enable\"}", id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-printToPDF
	public static String buildPrintPDFMessage(int id,
			Map<String, Object> params) {
		method = "Page.printToPDF";

		String message = buildMessage(id, method, params);
		System.err.println("Message : " + message);
		return message;
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-printToPDF
	public static String buildPrintPDFMessage(int id) {
		method = "Page.printToPDF";
		params = new HashMap<>();
		params.put("landscape", false);
		params.put("displayHeaderFooter", false);
		params.put("printBackground", true);
		params.put("preferCSSPageSize", true);
		return buildMessage(id, method, params);
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Page.printToPDF\", " +
		 * "\"params\":{\"landscape\":%b,\"displayHeaderFooter\":%b,\"printBackground\":%b,\"preferCSSPageSize\":%b}}",
		 * id, false, false, true, true);
		 */
		// NOTE: {"error":{"code":-32602,"message":"Invalid parameters"
		// ,"data":"landscape: boolean value expected...
	}

	public static String buildPrintPDFMessage() {
		return buildPrintPDFMessage(Utils.getInstance().getDynamicID());
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Runtime/#method-enable
	public static String buildEnableRuntimeMessage(int id) {
		return buildMessage(id, "Runtime.enable");
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Runtime.enable\"}", id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/1-2/Runtime/#method-evaluate
	public static String buildRuntimeEvaluateMessage(int id,
			final String expression, Boolean returnByValue) {
		String method = "Runtime.evaluate";
		params = new HashMap<>();
		params.put("expression", expression);
		params.put("returnByValue", returnByValue);
		return buildMessage(id, method, params);

	}

	// https://chromedevtools.github.io/devtools-protocol/1-3/Runtime/#method-evaluate
	public static String buildRuntimeEvaluateMessage(int id,
			final String expression, Boolean silent, Boolean awaitPromise,
			Boolean returnByValue) {
		String method = "Runtime.evaluate";
		params = new HashMap<>();
		params.put("expression", expression);
		params.put("silent", silent);
		params.put("awaitPromise", awaitPromise);
		params.put("returnByValue", returnByValue);
		return buildMessage(id, method, params);

	}

	// see also:
	// https://chromedevtools.github.io/devtools-protocol/1-2/Runtime/#method-evaluate
	// https://developer.mozilla.org/en-US/docs/Web/API/Document/evaluate
	// https://developer.mozilla.org/en-US/docs/Web/XPath/Introduction_to_using_XPath_in_JavaScript
	// NOTE: - heavy:
	// https://github.com/google/wicked-good-xpath
	public static String buildCustomRuntimeEvaluateMessage(int id,
			final String selector, Boolean returnByValue) {
		String method = "Runtime.evaluate";
		// the $x() and $() do not quite work
		String expression = String.format(((selector.charAt(0) == '/')
				? "document.evaluate('%s', document, null, XPathResult.ANY_TYPE, null);"
				: "document.querySelector('%s');"), selector);
		params = new HashMap<>();
		params.put("expression", expression);
		params.put("returnByValue", returnByValue);
		final String message = buildMessage(id, method, params);
		if (debug) {
			System.err.println("Sending message: " + message);
		}
		return message;
		/*
		 * return String.
		 * format("{\"id\":%d,\"method\":\"Runtime.evaluate\",\"params\":{\"returnByValue\":false,\"expression\":\"return document.evaluate(\u0027%s\u0027, document,null, XPathResult.ANY_TYPE, null); \" }}"
		 * , id, selector);
		 * 
		 * return String.
		 * format("{\"id\":%d,\"method\":\"Runtime.evaluate\",\"params\":{\"returnByValue\":false,\"expression\":\"return document.querySelector(\u0027%s\u0027);\"}	}"
		 * , id, selector);
		 * 
		 */
	}

	public static String buildRuntimeEvaluateMessage(int id,
			final String selector) {
		return buildRuntimeEvaluateMessage(id, selector, false);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/BackgroundService/#method-startObserving
	public static String buildObserveBackgroundServiceMessage(int id) {
		method = "BackgroundService.startObserving";
		params = new HashMap<>();
		params.put("service", "pushMessaging");
		return buildMessage(id, method, params);
		/*
		 * return
		 * String.format("{\"id\":%s,\"method\":\"BackgroundService.startObserving\"," +
		 * "\"params\":{\"service\":\"%s\"}}" id, "pushMessaging");
		 */
	}

	public static String buildGetBrowserContextMessage(int id) {
		return buildMessage(id, "Target.getBrowserContexts");
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Target.getBrowserContexts\"}",
		 * id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-clearBrowserCache
	public static String buildClearBrowserCacheMessage(int id) {
		return buildMessage(id, "Network.clearBrowserCache");
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Network.clearBrowserCache\"}",
		 * id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-clearBrowserCookies
	public static String buildClearBrowserCookiesMessage(int id) {
		return buildMessage(id, "Network.clearBrowserCookies");
		/*
		 * return
		 * String.format("{\"id\":%d,\"method\":\"Network.clearBrowserCookies\"}", id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-getAllCookies
	public static String buildGetAllCookiesMessage(int id) {
		return buildMessage(id, "Network.getAllCookies");
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Network.getAllCookies\"}", id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-deleteCookies
	public static String buildDeleteCookiesMessage(int id, String name,
			String url, String domain, String path) {
		method = "Network.deleteCookies";
		params = new HashMap<>();
		params.put("name", name);
		params.put("url", url);
		params.put("domain", domain);
		params.put("path", path);
		return buildMessage(id, method, params);
		/*
		 * return
		 * String.format("{\"id\":%d,\"method\":\"Network.clearBrowserCookies\"}", id);
		 */
	}

	public static String buildClearDataForOriginMessage(int id, String url) {
		method = "Storage.clearDataForOrigin";
		params = new HashMap<>();
		params.put("url", url);
		params.put("storageTypes", "all");
		if (debug) {
			System.err.println(String.format(
					"Sending:\n" + "{\"id\":%s,\"method\":\"Storage.clearDataForOrigin\","
							+ "\"params\":{\"origin\":\"%s\",\"storageTypes\":\"all\"}}",
					id, url));

		}
		return buildMessage(id, method, params);
	}

	public static String buildTakeElementScreenShotMessage(int id, long x, long y,
			long width, long height, int scale) {
		method = "Page.captureScreenshot";
		params = new HashMap<>();
		data = new HashMap<>();
		data.put("x", x);
		data.put("y", y);
		data.put("height", height);
		data.put("width", width);
		data.put("scale", 1);
		params.put("format", "jpeg");
		params.put("quality", 100);
		params.put("clip", data);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n" + "{\"id\":%s,\"method\":\"Page.captureScreenshot\","
							+ "\"params\":{\"clip\":{\"x\":%s,\"y\":%s,\"width\":%s,\"height\":%s,\"scale\":%s}}}",
					id, x, y, width, height, scale));

		}

		return buildMessage(id, method, params);
	}

	public static String buildTakePageScreenShotMessage(int id) {
		return buildMessage(id, "Page.captureScreenshot");
	}

	// TODO:
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getFrameOwner
	//

	// https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-createTarget
	public static String buildCreateTargetMessage(int id, String url, int width,
			int height, String browserContextId, boolean enableBeginFrameControl,
			boolean newWindow, boolean background) {

		method = "Target.createTarget";
		params = new HashMap<>();
		params.put("url", url);
		params.put("width", width);
		params.put("height", height);
		// params.put("browserContextId", browserContextId);
		// params.put("enableBeginFrameControl", enableBeginFrameControl);
		params.put("newWindow", newWindow);
		// params.put("background", background);
		return buildMessage(id, method, params);
	}

	public static String buildCreateTargetMessage(int id, String url,
			boolean newWindow) {
		return buildCreateTargetMessage(id, url, 0, 0, null, false, newWindow,
				false);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-closeTarget
	public static String buildCloseTargetMessage(int id, String targetId) {
		method = "Target.closeTarget";
		params = new HashMap<>();
		params.put("targetId", targetId);
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-activateTarget
	public static String buildActivateTargetMessage(int id, String targetId) {

		method = "Target.activateTarget";
		params = new HashMap<>();
		params.put("targetId", targetId);
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-attachToTarget
	public static String buildAttachToTargetMessage(int id, String targetId) {

		method = "Target.attachToTarget";
		params = new HashMap<>();
		params.put("targetId", targetId);
		return buildMessage(id, method, params);
	}

	private static String buildAttachToTargetMessage(String targetId) {

		method = "Target.attachToTarget";
		// message = new Message(Utils.getInstance().getDynamicID(), method);
		params = new HashMap<>();
		params.put("targetId", targetId);
		return buildMessage(Utils.getInstance().getDynamicID(), method, params);
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Target.attachToTarget\"," +
		 * "\"params\":{\"targetId\":\"%s\"}}", Utils.getInstance().getDynamicID(),
		 * targetId);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-detachFromTarget
	public static String buildDetachFromTargetMessage(int id, String sessionID,
			String targetId) {

		method = "Target.detachFromTarget";
		params = new HashMap<>();
		params.put("targetId", targetId);
		params.put("sessionID", sessionID);
		return buildMessage(id, method, params);
	}

	private static String buildDetachFromTargetMessage(String sessionID,
			String targetId) {

		method = "Target.detachFromTarget";
		params = new HashMap<>();
		params.put("targetId", targetId);
		params.put("sessionID", sessionID);
		return buildMessage(Utils.getInstance().getDynamicID(), method, params);
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Target.detachFromTarget\"," +
		 * "\"params\":{\"targetId\":\"%s\", \"sessionId\":\"%s\"}}",
		 * Utils.getInstance().getDynamicID(), targetId, sessionId);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-getTargetInfo
	public static String buildTargetInfoMessage(int id, String targetId) {

		method = "Target.TargetInfo";
		params = new HashMap<>();
		params.put("targetId", targetId);
		return buildMessage(id, method, params);
	}

	private static String buildTargetInfoMessage(String targetId) {

		method = "Target.TargetInfo";
		params = new HashMap<>();
		params.put("targetId", targetId);
		return buildMessage(Utils.getInstance().getDynamicID(), method, params);
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Target.TargetInfo\"," +
		 * "\"params\":{\"targetId\":\"%s\"}}", Utils.getInstance().getDynamicID(),
		 * targetId);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-getTargetInfo
	public static String buildGetTargetsMessage(int id) {
		return buildMessage(id, "Target.getTargets");
	}

	//
	public static String buildRequestInterceptorEnabledMessage() {
		String method = "Network.setRequestInterception";
		int id = 4;
		message = new Message(id, method);
		params = new HashMap<>();
		params.put("enabled", true);
		return buildMessage(id, method, params);
		/*
		 * return String.format(
		 * "{\"id\":4, \"method\":\"Network.setRequestInterception\"," +
		 * "\"params\":{\"enabled\":true}}");
		 */
	}

	public static String buildBasicHttpAuthenticationMessage(String username,
			String password) {
		byte[] encodedBytes = Base64
				.encodeBase64(String.format("%s:%s", username, password).getBytes());
		String base64EncodedCredentials = new String(encodedBytes);
		String method = "Network.setExtraHTTPHeaders";
		int id = 2;
		params = new HashMap<>();
		data = new HashMap<>();
		data.put("Authorization",
				String.format("Basic %s", base64EncodedCredentials));
		params.put("headers", data);
		return buildMessage(id, method, params);
		/*
		 * return String.format( "{\"id\":2,\"method\":\"Network.setExtraHTTPHeaders\","
		 * + "\"params\":{\"headers\":{\"Authorization\":\"Basic %s\"}}}",
		 * base64EncodedCredentials);
		 */
	}

	public static String buildSendObservingPushMessage() {
		String method = "BackgroundService.clearEvents";
		message = new Message(Utils.getInstance().getDynamicID(), method);
		params = new HashMap<>();
		params.put("service", "backgroundFetch");
		return buildMessage(Utils.getInstance().getDynamicID(), method, params);
		/*
		 * return String.format(
		 * "{\"id\":%d,\"method\":\"BackgroundService.clearEvents\"," +
		 * "\"params\":{\"service\":\"backgroundFetch\"}}",
		 * Utils.getInstance().getDynamicID());
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setUserAgentOverride
	public static String buildSetUserAgentOverrideMessage(String userAgent,
			String platform) {
		String method = "Network.setUserAgentOverride";
		message = new Message(Utils.getInstance().getDynamicID(), method);
		params = new HashMap<>();
		params.put("userAgent", userAgent);
		params.put("platform", platform);
		return buildMessage(Utils.getInstance().getDynamicID(), method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Performance/#method-setTimeDomain
	public static String buildSetTimeDomainMessage(int id,
			final String timeDomain) {

		String method = "Performance.setTimeDomain";
		params = new HashMap<>();
		params.put("timeDomain", timeDomain);
		return buildMessage(id, method, params);
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Performance.setTimeDomain\"," +
		 * "\"params\":{\"timeDomain\":\"%s\"}}", id, timeDomain);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Performance/#method-enable
	public static String buildPerformanceEnableMessage(int id) {
		return buildMessage(id, "Performance.enable");
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Performance.enable\"}", id);
		 */
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Performance/#method-disable
	public static String buildPerformanceDisableMessage(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n" + "{\"id\":%d,\"method\":\"Performance.disable\"}", id));

		}
		return buildMessage(id, "Performance.disable");
	}

	//
	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-enable
	public static String buildDOMEnableMessage(int id) {
		if (debug) {
			System.err.println(String
					.format("Sending:\n" + "{\"id\":%d,\"method\":\"DOM.enable\"}", id));

		}
		return buildMessage(id, "DOM.enable");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-disable
	public static String buildDOMDisableMessage(int id) {
		if (debug) {
			System.err.println(String
					.format("Sending:\n" + "{\"id\":%d,\"method\":\"DOM.disable\"}", id));

		}
		return buildMessage(id, "DOM.disable");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/CSS/#method-enable
	public static String buildCSSEnableMessage(int id) {
		if (debug) {
			System.err.println(String
					.format("Sending:\n" + "{\"id\":%d,\"method\":\"DOM.enable\"}", id));

		}
		return buildMessage(id, "CSS.enable");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/CSS/#method-disable
	public static String buildCSSDisableMessage(int id) {
		if (debug) {
			System.err.println(String
					.format("Sending:\n" + "{\"id\":%d,\"method\":\"CSS.disable\"}", id));

		}
		return buildMessage(id, "CSS.disable");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-highlightFrame
	public static String buildOverlayHighlightFrameMessage(int id,
			final String frameId) {
		method = "Overlay.highlightFrame";
		params = new HashMap<>();
		Map<String, Integer> rgb_data = new HashMap<>();
		rgb_data.put("r", Utils.getRandomColor());
		rgb_data.put("g", Utils.getRandomColor());
		rgb_data.put("b", Utils.getRandomColor());
		rgb_data.put("a", 1);
		params.put("frameId", frameId);
		params.put("contentColor", rgb_data);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n" + "{\"id\":%d,\"method\":\"Overlay.highlightFrame\"}",
					id));

		}
		return buildMessage(id, method, params);

	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-enable
	public static String buildOverlayEnableMessage(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n" + "{\"id\":%d,\"method\":\"Overlay.enable\"}", id));

		}
		return buildMessage(id, "Overlay.enable");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Performance/#method-getMetrics
	public static String buildPerformanceGetMetricsMessage(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n" + "{\"id\":%d,\"method\":\"Performance.getMetrics\"}",
					id));

		}
		return buildMessage(id, "Performance.getMetrics");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setUserAgentOverride
	public static String buildEmulationSetUserAgentMessage(int id,
			final String userAgent) {
		method = "Emulation.setUserAgentOverride";
		params = new HashMap<>();
		params.put("userAgent", userAgent);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Emulation.setUserAgentOverride\"}",
					id));

		}
		return buildMessage(id, method, params);

	}

	// DEPRECATED - not returning result when invoked
	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnLoad
	public static String buildPageAddScriptToEvaluateOnLoadMessage(int id,
			final String source) {
		method = "Page.addScriptToEvaluateOnLoad";
		params = new HashMap<>();
		params.put("source", source);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Page.addScriptToEvaluateOnLoad\", \"params\":{\"source\":\"%s\"}",
					id, source));

		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnNewDocument
	public static String buildPageAddScriptToEvaluateOnNewDocumentMessage(int id,
			final String source) {
		method = "Page.addScriptToEvaluateOnNewDocument";
		params = new HashMap<>();
		params.put("source", source);
		// params.put("worldName", null);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Page.addScriptToEvaluateOnNewDocument\", \"params\":{\"source\":\"%s\"}",
					id, source));

		}
		return buildMessage(id, method, params);
	}

	public static String buildPageAddScriptToEvaluateOnNewDocumentMessage(
			final String source) {

		method = "Page.addScriptToEvaluateOnNewDocument";
		params = new HashMap<>();
		params.put("source", source);
		// params.put("worldName", null);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Page.addScriptToEvaluateOnNewDocument\", \"params\":{\"source\":\"\"}",
					Utils.getInstance().getDynamicID(), source));

		}

		return buildMessage(Utils.getInstance().getDynamicID(), method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-removeScriptToEvaluateOnNewDocument
	public static String buildPageRemoveScriptToEvaluateOnNewDocument(int id,
			final String identifier) {
		method = "Page.removeScriptToEvaluateOnNewDocument";
		params = new HashMap<>();
		params.put("identifier", identifier);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Page.removeScriptToEvaluateOnNewDocument\", \"params\":{\"identifier\":\"\"}",
					id, identifier));

		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
	public static String buildEmulationSetDeviceMetricsMessage(int id,
			final int width, final int height, final int scaleFactor,
			final boolean isMobile, final int scale) {
		method = "Emulation.setDeviceMetricsOverride";
		params = new HashMap<>();
		params.put("width", width);
		params.put("height", height);
		params.put("deviceScaleFactor", scaleFactor);
		params.put("mobile", isMobile);
		params.put("scale", scale);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Emulation.setDeviceMetricsOverride\"}",
					id));

		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
	public static String buildEmulationSetDeviceMetricsMessage(int id,
			final int width, final int height, final int scaleFactor) {
		return buildEmulationSetDeviceMetricsMessage(id, width, height, scaleFactor,
				true, 1);
	}

	public static String buildEmulationSetDeviceMetricsMesage(int id,
			final int width, final int height) {
		return buildEmulationSetDeviceMetricsMessage(id, width, height, 1, true, 1);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-clearBrowserCache/
	public static String buildNetworkClearBrowserCache(int id) {
		return buildMessage(id, "Network.clearBrowserCache");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setCacheDisabled
	public static String buildNetworkSetCacheDisabled(int id,
			boolean cacheDisabled) {
		params = new HashMap<>();
		method = "Network.setCacheDisabled";
		params.put("cacheDisabled", cacheDisabled);
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setBlockedURLs
	// NOTE: not available in stable or stable RC branches
	// found not working with Selenium 3.x
	public static String buildNetworkSetBlockedURLs(int id, String[] urls) {
		method = "Network.setBlockedURLs";
		params = new HashMap<>();
		params.put("urls", urls);
		if (debug) {
			System.err.println(String.format(
					"sending:\n"
							+ "{\"id\":%d,\"method\":\"%s\",\"params\":{\"urls\":[%s]}}",
					id, method, Arrays.asList(urls)));

		}
		return buildMessage(id, method, params);
	}

	public static String buildNetworkSetBlockedURLs(int id, List<String> urls) {
		method = "Network.setBlockedURLs";

		params = new HashMap<>();
		params.put("urls", urls.toArray());
		if (debug) {
			System.err.println(String.format(
					"sending:\n"
							+ "{\"id\":%d,\"method\":\"%s\",\"params\":{\"urls\":[%s]}}",
					id, method, urls.toString()));
		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getLayoutMetrics
	public static String buildPageGetLayoutMetricsMessage(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n{\"id\":%d,\"method\":\"Page.getLayoutMetrics\"}", id));
		}
		return buildMessage(id, "Page.getLayoutMetrics");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getFrameTree
	public static String buildPageGetFrameTreeMessage(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n{\"id\":%d,\"method\":\"Page.getFrameTree\"}", id));

		}
		return buildMessage(id, "Page.getFrameTree");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getFrameOwner
	public static String buildPageGetFrameOwnerMessage(int id, String frameId) {
		method = "DOM.getFrameOwner";
		params = new HashMap<>();
		params.put("frameId", frameId);
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"Page.getFrameTree\"}", id));
		 * 
		 * }
		 */
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getFrameOwner
	public static String buildBrowserResetDownloadBehaviorMessage(int id) {
		method = "Browser.setDownloadBehavior";
		params = new HashMap<>();
		params.put("behavior", "default");
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{\"behavior\":\"default\"}}}"
		 * , id, method ));
		 * 
		 * }
		 */
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-setDownloadBehavior
	public static String buildBrowserSetDownloadBehaviorMessage(int id,
			String downloadPath) {
		return buildBrowserSetDownloadBehaviorMessage(id, downloadPath, false);
	}

	// NOTE: the "allowAndName" will randomly name the downloaded file public
	public static String buildBrowserSetDownloadBehaviorMessage(int id,
			String downloadPath, boolean randomlyName) {
		method = "Browser.setDownloadBehavior";
		params = new HashMap<>();
		params.put("behavior", randomlyName ? "allowAndName" : "allow");
		params.put("downloadPath", downloadPath);
		params.put("eventsEnabled", true);
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{\"behavior\":\"%s\", \"downloadPath\":\"%s\"}}}"
		 * , id, method, (randomlyName ? "allowAndName" : "allow"), downloadPath));
		 * 
		 * }
		 */
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-setDownloadBehavior
	public static String buildPageResetDownloadBehaviorMessage(int id) {
		method = "Page.setDownloadBehavior";
		params = new HashMap<>();
		params.put("behavior", "default");
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{\"behavior\":\"default\"}}}"
		 * , id, method ));
		 * 
		 * }
		 */
		return buildMessage(id, method, params);
	}

	public static String buildPageSetDownloadBehaviorMessage(int id,
			String downloadPath) {
		method = "Page.setDownloadBehavior";
		params = new HashMap<>();
		params.put("behavior", "allow");
		params.put("downloadPath", downloadPath);
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{\"behavior\":\"allow\", \"downloadPath\":\"%s\"}}}"
		 * , id, method,downloadPath)); }
		 */
		return buildMessage(id, method, params);
	}

	public static String buildEmulationResetPageScaleMessage(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Emulation.resetPageScaleFactor\"}",
					id));

		}
		return buildMessage(id, "Emulation.resetPageScaleFactor");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getHistograms
	public static String buildBrowserHistogramsMessage(int id) {
		return buildBrowserHistogramsMessage(id, "");
	}

	public static String buildBrowserHistogramsMessage(int id, String query) {
		method = "Browser.getHistograms";
		params = new HashMap<>();

		params.put("query", query);
		params.put("delta", false);

		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{\"query\":\"%s\", \"delta\": \"false\"}}}"
		 * , id, method,query)); }
		 */
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Browser/#method-getHistogram
	public static String buildBrowserHistogramMessage(int id, String name) {
		method = "Browser.getHistogram";
		params = new HashMap<>();

		params.put("name", name);
		params.put("delta", false);

		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{\"name\":\"%s\", \"delta\": \"false\"}}}"
		 * , id, method,name )); }
		 */
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-getAllCookies
	public static String buildNetworkGetAllCookiesMessage(int id) {
		method = "Network.getAllCookies";
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{}}}", id, method )); }
		 */
		return buildMessage(id, method);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-getCookies
	public static String buildNetworkGetCookiesMessage(int id,
			List<String> urls) {
		method = "Network.getCookies";
		params = new HashMap<>();

		params.put("urls", urls);
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{\"urls\":\"%s\"}}}", id,
		 * method,urls )); }
		 */
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getNavigationHistory
	public static String buildPageGetNavigationHistoryMessage(int id) {
		method = "Page.getNavigationHistory";
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{}}}", id, method )); }
		 */
		return buildMessage(id, method);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-navigateToHistoryEntry
	public static String buildPageNavigateToHistoryEntryMessage(int id,
			int entryId) {
		method = "Page.navigateToHistoryEntry";
		params = new HashMap<>();
		params.put("entryId", entryId);
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{\"entryId\":  %d}}}",
		 * id, method, entryId )); }
		 */
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Security/#method-setIgnoreCertificateErrors
	public static String buildSecurityIgnoreCertificateErrorsMessage(int id,
			boolean ignore) {
		method = "Security.setIgnoreCertificateErrors";
		params = new HashMap<>();
		params.put("ignore", ignore);
		return buildMessage(id, method);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-resetNavigationHistory
	public static String buildPageResetNavigationHistoryMessage(int id) {
		method = "Page.resetNavigationHistory";
		/*
		 * if (debug) { System.err.println(String.format(
		 * "Sending:\n{\"id\":%d,\"method\":\"%s\", \"params\":{}}}", id, method )); }
		 */
		return buildMessage(id, method);
	}

}

