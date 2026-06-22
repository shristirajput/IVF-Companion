package com.ivf.companion.controller;

import com.ivf.companion.config.UserPrincipal;
import com.ivf.companion.dto.ForumCommentRequest;
import com.ivf.companion.dto.ForumPostRequest;
import com.ivf.companion.exception.ResourceNotFoundException;
import com.ivf.companion.model.ForumComment;
import com.ivf.companion.model.ForumPost;
import com.ivf.companion.model.User;
import com.ivf.companion.repository.ForumCommentRepository;
import com.ivf.companion.repository.ForumPostRepository;
import com.ivf.companion.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts() {
        List<ForumPost> posts = forumPostRepository.findAllByOrderByCreatedAtDesc();
        
        List<Map<String, Object>> response = posts.stream().map(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("isAnonymous", post.isAnonymous());
            map.put("createdAt", post.getCreatedAt());
            map.put("authorName", post.isAnonymous() ? "Anonymous Companion" : post.getAuthor().getFullName());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody ForumPostRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ForumPost post = new ForumPost();
        post.setAuthor(user);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAnonymous(request.isAnonymous());

        ForumPost saved = forumPostRepository.save(post);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        if (!forumPostRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        List<ForumComment> comments = forumCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        
        List<Map<String, Object>> response = comments.stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("postId", comment.getPost().getId());
            map.put("content", comment.getContent());
            map.put("createdAt", comment.getCreatedAt());
            map.put("authorName", comment.getPost().isAnonymous() && comment.getAuthor().getId().equals(comment.getPost().getAuthor().getId()) 
                    ? "Anonymous Companion (Author)" 
                    : comment.getAuthor().getFullName());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody ForumCommentRequest request) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ForumComment comment = new ForumComment();
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setContent(request.getContent());

        ForumComment saved = forumCommentRepository.save(comment);
        return ResponseEntity.ok(saved);
    }
}
