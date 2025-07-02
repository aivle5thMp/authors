package mp.domain;

import java.util.*;
import lombok.*;
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
        if (aggregate.getStatus() != null) {
            this.status = aggregate.getStatus().getValue();
        }
    }

    public AuditCompleted() {
        super();
    }
}
//>>> DDD / Domain Event
