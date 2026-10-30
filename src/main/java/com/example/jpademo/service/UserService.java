package com.example.jpademo.service;

import com.example.jpademo.entity.Post;
import com.example.jpademo.entity.Profile;
import com.example.jpademo.entity.User;
import com.example.jpademo.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for User (the PARENT of Profile and Post).
 *
 * Relations handled here:
 *   User 1 -- 1 Profile  (One-to-One,  User is the inverse side, cascade ALL)
 *   User 1 -- * Post     (One-to-Many, User is the inverse side, cascade ALL)
 *
 * @Transactional on the class = every public method runs inside ONE database transaction:
 *   - lazy collections (user.getPosts()) can be loaded safely inside the method,
 *   - changes made to a loaded (managed) entity are saved automatically when the method ends,
 *   - if an exception is thrown, everything is rolled back.
 * Methods that only read are marked readOnly = true (a bit faster, and they cannot write by mistake).
 *
 * Note: when a controller returns a User as JSON, Jackson reads user.getPosts() AFTER the method has
 * finished. That works because Spring Boot enables "open-in-view" by default (the DB session stays
 * open until the HTTP response is written).
 */
@Service
@Transactional
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * CREATE a user together with its profile and posts in one go (cascade ALL).
     *
     * Profile and Post are the OWNING sides (they hold the FK user_id). The JSON body cannot contain
     * the "user" field of Profile / Post (@JsonIgnore), so we set it here by hand, otherwise
     * user_id would be saved as NULL.
     *
     * Resulting SQL (order): INSERT user -> INSERT profile (user_id) -> INSERT post(s) (user_id)
     */
    public User createUser(User user){
        if(user.getProfile() != null){
            user.getProfile().setUser(user);                       // owning side of One-to-One
        }
        if(user.getPosts() != null){
            user.getPosts().forEach(post -> post.setUser(user));   // owning side of Many-to-One
        }
        return userRepository.saveAndFlush(user);                  // cascade ALL saves profile + posts too
    }

    /**
     * READ one user by id. Throws IllegalArgumentException if it does not exist.
     * The returned user carries its profile (EAGER) and its posts (LAZY, loaded when accessed).
     */
    @Transactional(readOnly = true)
    public User getUserById(Integer id){
        User user= userRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("User not found with id: " + id));
        return user;
    }

    /**
     * READ all users.
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    /**
     * UPDATE a user (partial update: every field that is null in the request is left unchanged).
     *
     *  - name / email : plain columns of the user table.
     *  - profile      : One-to-One. Updates bio / phone of the existing profile,
     *                   or creates the profile if the user does not have one yet.
     *  - posts        : One-to-Many. For each post in the request:
     *                      * no id  -> it is a NEW post, attach it to this user
     *                      * has id -> it must be one of THIS user's posts, update its title / content
     *
     * The user is loaded inside the transaction (a "managed" entity), so Hibernate notices the changed
     * fields and writes the UPDATE statements itself (dirty checking).
     */
    public User updateUser(Integer id, User user){
        User user1= userRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("User not found with id: " + id));
        user1.setName(user.getName() != null ? user.getName() : user1.getName());
        user1.setEmail(user.getEmail() != null ? user.getEmail() : user1.getEmail());

        // ---- Profile (One-to-One) ----
        if(user.getProfile() != null){
            Profile incomingProfile = user.getProfile();
            Profile existingProfile = user1.getProfile();

            if(existingProfile == null){
                // The user has no profile yet -> attach the new one (cascade ALL will INSERT it).
                incomingProfile.setUser(user1);      // owning side (writes profile.user_id)
                user1.setProfile(incomingProfile);   // inverse side (keeps the objects consistent)
            } else {
                existingProfile.setBio(incomingProfile.getBio() != null ? incomingProfile.getBio() : existingProfile.getBio());
                existingProfile.setPhone(incomingProfile.getPhone() != null ? incomingProfile.getPhone() : existingProfile.getPhone());
            }
        }

        // ---- Posts (One-to-Many) ----
        if (user.getPosts() != null){
            for (Post p: user.getPosts()){
                if (p.getId() == null) {
                    // NEW post: set the owning side (post.user_id) and add it to the user's list.
                    p.setUser(user1);
                    user1.getPosts().add(p);
                } else {
                    // EXISTING post: look for it among THIS user's posts only (so a user cannot edit
                    // somebody else's post by sending its id).
                    Post existingPost = user1.getPosts().stream()
                            .filter(post -> p.getId().equals(post.getId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + p.getId() + " for user id: " + id));
                    existingPost.setTitle(p.getTitle() != null ? p.getTitle() : existingPost.getTitle());
                    existingPost.setContent(p.getContent() != null ? p.getContent() : existingPost.getContent());
                }
            }
        }

        return userRepository.saveAndFlush(user1);
    }

    /**
     * DELETE a user.
     *
     * cascade = ALL on User.profile and User.posts means the DB rows go in this order:
     *   post_tags rows of each post (link table, removed because Post owns it)
     *   -> post rows -> profile row -> user row.
     * The Tags themselves are NOT deleted (Post.tags has no REMOVE cascade), they may belong to other posts.
     */
    public void deleteUser(Integer id){
        User user = getUserById(id);
        userRepository.delete(user);
    }

}
