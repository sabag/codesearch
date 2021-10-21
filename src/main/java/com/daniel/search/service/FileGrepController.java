package com.daniel.search.service;


import com.daniel.search.model.SearchRequest;
import com.daniel.search.model.SearchResultEvent;
import com.daniel.search.model.ViewRequest;
import com.daniel.search.model.ViewResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
public class FileGrepController {

    private final FileGrepService service;

    private final TaskExecutor taskExecutor;


    @Autowired
    public FileGrepController(FileGrepService service, TaskExecutor taskExecutor) {
        this.service = service;
        this.taskExecutor = taskExecutor;
    }



    @PostMapping(value = "/view")
    public ResponseEntity<ViewResponse> view(@RequestBody ViewRequest payload){
        if(payload == null || !payload.isValid()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            ViewResponse response = service.readFileContent(payload);
            return ResponseEntity.ok(response);

        }catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ViewResponse(e));
        }
    }


    @GetMapping(path = "/search/{searchTerm}", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SearchResultEvent> streamSearchEvents(@PathVariable String searchTerm) {

        if(StringUtils.isEmpty(searchTerm)){
            return Flux.error(new IllegalArgumentException("Invalid search request, check the payload of /search"));
        }

        log.info("received request to search '{}' as event stream", searchTerm);

        return Flux.push(sink -> {
            taskExecutor.execute(() -> {
                service.sourceSearchAsEvents(sink, new SearchRequest(searchTerm));
            });
        });

    }

//    private void create(FluxSink<SearchResultEvent> sink) {
//        for(int i=1;i<6;i++){
//            sink.next(new SearchResultEvent("File"+i));
//            try{Thread.sleep(1000);}catch (Exception ignore){}
//        }
//        sink.next(SearchResultEvent.LAST);
//        sink.complete();
//    }

}