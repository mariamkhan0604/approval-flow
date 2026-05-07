package com.approvalflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a user stored in the "users" MongoDB collection.
 *
 * Lombok annotations explained:
 *   @Data          → generates getters, setters, equals, hashCode, toString
 *   @Builder       → enables the builder pattern:  User.builder().name("Alice").build()
 *   @NoArgsConstructor → generates a no-argument constructor (required by MongoDB)
 *   @AllArgsConstructor → generates a constructor with all fields
 *
 * @Document(collection = "users") maps this class to the "users" collection in MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String userId;
    private String name;
    private String username;
    private String password;  
    private Role role;
}