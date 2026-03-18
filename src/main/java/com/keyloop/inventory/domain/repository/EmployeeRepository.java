package com.keyloop.inventory.domain.repository;

import com.keyloop.inventory.domain.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface (port) for Employee entity.
 * Implementations are provided in the infrastructure layer.
 */
public interface EmployeeRepository {

    Optional<Employee> findById(UUID id);

    Optional<Employee> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Employee> findByTenantId(UUID tenantId);

    Employee save(Employee employee);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}
