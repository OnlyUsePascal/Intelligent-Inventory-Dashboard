package com.keyloop.inventory.infrastructure.security;

import com.keyloop.inventory.domain.model.Employee;
import com.keyloop.inventory.domain.model.EmployeeRole;
import com.keyloop.inventory.domain.repository.EmployeeRepository;
import com.keyloop.inventory.domain.repository.TenantRepository;
import com.keyloop.inventory.infrastructure.security.exception.MissingHeaderException;
import com.keyloop.inventory.infrastructure.security.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Security filter that extracts tenant/employee context from HTTP headers.
 * 
 * Expected header:
 * - X-User-Context: tenantId|employeeId|role (role is ADMIN, INVENTORY, SALE)
 * 
 * This filter validates:
 * 1. All required headers are present
 * 2. Tenant exists in the database
 * 3. Employee exists and belongs to the tenant
 * 4. Employee's role matches the provided role header
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_CONTEXT = "X-User-Context";

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);

    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-resources/**",
            "/webjars/**"
    );

    private final TenantRepository tenantRepository;
    private final EmployeeRepository employeeRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public TenantContextFilter(TenantRepository tenantRepository, EmployeeRepository employeeRepository) {
        this.tenantRepository = tenantRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract and validate headers
            UserContext userContext = extractUserContext(request);
            UUID tenantId = userContext.tenantId();
            UUID employeeId = userContext.employeeId();
            EmployeeRole role = userContext.role();

            // Validate tenant exists
            if (!tenantRepository.existsById(tenantId)) {
                log.warn("Invalid tenant ID: {}", tenantId);
                throw UnauthorizedException.invalidTenant();
            }

            // Validate employee exists and belongs to tenant
            Employee employee = employeeRepository.findByIdAndTenantId(employeeId, tenantId)
                    .orElseThrow(() -> {
                        log.warn("Employee {} not found for tenant {}", employeeId, tenantId);
                        return UnauthorizedException.invalidEmployee();
                    });

            // Validate role matches
            if (employee.getRole() != role) {
                log.warn("Role mismatch for employee {}. Expected: {}, Provided: {}", 
                        employeeId, employee.getRole(), role);
                throw UnauthorizedException.roleMismatch();
            }

            // Set context
            TenantContext.set(tenantId, employeeId, role);
            log.debug("Tenant context set - Tenant: {}, Employee: {}, Role: {}", tenantId, employeeId, role);

            filterChain.doFilter(request, response);
        } finally {
            // Always clear context after request processing
            TenantContext.clear();
        }
    }

    private UserContext extractUserContext(HttpServletRequest request) {
        String userContextHeader = request.getHeader(HEADER_USER_CONTEXT);
        if (userContextHeader == null || userContextHeader.isBlank()) {
            throw new MissingHeaderException(HEADER_USER_CONTEXT);
        }

        String[] parts = userContextHeader.split("\\|", -1);
        if (parts.length != 3) {
            log.warn("Invalid user context format: {}", userContextHeader);
            throw new MissingHeaderException(HEADER_USER_CONTEXT,
                    "Invalid user context header format. Expected 'tenantId|employeeId|role'.");
        }

        String tenantIdStr = parts[0].trim();
        String employeeIdStr = parts[1].trim();
        String roleStr = parts[2].trim();
        if (tenantIdStr.isBlank() || employeeIdStr.isBlank() || roleStr.isBlank()) {
            log.warn("Invalid user context value: {}", userContextHeader);
            throw new MissingHeaderException(HEADER_USER_CONTEXT,
                    "Invalid user context header value. Expected non-empty tenantId, employeeId, and role.");
        }

        UUID tenantId = parseUuid(tenantIdStr, "tenant ID");
        UUID employeeId = parseUuid(employeeIdStr, "employee ID");
        EmployeeRole role = parseRole(roleStr);

        return new UserContext(tenantId, employeeId, role);
    }

    private UUID parseUuid(String value, String label) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid {} format: {}", label, value);
            throw new MissingHeaderException(HEADER_USER_CONTEXT,
                    "Invalid user context header value. Expected UUIDs for tenantId and employeeId.");
        }
    }

    private EmployeeRole parseRole(String value) {
        try {
            return EmployeeRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role: {}", value);
            throw new MissingHeaderException(HEADER_USER_CONTEXT,
                    "Invalid user context header value. Expected role ADMIN, INVENTORY, or SALE.");
        }
    }

    private record UserContext(UUID tenantId, UUID employeeId, EmployeeRole role) {
    }
}
