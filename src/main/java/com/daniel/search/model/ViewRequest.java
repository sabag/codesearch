package com.daniel.search.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewRequest {

    private String searchTerm;
    private String file;

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.hasText(file) && StringUtils.hasText(searchTerm);
    }
}
