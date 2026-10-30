package com.example.jpademo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

/*
 * POST  ->  table "post"
 * ------------------------------------------------------------------------------
 * Relations of Post (Post is the OWNING side of BOTH of them):
 *
 *   Post * ---- 1 User   Many-to-One    OWNING side  -> FK column  post.user_id
 *   Post * ---- * Tag    Many-to-Many   OWNING side  -> join table post_tags(post_id, tag_id)
 */
@Entity
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
    // cascade = PERSIST + MERGE   (this was CascadeType.ALL before)
    //   ALL also contains REMOVE. With REMOVE, deleting one Post would also try to delete its Tags,
    //   but a Tag is SHARED with other Posts -> foreign-key error, or the shared tag silently
    //   disappears for everybody. PERSIST + MERGE still allow "save a new Post together with new Tags".
    //
    // @JsonIgnore -> tags are not printed with the post, and cannot be sent inside a JSON body.
    //                Attach them with PostService.addTagToPost(...) / createPost(..., tagIds).
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnore
    private List<Tag> tags;

    // JPA needs a no-argument constructor.
    public Post() {
    }

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // ---- getters & setters ----
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
