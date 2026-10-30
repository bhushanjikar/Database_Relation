package com.example.jpademo.repo;

import com.example.jpademo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository (DAO) for the "post" table.
 * Inherits the ready-made CRUD methods from JpaRepository<Post, Integer>.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    /**
     * DERIVED QUERY - Spring builds the SQL from the method name:
     *      findBy + User + Id   ->   look into the field `user` (Many-to-One) and compare its `id`
     *      SQL:  SELECT ... FROM post WHERE user_id = ?
     *
     * Returns every post written by one user (the "many" side of User 1 - * Post).
     */
    List<Post> findByUserId(Integer userId);
}
