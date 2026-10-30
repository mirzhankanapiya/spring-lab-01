package kz.iitu.springlab.web.dto;

import java.util.List;

public record ScrollResponse(
        List<BookResponse> items,
        String nextCursor,
        boolean hasNext) { }