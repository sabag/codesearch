package com.daniel.search.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ViewResponse {

    private String error;

    private String file;
    private String content;

    public ViewResponse(Exception ex){
        this.error = ex.getMessage();
    }

    public ViewResponse(String file, String content){
        this.file = file;
        this.content = content;
    }

}
