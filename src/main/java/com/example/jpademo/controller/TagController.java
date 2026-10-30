package com.example.jpademo.controller;

import com.example.jpademo.entity.Post;
import com.example.jpademo.entity.Tag;
import com.example.jpademo.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Tag (Many-to-Many with Post, Tag is the inverse side).
 * Linking a tag to a post is done on the post side: POST /post/{postId}/tag/{tagId}.
 *
 *   POST   /tag             create a tag
 *   GET    /tag             all tags
 *   GET    /tag/{id}        one tag
 *   GET    /tag/{id}/post   all posts that use the tag
 *   PUT    /tag/{id}        rename the tag
 *   DELETE /tag/{id}        delete the tag (it is first removed from every post that used it; posts stay)
 */
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /** POST /tag  body: { "name": "java" }  ->  201 CREATED. */
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        return new ResponseEntity<>(tagService.createTag(tag), HttpStatus.CREATED);
    }

    /** GET /tag  ->  200 with all tags. */
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    /** GET /tag/{id}  ->  200 with the tag, 404 if it does not exist. */
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Integer id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    /** GET /tag/{id}/post  ->  200 with all posts that use this tag. */
    @GetMapping("/{id}/post")
    public ResponseEntity<List<Post>> getPostsByTagId(@PathVariable Integer id) {
        return ResponseEntity.ok(tagService.getPostsByTagId(id));
    }

    /** PUT /tag/{id}  ->  200 with the renamed tag. */
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable Integer id, @RequestBody Tag tag) {
        return ResponseEntity.ok(tagService.updateTag(id, tag));
    }

    /** DELETE /tag/{id}  ->  204 NO CONTENT. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
