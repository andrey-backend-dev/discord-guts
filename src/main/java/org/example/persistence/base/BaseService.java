package org.example.persistence.base;

import lombok.Getter;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@Getter
public abstract class BaseService<ID, E, D, R extends CrudRepository<E, ID>, M extends BaseMapper<E, D>> {

    private final R repository;
    private final M mapper;
    
    protected BaseService(R repository, M mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public D save(E entity) {
        return mapper.entityToDto(repository.save(entity));
    }

    public D saveDto(D dto) {
        return mapper.entityToDto(repository.save(mapper.dtoToEntity(dto)));
    }

    public List<D> saveAll(List<E> entities) {
        return IterableUtils.toList(repository.saveAll(entities)).stream().map(mapper::entityToDto).toList();
    }

    public List<D> saveDtos(List<D> dtos) {
        return saveAll(dtos.stream().map(mapper::dtoToEntity).toList());
    }

    public List<D> findAll() {
        return IterableUtils.toList(repository.findAll()).stream().map(mapper::entityToDto).toList();
    }

}
