package com.example.jpademo.controller;

import com.example.jpademo.entity.User;
import com.example.jpademo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller = the HTTP entry point. It only receives the request and calls the service;
 * all the real logic (setting up the relations) is in UserService.
 *
 * Base URL: /user
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    // Constructor injection: Spring passes the UserService bean in automatically.
    public UserController(UserService userService) {
        this.userService = userService;
    }

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
     * Tags cannot be sent in this body (Post.tags is @JsonIgnore); attach them afterwards via PostService.
     * Response: 201 CREATED with the saved user (ids filled in).
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }
}
