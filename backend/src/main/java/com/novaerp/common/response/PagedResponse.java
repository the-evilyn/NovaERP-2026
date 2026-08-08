package com.novaerp.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standardized pagination envelope for list endpoints.
 *
 * @param <T> Element item type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standardized Paginated Response Envelope")
public class PagedResponse<T> {

    @Schema(description = "Page elements collection")
    private List<T> content;

    @Schema(description = "Current page index (0-based)", example = "0")
    private int pageNumber;

    @Schema(description = "Requested page size", example = "20")
    private int pageSize;

    @Schema(description = "Total matching elements across all pages", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Indicates whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Indicates whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Indicates whether the page has items", example = "false")
    private boolean empty;

    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
