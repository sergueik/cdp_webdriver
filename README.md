### Info

This project contains test scenarios practicing Java accessing [Chrome Devtools API](https://chromedevtools.github.io/devtools-protocol) during Selenium test without upgrading the Selenium driver to alpha release __4.0.x__

The code was developed from replica of [ahajamit/chrome-devtools-webdriver-integration](https://github.com/sahajamit/chrome-devtools-webdriver-integration)
Chrome DevTools WebDriver integration project with borrowing more utils and test scenarios.

For accessing the __Chrome Devtools API__  after ugrading the Selenium driver to alpha release __4.0.x__ see [Selenium CDP](https://github.com/sergueik/selenium_cdp) project

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
then probes the chrome browser available sockets by sending
```sh
http://localhost:51086/json
```
to the port the browser DevTools is listening to, in this case `51086`.
extracting the `webSocketDebuggerUrl`
from the response
```json
[
  {
    "description": "",
    "devtoolsFrontendUrl": "/devtools/inspector.html?ws=localhost:10000/devtools/page/31BCAA3827B696A389EFE222BE7F9B0E",
    "id": "31BCAA3827B696A389EFE222BE7F9B0E",
    "title": "Service Worker https://www.google.com.sg/maps/preview/sw?hl=en",
    "type": "service_worker",
    "url": "https://www.google.com.sg/maps/preview/sw?hl=en",
    "webSocketDebuggerUrl": "ws://localhost:10000/devtools/page/31BCAA3827B696A389EFE222BE7F9B0E"
  }
]

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
### Docker Testing

Currently the test described in the original repository author's [blog](https://medium.com/@sahajamit/can-selenium-chrome-dev-tools-recipe-works-inside-a-docker-container-afff92e9cce5) does not appear to work:
#### Setup
* download stock Docker image with chrome and selenium proxy
```sh
docker pull selenium/standalone-chrome
```
Note: the defauld docker image is based on ubuntu and its size is 908 Mb compared to some 384 Mb of custom alpine based image with chromium browser (which is a work in progress).

* run the Docker container with additional port published
``sh
docker run -d --expose=9222 -p 4444:4444 -p 0.0.0.0:9222:9222 --name selenium-standalone-chrome -v /dev/shm:/dev/shm selenium/standalone-chrome
```
alternatively can specify custom debugging port and omit volume:
```sh
docker container prune -f
docker run -d --expose=10000 -p 4444:4444 -p 0.0.0.0:10000:10000 --name selenium-standalone-chrome selenium/standalone-chrome

```
#### Run Test	
run the Docker tests
```sh
mvn test -DdebugPort=10000
```
will see the error:
```sh
```
### See Also:
  * intro to [headless chrome](https://developers.google.com/web/updates/2017/04/headless-chrome)
  * original project author's [blog](https://medium.com/@sahajamit/selenium-chrome-dev-tools-makes-a-perfect-browser-automation-recipe-c35c7f6a2360)
  * SeleniumHQ devtools-specific [tests](https://github.com/SeleniumHQ/selenium/tree/cdp_codegen/java/client/test/org/openqa/selenium/devtools) - one has to switch to __cdp_codegen__ branch.
### Author
[Serguei Kouzmine](kouzmine_serguei@yahoo.com)

