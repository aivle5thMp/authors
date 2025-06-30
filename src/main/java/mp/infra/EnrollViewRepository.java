package mp.infra;

import java.util.List;
import java.util.UUID;
import mp.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
    collectionResourceRel = "enrollViews",
    path = "enrollViews"
)
public interface EnrollViewRepository
    extends PagingAndSortingRepository<EnrollView, UUID> {
    List<EnrollView> findByUserId(UUID userId);
}
