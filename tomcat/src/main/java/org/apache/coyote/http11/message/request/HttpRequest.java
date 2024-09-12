package org.apache.coyote.http11.message.request;

import java.util.Optional;
import org.apache.catalina.session.Session;
import org.apache.catalina.session.SessionManager;
import org.apache.coyote.http11.message.HttpCookie;
import org.apache.coyote.http11.message.HttpHeaderName;
import org.apache.coyote.http11.message.HttpHeaders;

public class HttpRequest {

    private static final String REQUEST_LINE_DELIMITER = " ";
    private static final int HTTP_METHOD_INDEX = 0;
    private static final int HTTP_URL_INDEX = 1;

    private final HttpMethod method;
    private final HttpUrl url;
    private final HttpHeaders headers;
    private final HttpRequestBody body;

    public HttpRequest(HttpMethod method, HttpUrl url, HttpHeaders headers, HttpRequestBody body) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
    }

    public HttpRequest(HttpMethod method, HttpUrl url) {
        this(method, url, new HttpHeaders(), new HttpRequestBody());
    }

    public static HttpRequest of(String requestLine, HttpHeaders headers, HttpRequestBody body) {
        String[] requestLineElements = requestLine.split(REQUEST_LINE_DELIMITER);
        HttpMethod method = HttpMethod.from(requestLineElements[HTTP_METHOD_INDEX]);
        String url = requestLineElements[HTTP_URL_INDEX];

        return new HttpRequest(method, HttpUrlParser.parseUrl(url), headers, body);
    }

    public boolean hasFormParameters() {
        return body.hasFormParameters();
    }

    public boolean hasQueryString() {
        return url.hasQueryString();
    }

    public boolean hasHeader(String header) {
        return headers.hasHeader(header);
    }

    public boolean hasSession() {
        return getSession().isPresent();
    }

    public HttpCookie getCookie() {
        return headers.getCookie();
    }

    public String getFormParameter(String key) {
        return body.getFormParameter(key);
    }

    public String getQueryParameter(String key) {
        return url.getQueryParameter(key);
    }

    public HttpRequestInfo getRequestInfo() {
        return new HttpRequestInfo(method, getUrlPath());
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUrlPath() {
        return url.getPath();
    }

    public Optional<String> getHeaderFieldByName(HttpHeaderName name) {
        return headers.getFieldByHeaderName(name);
    }

    public Optional<Session> getSession() {
        HttpCookie cookie = headers.getCookie();
        return Optional.ofNullable(SessionManager.getInstance()
                .findSession(cookie.getJsessionid()));
    }
}
