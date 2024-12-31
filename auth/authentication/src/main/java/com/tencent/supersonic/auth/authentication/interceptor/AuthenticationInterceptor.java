package com.tencent.supersonic.auth.authentication.interceptor;

import com.tencent.supersonic.auth.api.authentication.config.AuthenticationConfig;
import com.tencent.supersonic.auth.api.authentication.constant.UserConstants;
import com.tencent.supersonic.auth.authentication.service.UserServiceImpl;
import com.tencent.supersonic.auth.authentication.utils.TokenService;
import com.tencent.supersonic.common.util.S2ThreadContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.RequestFacade;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.MimeHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class AuthenticationInterceptor implements HandlerInterceptor {

    protected AuthenticationConfig authenticationConfig;

    protected UserServiceImpl userServiceImpl;

    protected TokenService tokenService;

    protected S2ThreadContext s2ThreadContext;

    protected boolean isExcludedUri(String uri) {
        String excludePathStr = authenticationConfig.getExcludePath();
        if (StringUtils.isEmpty(excludePathStr)) {
            return false;
        }
        List<String> excludePaths = Arrays.asList(excludePathStr.split(","));
        if (CollectionUtils.isEmpty(excludePaths)) {
            return false;
        }
        return excludePaths.stream().anyMatch(uri::startsWith);
    }

    protected boolean isIncludedUri(String uri) {
        String includePathStr = authenticationConfig.getIncludePath();
        if (StringUtils.isEmpty(includePathStr)) {
            return false;
        }
        List<String> includePaths = Arrays.asList(includePathStr.split(","));
        if (CollectionUtils.isEmpty(includePaths)) {
            return false;
        }
        return includePaths.stream().anyMatch(uri::startsWith);
    }

    protected boolean isInternalRequest(HttpServletRequest request) {
        String internal = request.getHeader(UserConstants.INTERNAL);
        return "true".equalsIgnoreCase(internal);
    }

    protected boolean isAppRequest(HttpServletRequest request) {
        String appId = request.getHeader(authenticationConfig.getAppId());
        return StringUtils.isNotBlank(appId);
    }

    protected void reflectSetParam(HttpServletRequest request, String key, String value) {
        try {
            if (request instanceof StandardMultipartHttpServletRequest) {
                RequestFacade servletRequest =
                        (RequestFacade) ((StandardMultipartHttpServletRequest) request)
                                .getRequest();
                Class<? extends HttpServletRequest> servletRequestClazz = servletRequest.getClass();
                Field request1 = servletRequestClazz.getDeclaredField("request");
                request1.setAccessible(true);
                Object o = request1.get(servletRequest);
                Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
                coyoteRequest.setAccessible(true);
                Object o1 = coyoteRequest.get(o);
                Field headers = o1.getClass().getDeclaredField("headers");
                headers.setAccessible(true);
                MimeHeaders o2 = (MimeHeaders) headers.get(o1);
                if (o2.getValue(key) != null) {
                    o2.setValue(key).setString(value);
                } else {
                    o2.addValue(key).setString(value);
                }
            } else {
                Class<? extends HttpServletRequest> requestClass = request.getClass();
                Field request1 = requestClass.getDeclaredField("request");
                request1.setAccessible(true);
                Object o = request1.get(request);
                Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
                coyoteRequest.setAccessible(true);
                Object o1 = coyoteRequest.get(o);
                Field headers = o1.getClass().getDeclaredField("headers");
                headers.setAccessible(true);
                MimeHeaders o2 = (MimeHeaders) headers.get(o1);
                o2.addValue(key).setString(value);
            }
        } catch (Exception e) {
            log.error("reflectSetParam error:", e);
        }
    }
}
