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
	private String URL = "https://www.wikipedia.org";;
	private JSONObject responseMessage = null;
	private JSONObject result = null;

	@Test
	public void test4() {
		try {
			// Act

			driver.navigate().to(URL);

			final String selector = "input";
			CDPClient.sendMessage(MessageBuilder.buildRuntimeEvaluateMessage(id, selector, false));
			// Assert
			responseMessage = new JSONObject(CDPClient.getResponseMessage(id, null));
			assertThat(responseMessage, notNullValue());
			assertThat(responseMessage.has("result"), is(true));
			result = responseMessage.getJSONObject("result");
			System.err.println("getRuntimeEvaluateTest result: " + result);
			for (String field : Arrays.asList(new String[] { "type", "className", "description" })) {
				assertThat(result.has(field), is(true));
			}

		} catch (Exception e) {
			System.err.println("Web Driver exception (ignored): " + e.getMessage());
		}
	}

}
