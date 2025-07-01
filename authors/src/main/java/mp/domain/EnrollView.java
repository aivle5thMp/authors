package mp.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

//<<< EDA / CQRS
@Entity
@Table(name = "EnrollView_table")
@Data
public class EnrollView {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(columnDefinition = "BINARY(16)")
    private UUID userId;
    private String enrollStatus;
    private String portfolioUrl;
    private String status;
}
