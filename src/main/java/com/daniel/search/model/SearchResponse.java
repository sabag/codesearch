package com.daniel.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResponse {

    private FileResultsTree results;

}
