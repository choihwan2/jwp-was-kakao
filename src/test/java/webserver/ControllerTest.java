package webserver;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import utils.FileIoUtils;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static webserver.RequestHandler.getBasePath;

class ControllerTest {
    @Test
    void userCreateController() {
        HttpRequest httpRequest = createRequest("/usr/create");
        httpRequest.setEntity(ImmutableMap.of("userId", "red"));
        assertThat(new CreateUserController().execute(httpRequest).getHeaders())
                .containsExactly("Location: /index.html");
    }

    @Test
    void loginControllerWhenSuccess() {
        HttpRequest httpRequest = createRequest("/user/login");
        httpRequest.setEntity(ImmutableMap.of("userId", "blue", "password", "1234"));
        assertThat(new LoginController().execute(httpRequest).getHeaders())
                .containsExactly("Set-Cookie: logined=true; Path=/", "Location: /index.html");
    }

    @Test
    void loginControllerWhenFailed() {
        HttpRequest httpRequest = createRequest("/user/login");
        httpRequest.setEntity(ImmutableMap.of("userId", "blue", "password", "0000"));
        assertThat(new LoginController().execute(httpRequest).getHeaders())
                .containsExactly("Set-Cookie: logined=false; Path=/", "Location: /user/login_failed.html");
    }

    @Test
    void userListControllerWhenLoggedIn() {
        HttpRequest httpRequest = createRequest("/user/list");
        httpRequest.addHeaders(ImmutableMap.of("Cookie", "logined=true"));
        Response response = new UserListController().execute(httpRequest);
        assertAll(() -> assertThat(response.getViewName()).isEqualTo("user/list"),
                  () -> assertThat(response.getModel()).isNotNull());
    }

    @Test
    void userListControllerWhenNotLoggedIn() {
        HttpRequest httpRequest = createRequest("/user/list");
        assertThat(new UserListController().execute(httpRequest).getHeaders())
                .containsExactly("Location: /user/login.html");
    }

    @Test
    void staticContentController() {
        StaticContentController controller = new StaticContentController();
        assertAll(() -> {
                      Response response = controller.execute(createRequest("/css/bootstrap.min.css"));
                      assertThat(response.getHeaders()).containsExactly("Content-Type: text/css");
                  },
                  () -> {
                      Response response = controller.execute(createRequest("/js/jquery-2.2.0.min.js"));
                      assertThat(response.getHeaders()).containsExactly("Content-Type: application/js");
                  });
    }

    private HttpRequest createRequest(String uri) {
        return new HttpRequest("POST", uri, "HTTP 1.1");
    }

    private static class StaticContentController implements Controller {
        @Override
        public Response execute(HttpRequest httpRequest) throws Exception {
            String requestURI = httpRequest.getRequestURI();
            byte[] body = FileIoUtils.loadFileFromClasspath(getBasePath(requestURI) + requestURI);
            Response response = new Response();
            response.setBody(body);
            response.setHeaders("Content-Type: " + MimeType.fromFileName(requestURI).getMimeTypeValue());
            return response;
        }
    }

    enum MimeType {
        APPLICATION_JS("application/js", "js"),
        TEXT_CSS("text/css", "css"),
        IMAGE_PNG("image/png", "png"),
        IMAGE_SVG("image/svg+xml", "svg"),
        FONT_TTF("font/ttf", "ttf"),
        FONT_WOFF("font/woff", "woff"),
        FONT_WOFF2("font/woff2", "woff2"),
        FONT_EOT("application/vnd.ms-fontobject", "eot"),
        TEXT_HTML("text/html", "html");

        private final String mimeTypeValue;
        private final String fileExtension;

        MimeType(String mimeTypeValue, String fileExtension) {
            this.mimeTypeValue = mimeTypeValue;
            this.fileExtension = fileExtension;
        }

        public String getMimeTypeValue() {
            return mimeTypeValue;
        }

        public static MimeType fromFileName(String fileName) {
            return Stream.of(values())
                    .filter(mimeType -> fileName.endsWith(mimeType.fileExtension))
                    .findAny()
                    .orElse(MimeType.TEXT_HTML);
        }
    }
}
