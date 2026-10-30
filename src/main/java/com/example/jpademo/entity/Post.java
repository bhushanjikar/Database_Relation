package com.example.jpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/*
 * POST  ->  table "post"
 * ------------------------------------------------------------------------------
 * Relations of Post (Post is the OWNING side of BOTH of them):
 *
 *   Post * ---- 1 User   Many-to-One    OWNING side  -> FK column  post.user_id
 *   Post * ---- * Tag    Many-to-Many   OWNING side  -> join table post_tags(post_id, tag_id)
 */
// Lombok: @Getter / @Setter / @NoArgsConstructor replace the hand-written accessors and the empty constructor.
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post {
    // Primary key (MySQL AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String content;

    // ---- MANY-TO-ONE : many Posts -- 1 User  (OWNING side) ----------------------------------
    // @JoinColumn(name = "user_id") -> creates the FK column `user_id` in the post table.
    // This is the field that really writes user_id, so ALWAYS call post.setUser(user) before saving.
    // fetch (default)               -> EAGER: the owner is loaded together with the post.
    // @JsonIgnore                   -> do not print the user inside a post, otherwise
    //                                  User -> posts -> User -> posts ... loops forever in JSON.
    //                                  (Side effect: "user" in an incoming JSON body is ignored too,
    //                                  so the service sets it, see PostService.createPost.)
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // ---- MANY-TO-MANY : many Posts -- many Tags  (OWNING side) ------------------------------
    // A many-to-many cannot be stored with one FK column, so a 3rd "link" table is used:
    //
    //        post               post_tags                 tag
    //     +----------+     +---------+--------+     +----------+
    //     | id (PK)  |<----| post_id | tag_id |---->| id (PK)  |
    //     +----------+     +---------+--------+     +----------+
    //
    // @JoinTable describes that link table:
    //   name               = name of the link table
    //   joinColumns        = column pointing to THIS entity  (post_id -> post.id)
    //   inverseJoinColumns = column pointing to the OTHER entity (tag_id -> tag.id)
    //
    // Post owns the relation, so add / remove tags HERE (post.getTags().add(tag)) to insert /
    // delete rows in post_tags.
    //
    // cascade = PERSIST + MERGE   (REMOVE is NOT part of it)
    //   A Tag is SHARED with other Posts, so a REMOVE cascade would fail with a foreign-key error
    //   (the other posts' rows in post_tags still point to the tag). The rule "deleting a post also
    //   deletes its tags" is done in PostService.deletePost -> TagService.deleteTag, which first
    //   detaches the tag from every post that uses it and then deletes it.
    //
    // @JsonProperty(access = READ_ONLY) -> tags ARE printed with the post (so you can see them in GET
    //                responses) but a "tags" field in an incoming JSON body is ignored.
    //                Attach tags with createPost(..., tagIds) / PostService.addTagToPost(...).
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Tag> tags;

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
