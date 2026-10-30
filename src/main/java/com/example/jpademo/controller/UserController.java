package com.example.jpademo.controller;

import com.example.jpademo.entity.User;
import com.example.jpademo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller = the HTTP entry point. It only receives the request and calls the service;
 * all the real logic (setting up the relations) is in UserService.
 *
 * Lombok: @RequiredArgsConstructor writes the constructor for the final field(s), so Spring injects
 * the UserService automatically (constructor injection, no hand-written constructor needed).
 *
 * Base URL: /user
 *
 *   POST   /user        create a user (+ profile + posts)
 *   GET    /user        all users
 *   GET    /user/{id}   one user
 *   PUT    /user/{id}   update a user (partial update)
 *   DELETE /user/{id}   delete the user AND his profile, posts and the tags of those posts
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /user  ->  create a user together with its profile and posts (One-to-One + One-to-Many).
     *
     * Example body (profile and posts are optional):
     * {
     *   "name": "Asha",
     *   "email": "asha@example.com",
     *   "profile": { "bio": "Java learner", "phone": "9999999999" },
     *   "posts": [ { "title": "First post", "content": "Hello" } ]
     * }
     * Tags cannot be sent in this body (Post.tags is read-only in JSON); attach them afterwards via
     * POST /post/{postId}/tag/{tagId}.
     * Response: 201 CREATED with the saved user (ids filled in).
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    /** GET /user  ->  200 with the list of all users. */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** GET /user/{id}  ->  200 with the user, 404 if it does not exist. */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * PUT /user/{id}  ->  update name / email / profile / posts. Fields left out (null) keep their value.
     * A post inside "posts" WITHOUT id is added as a new post, WITH id it updates that post of this user.
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    /**
     * DELETE /user/{id}  ->  204 NO CONTENT.
     * Also deletes his profile, his posts and the tags of those posts.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
