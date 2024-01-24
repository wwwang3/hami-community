package top.wang3.hami.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;

public interface AuthenticationPostHandler {

    void handleLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication);

    void handleError(HttpServletRequest request, HttpServletResponse response, Exception e);

    void handleLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException;
}
