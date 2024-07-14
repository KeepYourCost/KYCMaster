package com.example.kyc.security.handler;



import com.example.kyc.handler.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;


import java.io.IOException;
import java.util.HashMap;

@Component
@RequestMapping
public class AuthenticationEntryPointHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String exception = (String) request.getAttribute("exception");
        StatusCode statusCode;


        if(exception == null || exception.equals("NullPointerException")) {
            statusCode = StatusCode.UNAUTHORIZED;
            setResponse(response, statusCode);
            return;
        }

        if(exception.equals("MalformedJwtException")) {
            statusCode = StatusCode.MALFORMED;
            setResponse(response, statusCode);
            return;
        }

        if(exception.equals("PasswordNotFoundException")) {
            statusCode = StatusCode.PASSWORD_NOT_FOUND;
            setResponse(response, statusCode);
            return;
        }

        if(exception.equals("ForbiddenException")) {
            statusCode = StatusCode.FORBIDDEN;
            setResponse(response, statusCode);
            return;
        }

        //토큰이 만료된 경우
        if(exception.equals("ExpiredJwtException")) {
            statusCode = StatusCode.EXPIRED_JWT;
            setResponse(response, statusCode);
            return;
        }


    }

    private void setResponse(HttpServletResponse response, StatusCode statusCode) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        HashMap<String, Object> json = new HashMap<>();
        json.put("code", statusCode.getStatusCode());
        json.put("message", statusCode.getMessage());

        response.getWriter().print(objectMapper.writeValueAsString(json));

    }
}
