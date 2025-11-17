package org.example.persistence.feature;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeatureRepository extends CrudRepository<Feature, Long> {
    Optional<Feature> findByName(FeatureName name);
}
