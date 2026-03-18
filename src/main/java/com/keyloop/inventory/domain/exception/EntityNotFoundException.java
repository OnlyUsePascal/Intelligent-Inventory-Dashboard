package com.keyloop.inventory.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested entity is not found.
 */
public class EntityNotFoundException extends DomainException {

    private final String entityType;
    private final String identifier;

    public EntityNotFoundException(String entityType, UUID id) {
        super(String.format("%s not found with id: %s", entityType, id));
        this.entityType = entityType;
        this.identifier = id.toString();
    }

    public EntityNotFoundException(String entityType, String field, String value) {
        super(String.format("%s not found with %s: %s", entityType, field, value));
        this.entityType = entityType;
        this.identifier = field + "=" + value;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getIdentifier() {
        return identifier;
    }
}
