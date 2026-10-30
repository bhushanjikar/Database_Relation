package com.example.jpademo.entity;

import jakarta.persistence.*;

import java.util.List;

/*
 * USER  ->  table "user"
 * ------------------------------------------------------------------------------
 * Relations of User (User is the "parent" of both):
 *
 *   User 1 ---- 1 Profile   One-to-One   INVERSE side  (FK profile.user_id is in the PROFILE table)
 *   User 1 ---- * Post      One-to-Many  INVERSE side  (FK post.user_id    is in the POST table)
 *
 * Because User is the inverse side of both, the "user" table itself has NO profile / post column.
 * See the cheat-sheet in JpademoApplication for the full picture.
 */
@Entity
public class User {
    // Primary key. IDENTITY = MySQL AUTO_INCREMENT (the database generates the id during INSERT).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String email;

    // ---- ONE-TO-ONE : User 1 -- 1 Profile  (INVERSE side) ----------------------------------
    // mappedBy = "user"   -> "the field named `user` inside Profile owns this relation".
    //                        So the FK column lives in profile.user_id, not here.
    // cascade = ALL       -> save / update / delete of a User is automatically applied to its Profile.
    // No @JsonIgnore here -> the profile IS shown inside the User JSON
    //                        (Profile.user is @JsonIgnore, which is what stops the infinite loop).
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;

    // ---- ONE-TO-MANY : User 1 -- * Post  (INVERSE side) -------------------------------------
    // mappedBy = "user"   -> Post.user owns the relation; the FK column is post.user_id.
    // cascade = ALL       -> saving / deleting a User also saves / deletes all of its Posts.
    // fetch (default)     -> LAZY: the posts are only read from the DB when you first touch the list.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts;

    // JPA needs a no-argument constructor to create objects when it reads rows from the DB.
    public User() {
    }

    public User(String name, String email, Profile profile, List<Post> posts) {
        this.name = name;
        this.email = email;
        this.profile = profile;
        this.posts = posts;
    }

    // ---- getters & setters (used by JPA / Hibernate and by Jackson for JSON) ----------------
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
