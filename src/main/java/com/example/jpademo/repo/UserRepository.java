package com.example.jpademo.repo;

import com.example.jpademo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository (DAO) for the "user" table.
 *
 * Extending JpaRepository<User, Integer> gives ready-made CRUD methods without writing any SQL:
 *   save / saveAndFlush, findById, findAll, delete, deleteById, count, existsById ...
 *   (User = entity type, Integer = type of its primary key)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
