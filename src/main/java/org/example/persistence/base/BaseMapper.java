package org.example.persistence.base;

public interface BaseMapper<E, D> {
    D entityToDto(E entity);
    E dtoToEntity(D dto);
}
