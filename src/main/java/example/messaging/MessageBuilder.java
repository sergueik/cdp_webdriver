package example.messaging;
/**
 * Copyright 2020,2021 Serguei Kouzmine
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		 * return String.format("{\"id\":%d,\"method\":\"DOM.getOuterHTMLt\", \"params\":{\"backendNodeId\":\"%d\"}}", id, backendNodeId);
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
		 * return String.format("{\"id\":%d,\"method\":\"DOM.getOuterHTMLt\", \"params\":{\"nodeId\":\"%d\"}}", id, nodeId);
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

	public static String buildGetDocumentMessage(int id, Boolean pierce) {
		return buildGetDocumentMessage(id, -1, pierce);
	}

	public static String buildGetDocumentMessage(int id, int depth,
			Boolean pierce) {
		method = "DOM.getDocument";
		params = new HashMap<>();
		params.put("pierce", pierce);
		params.put("depth", depth);
		return buildMessage(id, method, params);
		/*
		 * return String.format("{\"id\":%d,\"method\":\"DOM.getDocument\", \"params\":{\"pierce\":\"%b\"}}", id, pierce);
		 */
	}

	public static String buildDescribeNodeMessage(int id, long nodeId) {
		method = "DOM.describeNode";
		params = new HashMap<>();
		params.put("nodeId", nodeId);
		params.put("depth", 1);
		return buildMessage(id, method, params);
		/*
		 * return String.format(
		 * "{\"id\":%d,\"method\":\"DOM.describeNode\",\"params\":{\"nodeId\":\"%d\",\"depth\":\"%d\"}}",
		 * id, nodeId, 1);
		 */
	}

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
		 * return
		 * String.format("{\"id\":%d,\"method\":\"Runtime.evaluate\",\"params\":{\"returnByValue\":false,\"expression\":\"return document.evaluate(\u0027%s\u0027, document,null, XPathResult.ANY_TYPE, null); \" }}", id, selector);
		 * 
		 * return
		 * String.format("{\"id\":%d,\"method\":\"Runtime.evaluate\",\"params\":{\"returnByValue\":false,\"expression\":\"return document.querySelector(\u0027%s\u0027);\"}	}", id, selector);
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
			long height, long width, int scale) {
		method = "Page.captureScreenshot";
		params = new HashMap<>();
		data = new HashMap<>();
		data.put("x", x);
		data.put("y", y);
		data.put("height", height);
		data.put("width", width);
		data.put("scale", 100);
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

	// https://chromedevtools.github.io/devtools-protocol/tot/Target/#method-activateTarget
	public static String buildActivateTargetMessage(int id, String targetId) {

		method = "Target.activateTarget";
		params = new HashMap<>();
		params.put("targetId", targetId);
		return buildMessage(id, method, params);
	}

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

	public static String buildSetUserAgentOverrideMessage(String userAgent,
			String platform) {
		String method = "Network.setUserAgentOverride";
		message = new Message(Utils.getInstance().getDynamicID(), method);
		params = new HashMap<>();
		params.put("userAgent", userAgent);
		params.put("platform", platform);
		return buildMessage(Utils.getInstance().getDynamicID(), method, params);
	}

	private static String buildAttachToTargetMessage(String targetId) {

		String method = "BackgroundService.clearEvents";
		message = new Message(Utils.getInstance().getDynamicID(), method);
		params = new HashMap<>();
		params.put("targetId", targetId);
		return buildMessage(Utils.getInstance().getDynamicID(), method, params);
		/*
		 * return String.format("{\"id\":%d,\"method\":\"Target.attachToTarget\"," +
		 * "\"params\":{\"targetId\":\"%s\"}}", Utils.getInstance().getDynamicID(),
		 * targetId);
		 */
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
	// https://chromedevtools.github.io/devtools-protocol/tot/Overlay/#method-highlightFrame

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
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
	public static String buildPerformanceGetMetrics(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n" + "{\"id\":%d,\"method\":\"Performance.getMetrics\"}",
					id));

		}
		return buildMessage(id, "Performance.getMetrics");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Emulation/#method-setDeviceMetricsOverride
	public static String buildEmulationSetUserAgent(int id,
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

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-addScriptToEvaluateOnNewDocument
	public static String buildPageAddScriptToEvaluateOnNewDocument(int id,
			final String source) {
		method = "Page.addScriptToEvaluateOnNewDocument";
		params = new HashMap<>();
		params.put("source", source);
		params.put("worldName", null);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Page.addScriptToEvaluateOnNewDocument\", \"params\":{\"source\":\"\"}",
					id, source));

		}
		return buildMessage(id, method, params);
	}

	public static String buildPageAddScriptToEvaluateOnNewDocument(
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
	public static String buildEmulationSetDeviceMetrics(int id, final int width,
			final int height, final int scaleFactor, final boolean isMobile,
			final int scale) {
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

	public static String buildEmulationSetDeviceMetrics(int id, final int width,
			final int height, final int scaleFactor) {
		return buildEmulationSetDeviceMetrics(id, width, height, scaleFactor, true,
				1);
	}

	public static String buildEmulationSetDeviceMetrics(int id, final int width,
			final int height) {
		return buildEmulationSetDeviceMetrics(id, width, height, 1, true, 1);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-clearBrowserCache/
	public static String buildNetworkClearBrowserCache(int id) {
		return buildMessage(id, "Network.clearBrowserCache");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-setBlockedURLs
	// NOTE: not available in stable or stable RC branches
	public static String buildNetworkSetBlockedURLs(int id, String[] urls) {
		method = "Network.setBlockedURLs";
		params = new HashMap<>();
		params.put("urls", urls);
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Network.setBlockedURLs\",\"params\":{\"urls\":[%s]}}",
					id, Arrays.asList(urls)));

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
							+ "{\"id\":%d,\"method\":\"Network.setBlockedURLs\",\"params\":{\"urls\":[%s]}}",
					id, urls.toString()));
		}
		return buildMessage(id, method, params);
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/Page/#method-getFrameTree
	public static String buildPageGetFrameTree(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n{\"id\":%d,\"method\":\"Page.getFrameTree\"}", id));

		}
		return buildMessage(id, "Page.getFrameTree");
	}

	// https://chromedevtools.github.io/devtools-protocol/tot/DOM/#method-getFrameOwner
	public static String buildPageGetFrameOwner(int id, String frameId) {
		method = "DOM.getFrameOwner";
		params = new HashMap<>();
		params.put("frameId", frameId);
		/*
		if (debug) {
			System.err.println(String.format(
					"Sending:\n{\"id\":%d,\"method\":\"Page.getFrameTree\"}", id));
		
		}
		*/
		return buildMessage(id, method, params);
	}

	public static String buildEmulationResetPageScale(int id) {
		if (debug) {
			System.err.println(String.format(
					"Sending:\n"
							+ "{\"id\":%d,\"method\":\"Emulation.resetPageScaleFactor\"}",
					id));

		}
		return buildMessage(id, "Emulation.resetPageScaleFactor");
	}
}
