package com.example.jpademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * =====================================================================================
 *  DATABASE RELATIONS CHEAT-SHEET  (read this first when you are revising)
 * =====================================================================================
 *
 *  ER DIAGRAM
 *  ----------
 *      +---------+  1            1  +-----------+
 *      |  User   |------------------|  Profile  |      ONE-TO-ONE
 *      +---------+                  +-----------+
 *           | 1
 *           |                                          ONE-TO-MANY  (User -> Post)
 *           | *                                        MANY-TO-ONE  (Post -> User)
 *      +---------+  *            *  +-----------+
 *      |  Post   |------------------|    Tag    |      MANY-TO-MANY (join table: post_tags)
 *      +---------+                  +-----------+
 *
 *  WHO OWNS WHAT  (the most important table to remember)
 *  ------------------------------------------------------------------------------------
 *  Relation       | OWNING side (writes the FK)  | INVERSE side (mappedBy)  | Where the link is stored
 *  ---------------+------------------------------+--------------------------+-----------------------
 *  User - Profile | Profile.user  @OneToOne      | User.profile             | profile.user_id
 *  User - Post    | Post.user     @ManyToOne     | User.posts @OneToMany    | post.user_id
 *  Post - Tag     | Post.tags     @ManyToMany    | Tag.posts                | post_tags(post_id, tag_id)
 *  ------------------------------------------------------------------------------------
 *
 *  GOLDEN RULES
 *  ------------
 *  1. OWNING side  = the side WITHOUT "mappedBy". Only changes made on this side are
 *                    written to the database (FK column or join table).
 *  2. INVERSE side = the side WITH "mappedBy". It is only a read-only mirror, so setting
 *                    something only on this side saves nothing.
 *                    => always set the OWNING side (post.setUser(...), profile.setUser(...),
 *                       post.getTags().add(...)). Updating the other side as well keeps the
 *                       objects in memory consistent.
 *  3. cascade      = "when I save / update / delete the parent, do the same to the child".
 *                    User -> Profile and User -> Post use CascadeType.ALL.
 *                    Post -> Tag uses only PERSIST + MERGE (tags are SHARED, never delete them
 *                    together with a post).
 *  4. fetch (default when you write nothing):
 *                    @OneToOne / @ManyToOne   -> EAGER (loaded immediately)
 *                    @OneToMany / @ManyToMany -> LAZY  (loaded on first access, needs an open
 *                                                       session -> that is why the services are @Transactional)
 *  5. @JsonIgnore  = used on back-references (Profile.user, Post.user, Post.tags, Tag.posts) so
 *                    converting an entity to JSON does not loop forever (User -> Post -> User ...).
 *
 *  PROJECT LAYERS  (a request travels top to bottom)
 *  -------------------------------------------------
 *  controller/  ->  service/  ->  repo/  ->  entity/  ->  MySQL
 *  (HTTP in/out)    (business     (Spring Data  (tables)
 *                    logic)        JPA queries)
 *
 *  NOTE ABOUT THIS CLASS'S PACKAGE
 *  -------------------------------
 *  @SpringBootApplication only scans THIS package and the packages BELOW it. This class used to
 *  live in com.example.jpademo.main, so entity / repo / service / controller (which are siblings
 *  of "main", not children) were never found. It now lives in the root package
 *  com.example.jpademo, so everything under it is scanned automatically.
 */
@SpringBootApplication
public class JpademoApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpademoApplication.class, args);
	}
}
