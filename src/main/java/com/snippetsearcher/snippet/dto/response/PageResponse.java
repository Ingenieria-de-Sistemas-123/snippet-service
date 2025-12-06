package com.snippetsearcher.snippet.dto.response;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
    List<T> items, int page, int pageSize, long totalElements, int totalPages) {

  public static <S, T> PageResponse<T> from(Page<S> source, Function<S, T> mapper) {
    Page<T> mapped = source.map(mapper);
    return new PageResponse<>(
        mapped.getContent(),
        mapped.getNumber(),
        mapped.getSize(),
        mapped.getTotalElements(),
        mapped.getTotalPages());
  }
}
