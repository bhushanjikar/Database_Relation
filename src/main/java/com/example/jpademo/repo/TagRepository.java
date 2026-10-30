package com.example.jpademo.repo;

import com.example.jpademo.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository (DAO) for the "tag" table.
 * Inherits the ready-made CRUD methods from JpaRepository<Tag, Integer>.
 * (The link table post_tags has no repository of its own: it is managed through Post.tags.)
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
}
