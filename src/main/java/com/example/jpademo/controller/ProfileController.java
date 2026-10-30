package com.example.jpademo.controller;

import com.example.jpademo.entity.Profile;
import com.example.jpademo.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Profile (One-to-One with User).
 * Normally a profile is created together with its user (POST /user); these endpoints work on a profile
 * on its own.
 *
 *   POST   /profile/user/{userId}   add a profile to an EXISTING user (400 if he already has one)
 *   GET    /profile                 all profiles
 *   GET    /profile/{id}            one profile
 *   GET    /profile/user/{userId}   the profile of a user
 *   PUT    /profile/{id}            update bio / phone (partial update)
 *   DELETE /profile/{id}            delete the profile only (the user stays)
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /** POST /profile/user/{userId}  body: { "bio": "...", "phone": "..." }  ->  201 CREATED. */
    @PostMapping("/user/{userId}")
    public ResponseEntity<Profile> createProfile(@PathVariable Integer userId, @RequestBody Profile profile) {
        return new ResponseEntity<>(profileService.createProfile(userId, profile), HttpStatus.CREATED);
    }

    /** GET /profile  ->  200 with all profiles. */
    @GetMapping
    public ResponseEntity<List<Profile>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    /** GET /profile/{id}  ->  200 with the profile, 404 if it does not exist. */
    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Integer id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    /** GET /profile/user/{userId}  ->  200 with the profile of that user, 404 if there is none. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Profile> getProfileByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    /** PUT /profile/{id}  ->  200 with the updated profile. */
    @PutMapping("/{id}")
    public ResponseEntity<Profile> updateProfile(@PathVariable Integer id, @RequestBody Profile profile) {
        return ResponseEntity.ok(profileService.updateProfile(id, profile));
    }

    /** DELETE /profile/{id}  ->  204 NO CONTENT. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Integer id) {
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
