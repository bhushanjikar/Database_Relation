package com.example.jpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

/*
 * TAG  ->  table "tag"
 * ------------------------------------------------------------------------------
 * Relation of Tag:
 *
 *   Tag * ---- * Post   Many-to-Many   INVERSE side  (the link table post_tags is owned by Post.tags)
 *
 * A Tag is a small reusable label (e.g. "java", "jpa") that many posts can share.
 */
@Entity
public class Tag {
    // Primary key (MySQL AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    // ---- MANY-TO-MANY : many Tags -- many Posts  (INVERSE side) -----------------------------
    // mappedBy = "tags" -> "the field named `tags` inside Post owns this relation and the
    //                       join table post_tags". Nothing is stored in the tag table for this.
    // READ-ONLY for the database: tag.getPosts().add(post) alone does NOT insert anything.
    // To link / unlink, change the OWNING side: post.getTags().add(tag) / .remove(tag).
    // No cascade on purpose: deleting a Tag must never delete Posts.
    // fetch (default)   -> LAZY.
    // @JsonIgnore       -> avoids the Tag -> posts -> tags -> ... JSON loop.
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<Post> posts;

    // JPA needs a no-argument constructor.
    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    // ---- getters & setters ----
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

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
