package mp.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.*;
import lombok.Data;
import mp.AuthorsApplication;
import mp.domain.AuditCompleted;
import mp.domain.AuthorApplied;
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

    private String status;

    private String name;

    private String bio;

    private String portfolioUrl;

    @PostPersist
    public void onPostPersist() {
        AuthorApplied authorApplied = new AuthorApplied(this);
        authorApplied.publishAfterCommit();
    }

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
