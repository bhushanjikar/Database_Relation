package com.example.jpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/*
 * PROFILE  ->  table "profile"
 * ------------------------------------------------------------------------------
 * Relation of Profile:
 *
 *   Profile 1 ---- 1 User   One-to-One   OWNING side  (this table holds the FK column user_id)
 *
 * Rule of thumb: the table that contains the FK column is the OWNING side.
 * Here that is "profile", so Profile is the side that must be set when linking
 * (profile.setUser(user)).
 */
@Entity
public class Profile {
    // Primary key (MySQL AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String bio;
    private String phone;

    // ---- ONE-TO-ONE : Profile 1 -- 1 User  (OWNING side) ------------------------------------
    // @JoinColumn(name = "user_id") -> creates the FK column `user_id` in the profile table
    //                                  (points to user.id). No "mappedBy" here = this is the owner.
    // Hibernate's schema generation normally also makes user_id UNIQUE, so one user cannot get
    // two profiles (ProfileService.createProfile checks this too, to give a clear error message).
    // fetch (default)               -> EAGER: the user is loaded together with the profile.
    // @JsonIgnore                   -> do not print the user inside the profile; User already prints
    //                                  its profile, printing both would loop forever.
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // JPA needs a no-argument constructor.
    public Profile() {
    }

    public Profile(String bio, String phone, User user) {
        this.bio = bio;
        this.phone = phone;
        this.user = user;
    }

    // ---- getters & setters ----
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
