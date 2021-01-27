package example;
/**
 * Copyright 2020,2021 Serguei Kouzmine
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// TODO: get rid of
import com.neovisionaries.ws.client.WebSocketException;

import example.messaging.CDPClient.MessageTimeOutException;
import example.messaging.MessageBuilder;
import example.messaging.ServiceWorker;
import example.utils.Utils;

public class RuntimeTest extends BaseTest {
	private String URL = null;
	private String responseMessage = null;
	private JSONObject result = null;
	private static By locator = null;
	private static WebElement element = null;
	private static WebDriverWait wait;

	@SuppressWarnings("unchecked")
	@Test
	public void test4() {
		try {
			// Act
			int id1 = Utils.getInstance().getDynamicID();

			final String selector = "center > input.gNO89b";// "input[value='Google Search']";
			CDPClient.sendMessage(MessageBuilder.buildRuntimeEvaluateMessage(id1, selector, false));
			responseMessage = CDPClient.getResponseDataMessage(id1);
			// Assert
			result = new JSONObject(responseMessage);
			System.err.println("getRuntimeEvaluateTest Response: " + result);

			assertThat(result, notNullValue());
		} catch (Exception e) {
			System.err.println("Web Driver exception (ignored): " + e.getMessage());
		}
	}

}
