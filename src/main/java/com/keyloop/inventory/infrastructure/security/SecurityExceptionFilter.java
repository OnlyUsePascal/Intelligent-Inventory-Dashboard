package com.keyloop.inventory.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyloop.inventory.infrastructure.security.exception.ForbiddenException;
import com.keyloop.inventory.infrastructure.security.exception.MissingHeaderException;
import com.keyloop.inventory.infrastructure.security.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

/**
 * Filter that handles exceptions thrown from TenantContextFilter and converts them
 * to RFC 7807 Problem Details responses.
 * 
 * This filter runs before TenantContextFilter to catch any exceptions it throws.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE - 1)
@RequiredArgsConstructor
public class SecurityExceptionFilter extends OncePerRequestFilter {

    private static final String ERROR_BASE_URI = "https://api.keyloop.com/errors/";
    
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (MissingHeaderException ex) {
            writeProblemDetail(response, request, HttpStatus.BAD_REQUEST, 
                    "missing-header", "Bad Request", ex.getMessage());
        } catch (UnauthorizedException ex) {
            writeProblemDetail(response, request, HttpStatus.UNAUTHORIZED, 
                    "unauthorized", "Unauthorized", ex.getMessage());
        } catch (ForbiddenException ex) {
            writeProblemDetail(response, request, HttpStatus.FORBIDDEN, 
                    "forbidden", "Forbidden", ex.getMessage());
        }
    }

    private void writeProblemDetail(HttpServletResponse response, HttpServletRequest request,
                                     HttpStatus status, String errorType, String title, String detail) 
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(ERROR_BASE_URI + errorType));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
