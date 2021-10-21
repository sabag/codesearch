package com.daniel.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
@AllArgsConstructor
public class SearchRequest {
    private String term;

    public boolean isValid() {
        return StringUtils.hasText(term);
    }
}
