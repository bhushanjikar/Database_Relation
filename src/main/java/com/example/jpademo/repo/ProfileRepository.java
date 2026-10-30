package com.example.jpademo.repo;

import com.example.jpademo.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository (DAO) for the "profile" table.
 * Inherits the ready-made CRUD methods from JpaRepository<Profile, Integer>.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {

    /**
     * DERIVED QUERY - Spring builds the SQL from the method name:
     *      findBy + User + Id   ->   look into the field `user` (One-to-One) and compare its `id`
     *      SQL:  SELECT ... FROM profile WHERE user_id = ?
     *
     * Used to go from a User to its Profile when we only know the user's id.
     * Optional = the user may have no profile yet.
     */
    Optional<Profile> findByUserId(Integer userId);
}
