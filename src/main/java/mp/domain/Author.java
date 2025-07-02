package mp.domain;

import java.util.UUID;
import javax.persistence.*;
import lombok.Data;
import mp.AuthorsApplication;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "Author_table")
@Data
//<<< DDD / Aggregate Root
public class Author {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(columnDefinition = "BINARY(16)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthorStatus status = AuthorStatus.PENDING;

    private String name;

    private String bio;

    private String portfolioUrl;

    @PostUpdate
    public void onPostUpdate() {
        AuditCompleted auditCompleted = new AuditCompleted(this);
        auditCompleted.publishAfterCommit();
    }

    public static AuthorRepository repository() {
        AuthorRepository authorRepository = AuthorsApplication.applicationContext.getBean(
            AuthorRepository.class
        );
        return authorRepository;
    }
}
//>>> DDD / Aggregate Root
