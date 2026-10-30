package com.example.jpademo.service;

import com.example.jpademo.entity.Post;
import com.example.jpademo.entity.Tag;
import com.example.jpademo.repo.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for Tag.
 *
 * Relation handled here:
 *   Tag * -- * Post  (Many-to-Many, Tag is the INVERSE side: the link table post_tags is owned by Post.tags)
 *
 * Because Tag is the inverse side, this service never ADDS a tag to a post (that is done on the owning
 * side, see PostService.addTagToPost). It only manages the tags themselves, reads which posts use a
 * tag, and - most important - cleans the link table before a tag is deleted.
 *
 * @Transactional: every method runs in one DB transaction (see UserService for the explanation).
 */
@Service
@Transactional
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * CREATE a tag (just a name). It is not attached to any post yet.
     */
    public Tag createTag(Tag tag) {
        return tagRepository.saveAndFlush(tag);
    }

    /**
     * READ one tag by id. Throws IllegalArgumentException if it does not exist.
     */
    @Transactional(readOnly = true)
    public Tag getTagById(Integer id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found with id: " + id));
    }

    /**
     * READ all tags.
     */
    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    /**
     * READ all posts that use a tag (navigating the relation from the INVERSE side).
     *
     * tag.getPosts() is LAZY, so it is only loaded when touched. Copying it into a new ArrayList does
     * that while the transaction is still open and hands the caller a plain list.
     * SQL behind it: post_tags JOIN post WHERE post_tags.tag_id = ?
     * Reading from the inverse side is fine; only WRITING must go through the owning side.
     */
    @Transactional(readOnly = true)
    public List<Post> getPostsByTagId(Integer tagId) {
        Tag tag = getTagById(tagId);
        return new ArrayList<>(tag.getPosts());
    }

    /**
     * UPDATE (rename) a tag. A null name in the request keeps the old name.
     */
    public Tag updateTag(Integer id, Tag tag) {
        Tag existing = getTagById(id);
        existing.setName(tag.getName() != null ? tag.getName() : existing.getName());
        return tagRepository.saveAndFlush(existing);
    }

    /**
     * DELETE a tag.
     *
     * Tag is the INVERSE side, so Hibernate does NOT remove its rows from the link table post_tags.
     * Deleting the tag directly would fail with a foreign-key error (post_tags.tag_id still points to it).
     * So first remove the tag from every post that uses it (owning side -> deletes the post_tags rows),
     * and only then delete the tag. The posts themselves are kept.
     */
    public void deleteTag(Integer id) {
        Tag tag = getTagById(id);
        for (Post post : tag.getPosts()) {
            post.getTags().remove(tag);    // owning side: this is what deletes the post_tags row
        }
        tagRepository.delete(tag);
    }
}
