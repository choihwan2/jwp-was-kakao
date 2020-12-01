package webserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> parameters;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(HttpMethod method, String path, Map<String, String> paramters, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.parameters = paramters;
        this.headers = headers;
        this.body = body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getBodyInMap() throws IOException {
        if (!headers.containsKey(ContentType.CONTENT_TYPE)) {
            throw new UnsupportedOperationException("HttpRequest does not support form body");
        }
        String rawContentType = headers.get(ContentType.CONTENT_TYPE);

        // TODO: 호출할때마다 객체 생성
        if (ContentType.APPLICATION_FORM_URLENCODED.isEqualTo(rawContentType)) {
            return FormUrlencodedBodyParser.parse(body);
        }

        throw new UnsupportedOperationException("HttpRequest does not support form body, Content-Type=" + rawContentType);
    }

}
