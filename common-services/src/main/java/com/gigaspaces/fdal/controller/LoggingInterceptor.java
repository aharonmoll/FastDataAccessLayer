package com.gigaspaces.fdal.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @author Svitlana_Pogrebna
 *
 */
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = Logger.getLogger(LoggingInterceptor.class.getName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.info(String.format("HTTP request %s %s, HTTP headers: %s", request.getMethod(), getUrl(request), getHeaders(Collections.list(request.getHeaderNames()), name -> request.getHeader(name))));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LOGGER.info(String.format("HTTP response %s, HTTP headers: %s", response.getStatus(), getHeaders(response.getHeaderNames(), name -> response.getHeader(name))));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            LOGGER.warning(String.format("HTTP request %s %s failed with the error: %s", request.getMethod(), getUrl(request), ex.getMessage()));
        }
    }

    private String getUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getRequestURI() + (queryString != null ? queryString : StringUtils.EMPTY);
    }

    private String getHeaders(Collection<String> headerNames, Function<String, String> headerExtractor) {
        StringBuilder builder = new StringBuilder('[');
        for (String headerName : headerNames) {
            builder.append(headerName).append(": ").append(headerExtractor.apply(headerName)).append(", ");
        }
        return builder.substring(0, builder.length() - 2) + "]";
    }
}
