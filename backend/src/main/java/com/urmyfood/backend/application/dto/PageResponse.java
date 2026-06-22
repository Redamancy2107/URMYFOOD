package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> content;

    private int page;

    private int size;

    @JsonProperty("total_elements")
    private long totalElements;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("has_next")
    private boolean hasNext;

    /** ISO-8601 timestamp cursor for next page (used by comments cursor pagination). Null if not applicable. */
    @JsonProperty("next_cursor")
    private String nextCursor;

    /** ISO-8601 anchor timestamp to stabilize offset pagination for posts/search. */
    @JsonProperty("anchor")
    private String anchor;

    /** Classic offset-based factory (backwards compatible) */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page + 1 < totalPages)
                .build();
    }

    /** Anchored offset-based factory for posts/search */
    public static <T> PageResponse<T> ofAnchored(List<T> content, int page, int size, long totalElements, String anchor) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page + 1 < totalPages)
                .anchor(anchor)
                .build();
    }

    /** Cursor-based factory for comments */
    public static <T> PageResponse<T> ofCursor(List<T> content, int size, boolean hasNext, String nextCursor) {
        return PageResponse.<T>builder()
                .content(content)
                .page(-1) // not used in cursor mode
                .size(size)
                .totalElements(-1) // not computed in cursor mode
                .totalPages(-1)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }
}
