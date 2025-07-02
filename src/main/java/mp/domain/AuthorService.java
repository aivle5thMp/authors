package mp.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    /**
     * 작가 신청 처리
     */
    public Author applyAuthor(Author author) {
        // Set initial status to pending if not provided
        if (author.getStatus() == null) {
            author.setStatus(AuthorStatus.PENDING);
        }
        
        return authorRepository.save(author);
    }

    /**
     * 전체 작가 신청 목록 조회 (관리자용)
     */
    public List<Author> getAllAuthors() {
        return (List<Author>) authorRepository.findAll();
    }

    /**
     * PENDING 상태인 작가 신청 목록 조회 (관리자용)
     */
    public List<Author> getPendingAuthors() {
        return authorRepository.findByStatus(AuthorStatus.PENDING);
    }

    /**
     * 작가 심사 처리 (관리자용)
     */
    public Optional<Author> reviewAuthor(UUID authorId, Boolean approved) {
        Optional<Author> authorOpt = authorRepository.findById(authorId);
        
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();
            author.setStatus(approved ? AuthorStatus.APPROVED : AuthorStatus.REJECTED);
            Author updatedAuthor = authorRepository.save(author);
            return Optional.of(updatedAuthor);
        }
        
        return Optional.empty();
    }

    /**
     * 특정 작가 조회
     */
    public Optional<Author> getAuthorById(UUID authorId) {
        return authorRepository.findById(authorId);
    }

    /**
     * 사용자 ID로 작가 신청 조회
     */
    public Optional<Author> getAuthorByUserId(UUID userId) {
        return authorRepository.findByUserId(userId);
    }
} 