### Info

This project contains a test scenarios practicing Java accessing the [Chrome Devtools API]() during Selenium test without upgrading the Selenium driver to alpha release __4.0.x__

The code was developing from replica of [ahajamit/chrome-devtools-webdriver-integration](https://github.com/sahajamit/chrome-devtools-webdriver-integration)
Chrome DevTools WebDriver integration project with borrowing more utils and test scenarios.

### Operation

The custom driver extension examines the chrome log file located in
```java
System.getProperty("user.dir")  + "/target/chromedriver.log"
```
and finds the line
```sh
[1587217990.273][INFO]: Launching chrome: "C:\Program Files (x86)\Google\Chrome\Application\chrome.exe" --disable-background-networking --disable-client-side-phishing-detection --disable-default-apps --disable-extensions --disable-hang-monitor --disable-popup-blocking --disable-prompt-on-repost --disable-sync --enable-automation --enable-blink-features=ShadowDOMV0 --enable-logging --ignore-certificate-errors --ignore-ssl-errors=true --log-level=0 --no-first-run --password-store=basic --remote-debugging-port=0 --ssl-protocol=any --start-maximized --test-type=webdriver --use-mock-keychain --user-data-dir="C:\Users\Serguei\AppData\Local\Temp\scoped_dir5740_1744005879" data:,
[1587217990.738][DEBUG]: DevTools HTTP Request: http://localhost:51086/json/version
```
and constructs a socket for that port using `com.neovisionaries.ws.client.WebSocket`.
The actual CDP commands and responses are posted to and read from that socket. The `MessageBuilder` class is used to deal with JSON conversion of session id and various message parameters using [gson](https://github.com/google/gson/blob/master/UserGuide.md), e.g.

```java
private static String buildMessage(int id, String method Map<String, Object> params) {
  final Gson gson = new Gson();
  message = new Message(id, method);
  for (String key : params.keySet()) {
    message.addParam(key, params.get(key));
  }
  return gson.toJson(message);
}
```
where `Message` is a generic class with properties `id`, `method`, and `params`.
this for an e.g. `Emulation.setGeolocationOverride` creates payload which looks like:
```json

{
  "id": 196822,
  "method": "Emulation.setGeolocationOverride",
  "params": {
    "latitude": 37.42229,
    "longitude": -122.084057,
    "accuracy": 100
  }
}
```     
### Author
[Serguei Kouzmine](kouzmine_serguei@yahoo.com)

