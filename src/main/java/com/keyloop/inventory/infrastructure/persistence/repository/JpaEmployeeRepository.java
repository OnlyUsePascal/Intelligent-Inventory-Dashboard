package com.keyloop.inventory.infrastructure.persistence.repository;

import com.keyloop.inventory.domain.model.Employee;
import com.keyloop.inventory.domain.repository.EmployeeRepository;
import com.keyloop.inventory.infrastructure.persistence.mapper.EmployeePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation of EmployeeRepository using Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class JpaEmployeeRepository implements EmployeeRepository {

    private final SpringDataEmployeeRepository springDataRepository;
    private final EmployeePersistenceMapper mapper;

    @Override
    public Optional<Employee> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByIdAndTenantId(UUID id, UUID tenantId) {
        return springDataRepository.findByIdAndTenantId(id, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Employee> findByTenantId(UUID tenantId) {
        return springDataRepository.findByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Employee save(Employee employee) {
        var entity = mapper.toEntity(employee);
        var saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public boolean existsByIdAndTenantId(UUID id, UUID tenantId) {
        return springDataRepository.existsByIdAndTenantId(id, tenantId);
    }
}
