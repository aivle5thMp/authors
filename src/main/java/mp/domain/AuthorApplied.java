package mp.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import mp.domain.*;
import mp.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class AuthorApplied extends AbstractEvent {

    private UUID id;
    private UUID userId;
    private String status;
    private String portfolioUrl;
    private String name;
    private String bio;

    public AuthorApplied(Author aggregate) {
        super(aggregate);
    }

    public AuthorApplied() {
        super();
    }
}
//>>> DDD / Domain Event
