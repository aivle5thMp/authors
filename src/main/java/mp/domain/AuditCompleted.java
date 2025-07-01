package mp.domain;
//
import java.time.LocalDate;
import java.util.*;
import lombok.*;
import mp.domain.*;
import mp.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class AuditCompleted extends AbstractEvent {

    private UUID id;
    private UUID userId;
    private String status;
    private String portfolioUrl;

    public AuditCompleted(Author aggregate) {
        super(aggregate);
    }

    public AuditCompleted() {
        super();
    }
}
//>>> DDD / Domain Event
