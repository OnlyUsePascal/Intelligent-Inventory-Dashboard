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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Expected headers:
 * - X-Tenant-Id: UUID of the tenant (dealership)
 * - X-Employee-Id: UUID of the employee making the request
 * - X-Employee-Role: Role of the employee (ADMIN, INVENTORY, SALE)
 * 
 * This filter validates:
 * 1. All required headers are present
 * 2. Tenant exists in the database
 * 3. Employee exists and belongs to the tenant
 * 4. Employee's role matches the provided role header
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_EMPLOYEE_ID = "X-Employee-Id";
    public static final String HEADER_EMPLOYEE_ROLE = "X-Employee-Role";

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
            UUID tenantId = extractTenantId(request);
            UUID employeeId = extractEmployeeId(request);
            EmployeeRole role = extractRole(request);

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

    private UUID extractTenantId(HttpServletRequest request) {
        String tenantIdStr = request.getHeader(HEADER_TENANT_ID);
        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            throw new MissingHeaderException(HEADER_TENANT_ID);
        }
        try {
            return UUID.fromString(tenantIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid tenant ID format: {}", tenantIdStr);
            throw new MissingHeaderException(HEADER_TENANT_ID);
        }
    }

    private UUID extractEmployeeId(HttpServletRequest request) {
        String employeeIdStr = request.getHeader(HEADER_EMPLOYEE_ID);
        if (employeeIdStr == null || employeeIdStr.isBlank()) {
            throw new MissingHeaderException(HEADER_EMPLOYEE_ID);
        }
        try {
            return UUID.fromString(employeeIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid employee ID format: {}", employeeIdStr);
            throw new MissingHeaderException(HEADER_EMPLOYEE_ID);
        }
    }

    private EmployeeRole extractRole(HttpServletRequest request) {
        String roleStr = request.getHeader(HEADER_EMPLOYEE_ROLE);
        if (roleStr == null || roleStr.isBlank()) {
            throw new MissingHeaderException(HEADER_EMPLOYEE_ROLE);
        }
        try {
            return EmployeeRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role: {}", roleStr);
            throw new MissingHeaderException(HEADER_EMPLOYEE_ROLE);
        }
    }
}
