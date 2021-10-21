package com.daniel.search.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchResultEvent {

    public static SearchResultEvent LAST = new SearchResultEvent(null, null, true);

    private String file;
    private String commonPath;
    private Boolean last;

    private SearchResultEvent(String file, String commonPath, Boolean last) {
        this.file = file;
        this.commonPath = commonPath;
        this.last = last;
    }

    public SearchResultEvent(String file, String common) {
        this(file, common, false);
    }

}
