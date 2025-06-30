package mp.infra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import mp.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//<<< Clean Arch / Inbound Adaptor

@RestController
@RequestMapping(value="/authors")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

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
    public ResponseEntity<ApiResponse<List<Author>>> listAuthors() {
        try {
            List<Author> authors = authorService.getAllAuthors();
            return ResponseEntity.ok(
                new ApiResponse<>(true, "작가 목록을 성공적으로 조회했습니다.", authors)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "작가 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }

    @PatchMapping("/review")
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

    @GetMapping("/{authorId}")
    public ResponseEntity<ApiResponse<Author>> getAuthor(@PathVariable UUID authorId) {
        try {
            Optional<Author> authorOpt = authorService.getAuthorById(authorId);
            
            if (authorOpt.isPresent()) {
                return ResponseEntity.ok(
                    new ApiResponse<>(true, "작가 정보를 성공적으로 조회했습니다.", authorOpt.get())
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "해당 작가를 찾을 수 없습니다.", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "작가 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
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
