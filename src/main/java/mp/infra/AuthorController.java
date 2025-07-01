package mp.infra;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import mp.domain.*;
import mp.security.JwtVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;    // ← 추가

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/authors")
@RequiredArgsConstructor
public class AuthorController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);

    @Autowired
    private AuthorService authorService;
    private final JwtVerifier jwtVerifier;

    @PostMapping("/apply")
    public ResponseEntity<SimpleResponse> applyAuthor(@RequestBody Author author) {
        try {
            authorService.applyAuthor(author);
            return ResponseEntity.ok(new SimpleResponse(true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleResponse(false));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Author>>> listAuthors(@RequestHeader("Authorization") String authHeader) {
        logger.info("작가 목록 조회 요청 시작");
        
        try {
            // JWT 검증 및 ADMIN 역할 확인
            String token = authHeader.replace("Bearer ", "");
            logger.info("JWT 토큰 추출 완료");
            
            Map<String, Object> userInfo = jwtVerifier.verifyAndExtract(token);
            logger.info("JWT 검증 완료 - 사용자: {}, 역할: {}", userInfo.get("name"), userInfo.get("role"));
            
            String role = (String) userInfo.get("role");
            if (!"ADMIN".equals(role)) {
                logger.warn("ADMIN 권한 없음 - 사용자: {}, 역할: {}", userInfo.get("name"), role);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "ADMIN 권한이 필요합니다.", null));
            }
            
            logger.info("ADMIN 권한 확인 완료 - 작가 목록 조회 진행");
            List<Author> authors = authorService.getAllAuthors();
            logger.info("작가 목록 조회 완료 - 총 {}명", authors.size());
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "작가 목록을 성공적으로 조회했습니다.", authors)
            );
        } catch (Exception e) {
            logger.error("작가 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "작가 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    @PatchMapping("/review")
    public ResponseEntity<SimpleResponse> reviewAuthor(@RequestBody ReviewRequest reviewRequest, @RequestHeader("Authorization") String authHeader) {
        logger.info("작가 심사 요청 시작 - 작가ID: {}, 승인여부: {}", reviewRequest.getAuthorId(), reviewRequest.getStatus());
        
        try {
            // JWT 검증 및 ADMIN 역할 확인
            String token = authHeader.replace("Bearer ", "");
            logger.info("JWT 토큰 추출 완료");
            
            Map<String, Object> userInfo = jwtVerifier.verifyAndExtract(token);
            logger.info("JWT 검증 완료 - 사용자: {}, 역할: {}", userInfo.get("name"), userInfo.get("role"));
            
            String role = (String) userInfo.get("role");
            if (!"ADMIN".equals(role)) {
                logger.warn("ADMIN 권한 없음 - 사용자: {}, 역할: {}", userInfo.get("name"), role);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SimpleResponse(false));
            }
            
            logger.info("ADMIN 권한 확인 완료 - 작가 심사 진행");
            Optional<Author> authorOpt = authorService.reviewAuthor(
                reviewRequest.getAuthorId(), 
                reviewRequest.getStatus()
            );
            
            if (authorOpt.isPresent()) {
                logger.info("작가 심사 완료 - 작가ID: {}, 결과: {}", reviewRequest.getAuthorId(), reviewRequest.getStatus());
                return ResponseEntity.ok(new SimpleResponse(true));
            } else {
                logger.warn("작가를 찾을 수 없음 - 작가ID: {}", reviewRequest.getAuthorId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SimpleResponse(false));
            }
        } catch (Exception e) {
            logger.error("작가 심사 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleResponse(false));
        }
    }

    @GetMapping("/user")
    public Map<String, Object> getUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtVerifier.verifyAndExtract(token);
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
}
//>>> Clean Arch / Inbound Adaptor
