package org.example.persistence.m2m;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ManyToManyService<D, E1, E2> {

    default List<D> createRelation(Map<E1, List<E2>> relationMap) {
        return relationMap.entrySet().stream()
                .map(entry -> entry.getValue().stream().map(e2 -> createRelation(entry.getKey(), e2)).toList())
                .flatMap(Collection::stream)
                .toList();
    }

    D createRelation(E1 entity1, E2 entity2);

}
