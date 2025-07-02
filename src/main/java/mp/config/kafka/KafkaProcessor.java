package mp.config.kafka;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface KafkaProcessor {
    String AUTHOR_REVIEW_OUT = "author-review-out";

    @Output(AUTHOR_REVIEW_OUT)
    MessageChannel authorReviewOut();
}
