package mp.domain;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "authors", path = "authors")
public interface AuthorRepository
    extends PagingAndSortingRepository<Author, UUID> {
    
    Optional<Author> findByUserId(UUID userId);
    List<Author> findByStatus(AuthorStatus status);
}
