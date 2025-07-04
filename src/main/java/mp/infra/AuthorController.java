package mp.infra;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import mp.domain.*;
import mp.util.UserHeaderUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping("/apply")
    public ResponseEntity<SimpleResponse> applyAuthor(@RequestBody Author author, HttpServletRequest request) {
        try {
            // 권한 체크 (USER, AUTHOR, ADMIN 중 하나 필요)
            if (!UserHeaderUtil.hasAnyRole(request, "USER", "AUTHOR", "ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SimpleResponse(false));
            }
            
            // Gateway에서 전달한 사용자 ID 가져오기
            UUID userId = UserHeaderUtil.getUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SimpleResponse(false));
            }
            
            author.setUserId(userId);
            authorService.applyAuthor(author);
            return ResponseEntity.ok(new SimpleResponse(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleResponse(false));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Author>>> listAuthors(HttpServletRequest request) {
        try {
            // 관리자 권한 체크
            if (!UserHeaderUtil.isAdmin(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "관리자 권한이 필요합니다.", null));
            }
            
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
    public ResponseEntity<SimpleResponse> reviewAuthor(@RequestBody ReviewRequest reviewRequest, HttpServletRequest request) {
        try {
            // 관리자 권한 체크
            if (!UserHeaderUtil.isAdmin(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SimpleResponse(false));
            }
            
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
    public ResponseEntity<ApiResponse<Author>> getMyApplicationStatus(HttpServletRequest request) {
        System.out.println("=== AuthorController.getMyApplicationStatus() ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        
        try {
            System.out.println("Checking authentication...");
            
            // 인증 확인
            if (!UserHeaderUtil.isAuthenticated(request)) {
                System.out.println("Authentication failed - returning 401");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "인증이 필요합니다.", null));
            }
            
            System.out.println("Authentication successful - getting user info...");
            UUID userId = UserHeaderUtil.getUserId(request);
            System.out.println("Looking up author for user ID: " + userId);
            
            Optional<Author> authorOpt = authorService.getAuthorByUserId(userId);
            
            if (authorOpt.isPresent()) {
                System.out.println("Author found - returning success response");
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "작가 신청 상태를 성공적으로 조회했습니다.", authorOpt.get())
                );
            } else {
                System.out.println("Author not found for user ID: " + userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "작가 신청 내역을 찾을 수 없습니다.", null));
            }
        } catch (Exception e) {
            System.err.println("Error in getMyApplicationStatus: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "작가 신청 상태 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser(HttpServletRequest request) {
        try {
            // 인증 확인
            if (!UserHeaderUtil.isAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "인증이 필요합니다."));
            }
            
            UUID userId = UserHeaderUtil.getUserId(request);
            String role = UserHeaderUtil.getUserRole(request);
            
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

    // Gateway에서 전달한 헤더 정보로 사용자 정보를 가져오므로 getCurrentUserId() 메서드 제거
    
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
