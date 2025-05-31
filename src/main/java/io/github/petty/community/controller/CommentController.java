package io.github.petty.community.controller;

import io.github.petty.community.dto.CommentRequest;
import io.github.petty.community.dto.CommentResponse;
import io.github.petty.community.service.CommentService;
import io.github.petty.users.dto.CustomUserDetails;
import io.github.petty.users.entity.Users;
import io.github.petty.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UsersRepository usersRepository;

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                        @RequestBody CommentRequest request,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = null;
        try {
            username = userDetails.getUsername();
            Users user = usersRepository.findByUsername(username);

            log.info("댓글 등록 시작 - postId: {}, user: {}", postId, username);

            Long commentId = commentService.addComment(postId, request, user);

            log.info("댓글 등록 완료 - commentId: {}", commentId);

            return ResponseEntity.ok().body(commentId);
       } catch (IllegalArgumentException e) {
           log.warn("댓글 등록 실패 - 잘못된 요청: {}", e.getMessage());
           return ResponseEntity.badRequest().body("잘못된 요청입니다: " + e.getMessage());
       } catch (Exception e) {
           log.error("댓글 등록 실패", e);
           return ResponseEntity.badRequest().body("댓글 등록에 실패했습니다.");
        }
    }

    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestBody CommentRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = userDetails.getUsername();
        Users user = usersRepository.findByUsername(username);
        commentService.updateComment(commentId, request, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        String username = null;
        try {
            username = userDetails.getUsername();
            Users user = usersRepository.findByUsername(username);

            log.info("댓글 삭제 시작 - commentId: {}, user: {}", commentId, username);
            commentService.deleteComment(commentId, user);

            log.info("댓글 삭제 완료 - commentId: {}", commentId);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("댓글 삭제 실패 - commentId: {}, user: {}", commentId, username, e);
            return ResponseEntity.status(500).body("댓글 삭제에 실패했습니다.");
        }
    }
}
