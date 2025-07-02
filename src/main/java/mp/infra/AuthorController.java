package mp.infra;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import mp.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER') or hasRole('AUTHOR') or hasRole('ADMIN')")
    public ResponseEntity<SimpleResponse> applyAuthor(@RequestBody Author author) {
        try {
            // Spring Security에서 현재 인증된 사용자 ID 가져오기
            UUID userId = getCurrentUserId();
            author.setUserId(userId);
            
            authorService.applyAuthor(author);
            return ResponseEntity.ok(new SimpleResponse(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleResponse(false));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Author>>> listAuthors() {
        try {
            List<Author> authors = authorService.getPendingAuthors();
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "대기 중인 작가 목록을 성공적으로 조회했습니다.", authors)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "작가 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    @PatchMapping("/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SimpleResponse> reviewAuthor(@RequestBody ReviewRequest reviewRequest) {
        try {
            Optional<Author> authorOpt = authorService.reviewAuthor(
                reviewRequest.getAuthorId(), 
                reviewRequest.getStatus()
            );
            
            if (authorOpt.isPresent()) {
                return ResponseEntity.ok(new SimpleResponse(true));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SimpleResponse(false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleResponse(false));
        }
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Author>> getMyApplicationStatus() {
        try {
            UUID userId = getCurrentUserId();
            Optional<Author> authorOpt = authorService.getAuthorByUserId(userId);
            
            if (authorOpt.isPresent()) {
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "작가 신청 상태를 성공적으로 조회했습니다.", authorOpt.get())
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "작가 신청 내역을 찾을 수 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "작가 신청 상태 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = (UUID) auth.getPrincipal();
            String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            
            Map<String, Object> userInfo = Map.of(
                "user_id", userId.toString(),
                "role", role
            );
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{authorId}/userId")
    public ResponseEntity<ApiResponse<UserIdResponse>> getUserIdByAuthorId(@PathVariable UUID authorId) {
        try {
            Optional<Author> authorOpt = authorService.getAuthorById(authorId);
            
            if (authorOpt.isPresent()) {
                Author author = authorOpt.get();
                UserIdResponse response = new UserIdResponse(author.getUserId());
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "작가의 사용자 ID를 성공적으로 조회했습니다.", response)
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "해당 작가를 찾을 수 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "작가 사용자 ID 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    /**
     * Spring Security에서 현재 인증된 사용자 ID 가져오기
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }
    
    // DTO Classes
    public static class ReviewRequest {
        private UUID authorId;
        private Boolean status;
        
        public UUID getAuthorId() { return authorId; }
        public void setAuthorId(UUID authorId) { this.authorId = authorId; }
        public Boolean getStatus() { return status; }
        public void setStatus(Boolean status) { this.status = status; }
    }

    public static class SimpleResponse {
        private boolean success;

        public SimpleResponse(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public ApiResponse() {}

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }

    public static class UserIdResponse {
        private UUID userId;

        public UserIdResponse() {}

        public UserIdResponse(UUID userId) {
            this.userId = userId;
        }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
    }
}
//>>> Clean Arch / Inbound Adaptor
