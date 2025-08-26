package org.example.persistence.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findByIdIn(Collection<Long> id);

}
