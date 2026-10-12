package dev.emreozcan.pazarsync.mock;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FaultInterceptor implements HandlerInterceptor {

    private final MockMarketplaceProperties properties;
    private final AtomicInteger forcedFailuresRemaining;

    public FaultInterceptor(MockMarketplaceProperties properties) {
        this.properties = properties;
        this.forcedFailuresRemaining = new AtomicInteger(properties.getFaults().getFailFirst());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        MockMarketplaceProperties.Faults faults = properties.getFaults();
        if (!faults.isEnabled() || !"GET".equals(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        boolean poll = path.endsWith("/orders") || path.endsWith("/questions");
        if (!poll) {
            return true;
        }
        if (forcedFailuresRemaining.get() > 0 && forcedFailuresRemaining.getAndDecrement() > 0) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Injected marketplace fault");
            return false;
        }
        if (ThreadLocalRandom.current().nextDouble() < faults.getFailureRate()) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Injected marketplace fault");
            return false;
        }
        return true;
    }
}
