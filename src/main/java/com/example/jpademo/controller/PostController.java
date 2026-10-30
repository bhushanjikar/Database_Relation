package com.example.jpademo.controller;

import com.example.jpademo.entity.Post;
import com.example.jpademo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Post (Many-to-One with User, Many-to-Many with Tag).
 *
 *   POST   /post/user/{userId}            create a post for a user  (optional ?tagIds=1,2 attaches existing tags)
 *   GET    /post                          all posts
 *   GET    /post/{id}                     one post (with its tags)
 *   GET    /post/user/{userId}            all posts of a user
 *   PUT    /post/{id}                     update title / content (partial update)
 *   POST   /post/{postId}/tag/{tagId}     link an existing tag to the post
 *   DELETE /post/{postId}/tag/{tagId}     unlink the tag from the post (the tag itself stays)
 *   DELETE /post/{id}                     delete the post AND its tags
 */
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * POST /post/user/{userId}?tagIds=1,2   body: { "title": "...", "content": "..." }  ->  201 CREATED.
     * tagIds is optional and must contain ids of tags that already exist.
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<Post> createPost(@PathVariable Integer userId,
                                           @RequestBody Post post,
                                           @RequestParam(required = false) List<Integer> tagIds) {
        return new ResponseEntity<>(postService.createPost(userId, post, tagIds), HttpStatus.CREATED);
    }

    /** GET /post  ->  200 with all posts. */
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    /** GET /post/{id}  ->  200 with the post, 404 if it does not exist. */
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Integer id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    /** GET /post/user/{userId}  ->  200 with all posts of that user, 404 if the user does not exist. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    /** PUT /post/{id}  ->  200 with the updated post. */
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Integer id, @RequestBody Post post) {
        return ResponseEntity.ok(postService.updatePost(id, post));
    }

    /** POST /post/{postId}/tag/{tagId}  ->  200 with the post (now including that tag). */
    @PostMapping("/{postId}/tag/{tagId}")
    public ResponseEntity<Post> addTagToPost(@PathVariable Integer postId, @PathVariable Integer tagId) {
        return ResponseEntity.ok(postService.addTagToPost(postId, tagId));
    }

    /** DELETE /post/{postId}/tag/{tagId}  ->  200 with the post (without that tag). Only the link is removed. */
    @DeleteMapping("/{postId}/tag/{tagId}")
    public ResponseEntity<Post> removeTagFromPost(@PathVariable Integer postId, @PathVariable Integer tagId) {
        return ResponseEntity.ok(postService.removeTagFromPost(postId, tagId));
    }

    /** DELETE /post/{id}  ->  204 NO CONTENT. Also deletes the tags of the post. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
