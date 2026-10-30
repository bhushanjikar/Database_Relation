package com.example.jpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
// Lombok: @Getter / @Setter / @NoArgsConstructor replace the hand-written accessors and the empty constructor.
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Tag {
    // Primary key (MySQL AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private String name;

    // ---- MANY-TO-MANY : many Tags -- many Posts  (INVERSE side) -----------------------------
    // mappedBy = "tags" -> "the field named `tags` inside Post owns this relation and the
    //                       join table post_tags". Nothing is stored in the tag table for this.
    // READ-ONLY for the database: tag.getPosts().add(post) alone does NOT insert anything.
    // To link / unlink, change the OWNING side: post.getTags().add(tag) / .remove(tag).
    // No cascade on purpose: deleting a Tag must never delete Posts. (TagService.deleteTag first removes
    // the tag from every post in this list, then deletes the tag.)
    // fetch (default)   -> LAZY.
    // @JsonIgnore       -> avoids the Tag -> posts -> tags -> ... JSON loop.
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<Post> posts;

    public Tag(String name) {
        this.name = name;
    }
}
