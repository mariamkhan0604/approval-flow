package com.approvalflow.repository;

import com.approvalflow.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
/**
 * Spring Data repository for the "users" collection.
 *
 * By extending MongoRepository<User, String> we automatically get:
 *   - save(user)
 *   - findById(id)
 *   - findAll()
 *   - deleteById(id)
 *   … and many more, all without writing a single SQL/Mongo query.
 *
 * The second generic parameter (String) is the type of the @Id field (userId).
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Spring Data will auto-generate the implementation at startup.
     * Method name convention: findBy + FieldName
     * → translates to:  db.users.findOne({ name: <name> })
     */
    Optional<User> findByUsername(String username);
}
