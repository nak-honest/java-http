package com.techcourse.controller;

import com.techcourse.exception.UnauthorizedException;
import com.techcourse.model.User;
import com.techcourse.service.UserService;
import java.io.IOException;
import org.apache.catalina.session.Session;
import org.apache.catalina.session.SessionManager;
import org.apache.coyote.http11.handler.HttpHandler;
import org.apache.coyote.http11.message.HttpCookie;
import org.apache.coyote.http11.message.HttpHeaderName;
import org.apache.coyote.http11.message.request.HttpRequest;
import org.apache.coyote.http11.message.response.HttpResponse;
import org.apache.coyote.http11.message.response.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginPostController implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginGetController.class);

    private final UserService service;

    public LoginPostController(UserService service) {
        this.service = service;
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        if (!request.hasFormParameters()) {
            throw new IllegalArgumentException("로그인에 필요한 데이터가 오지 않았습니다.");
        }
        String account = request.getFormParameter("account");
        String password = request.getFormParameter("password");

        try {
            User user = service.findUserByAccountAndPassword(account, password);
            log.info("로그인 성공! account: {}", user.getAccount());

            // TODO: 빌더로 생성해보기
            HttpResponse response = HttpResponse.from(HttpStatus.FOUND);

            response.setHeader(HttpHeaderName.LOCATION, "http://localhost:8080/index.html");
            if (!request.hasSession()) {
                Session session = addSession(user);
                setCookie(response, session);
            }

            return response;
        } catch (UnauthorizedException e) {
            // TODO: 예외 처리 한곳에 모으기
            HttpResponse response = HttpResponse.from(HttpStatus.FOUND);
            response.setHeader(HttpHeaderName.LOCATION, "http://localhost:8080/401.html");

            return response;
        }
    }

    private Session addSession(User user) {
        Session session = Session.create();
        session.setAttribute("user", user);
        SessionManager.getInstance()
                .add(session);

        return session;
    }

    private void setCookie(HttpResponse response, Session session) {
        HttpCookie cookie = HttpCookie.from(session);
        cookie.setJsessionid(session.getId());
        cookie.setHttpOnly(true);
        response.setCookie(cookie);
    }
}
