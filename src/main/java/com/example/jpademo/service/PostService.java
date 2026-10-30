package com.example.jpademo.service;

import com.example.jpademo.entity.Post;
import com.example.jpademo.entity.Tag;
import com.example.jpademo.entity.User;
import com.example.jpademo.repo.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Business logic for Post.
 *
 * Relations handled here (Post is the OWNING side of both):
 *   Post * -- 1 User  (Many-to-One,  FK column post.user_id)
 *   Post * -- * Tag   (Many-to-Many, link table post_tags)
 *
 * So everything that links a post to a user or to tags is written HERE, on the Post object.
 *
 * @Transactional: every method runs in one DB transaction (see UserService for the explanation).
 */
@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    // Used to load the owner / the tags (re-uses their "not found" error messages).
    private final UserService userService;
    private final TagService tagService;

    public PostService(PostRepository postRepository, UserService userService, TagService tagService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.tagService = tagService;
    }

    /**
     * CREATE a post for an existing user, without tags.
     *
     * @param userId id of the author
     * @param post   new post (title / content)
     */
    public Post createPost(Integer userId, Post post) {
        return createPost(userId, post, Collections.emptyList());
    }

    /**
     * CREATE a post for an existing user and attach EXISTING tags in the same call.
     *
     * Resulting SQL: INSERT post (with user_id) -> INSERT post_tags row for every tag.
     *
     * @param userId id of the author               -> sets the Many-to-One owning side (post.user_id)
     * @param post   new post (title / content)
     * @param tagIds ids of tags that already exist -> sets the Many-to-Many owning side (post_tags)
     */
    public Post createPost(Integer userId, Post post, List<Integer> tagIds) {
        User user = userService.getUserById(userId);
        post.setUser(user);                                  // owning side of Many-to-One

        List<Tag> tags = new ArrayList<>();
        if (tagIds != null) {
            for (Integer tagId : tagIds) {
                Tag tag = tagService.getTagById(tagId);
                if (!tags.contains(tag)) {                   // ignore a tag id sent twice
                    tags.add(tag);
                }
            }
        }
        post.setTags(tags);                                  // owning side of Many-to-Many

        return postRepository.saveAndFlush(post);
    }

    /**
     * READ one post by id. Throws IllegalArgumentException if it does not exist.
     */
    @Transactional(readOnly = true)
    public Post getPostById(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + id));
    }

    /**
     * READ all posts.
     */
    @Transactional(readOnly = true)
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    /**
     * READ all posts of one user (the "many" side of User 1 - * Post).
     * Uses the derived query PostRepository.findByUserId  ->  WHERE post.user_id = ?
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByUserId(Integer userId) {
        userService.getUserById(userId);            // throws "User not found" for an unknown user
        return postRepository.findByUserId(userId);
    }

    /**
     * UPDATE title / content (partial update: null fields in the request keep their old value).
     * The author and the tags are not touched here (use addTagToPost / removeTagFromPost for tags).
     */
    public Post updatePost(Integer id, Post post) {
        Post existing = getPostById(id);
        existing.setTitle(post.getTitle() != null ? post.getTitle() : existing.getTitle());
        existing.setContent(post.getContent() != null ? post.getContent() : existing.getContent());
        return postRepository.saveAndFlush(existing);
    }

    /**
     * LINK a post and a tag (Many-to-Many).
     * Adds one row (post_id, tag_id) to the link table post_tags. Does nothing if already linked.
     */
    public Post addTagToPost(Integer postId, Integer tagId) {
        Post post = getPostById(postId);
        Tag tag = tagService.getTagById(tagId);

        boolean alreadyLinked = post.getTags().stream().anyMatch(t -> t.getId().equals(tagId));
        if (!alreadyLinked) {
            post.getTags().add(tag);                // owning side: this is what inserts into post_tags
        }
        return postRepository.saveAndFlush(post);
    }

    /**
     * UNLINK a post and a tag (Many-to-Many).
     * Deletes the row (post_id, tag_id) from post_tags. The tag and the post both stay.
     */
    public Post removeTagFromPost(Integer postId, Integer tagId) {
        Post post = getPostById(postId);
        post.getTags().removeIf(t -> t.getId().equals(tagId));   // owning side: deletes the post_tags row
        return postRepository.saveAndFlush(post);
    }

    /**
     * DELETE a post.
     *
     *  - Its rows in the link table post_tags are deleted automatically (Post owns that table).
     *  - The Tags are kept: Post.tags has no REMOVE cascade, and other posts may still use them.
     *  - The author is kept. We remove the post from user.getPosts() first, because User.posts has
     *    cascade = ALL: a post that is still in that (loaded) list could be "re-saved" by the cascade
     *    and the delete would be cancelled. Keeping both sides in sync avoids that.
     */
    public void deletePost(Integer id) {
        Post post = getPostById(id);
        User owner = post.getUser();
        if (owner != null && owner.getPosts() != null) {
            owner.getPosts().remove(post);
        }
        postRepository.delete(post);
    }
}
