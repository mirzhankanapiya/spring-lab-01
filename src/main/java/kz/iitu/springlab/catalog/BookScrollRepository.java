package kz.iitu.springlab.catalog;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.repository.Repository;

public interface BookScrollRepository extends Repository<Book, Long> {

    // Keyset needs a stable unique order: year is not unique, so id is the tie-breaker
    Window<Book> findByYearGreaterThanEqualOrderByYearDescIdAsc(int fromYear,
                                                                ScrollPosition position,
                                                                Limit limit);
}