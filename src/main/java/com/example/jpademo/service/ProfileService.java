package com.example.jpademo.service;

import com.example.jpademo.entity.Profile;
import com.example.jpademo.entity.User;
import com.example.jpademo.repo.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for Profile.
 *
 * Relation handled here:
 *   Profile 1 -- 1 User  (One-to-One, Profile is the OWNING side: table "profile" holds user_id)
 *
 * Normally a profile is created together with its user (UserService.createUser). This service is for
 * working with a profile on its own: add one to an EXISTING user, read it, update it, delete it.
 *
 * @Transactional: every method runs in one DB transaction (see UserService for the explanation).
 */
@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    // Used to load the owner of the profile (re-uses the "User not found" error message).
    private final UserService userService;

    public ProfileService(ProfileRepository profileRepository, UserService userService) {
        this.profileRepository = profileRepository;
        this.userService = userService;
    }

    /**
     * CREATE a profile for an existing user.
     * A user can have only ONE profile (One-to-One), so we refuse if it already has one.
     *
     * @param userId  id of the user that will own this profile
     * @param profile new profile (bio / phone)
     */
    public Profile createProfile(Integer userId, Profile profile) {
        User user = userService.getUserById(userId);
        if (user.getProfile() != null) {
            throw new IllegalArgumentException("User with id: " + userId + " already has a profile");
        }
        profile.setUser(user);       // OWNING side -> this is what writes profile.user_id
        user.setProfile(profile);    // INVERSE side -> keeps the two objects consistent in memory
        return profileRepository.saveAndFlush(profile);
    }

    /**
     * READ a profile by its own id.
     */
    @Transactional(readOnly = true)
    public Profile getProfileById(Integer id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found with id: " + id));
    }

    /**
     * READ the profile that belongs to a user (goes through the FK profile.user_id).
     * See ProfileRepository.findByUserId.
     */
    @Transactional(readOnly = true)
    public Profile getProfileByUserId(Integer userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user id: " + userId));
    }

    /**
     * READ all profiles.
     */
    @Transactional(readOnly = true)
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    /**
     * UPDATE bio / phone (partial update: null fields in the request keep their old value).
     * The owner (user) is NOT changed here, a profile stays with the user it was created for.
     */
    public Profile updateProfile(Integer id, Profile profile) {
        Profile existing = getProfileById(id);
        existing.setBio(profile.getBio() != null ? profile.getBio() : existing.getBio());
        existing.setPhone(profile.getPhone() != null ? profile.getPhone() : existing.getPhone());
        return profileRepository.saveAndFlush(existing);
    }

    /**
     * DELETE a profile. The user itself is kept.
     *
     * Why we first do user.setProfile(null):
     * User.profile has cascade = ALL. If the user (already loaded) still pointed to the profile we are
     * deleting, Hibernate would find the "deleted" profile through that cascade at flush time and
     * cancel / complain about the delete. Un-linking both sides first avoids that.
     */
    public void deleteProfile(Integer id) {
        Profile profile = getProfileById(id);
        User user = profile.getUser();
        if (user != null) {
            user.setProfile(null);
        }
        profileRepository.delete(profile);
    }
}
