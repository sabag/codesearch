package com.daniel.search.service;

import com.daniel.search.SearchApplication;
import com.daniel.search.model.SearchConfig;
import com.daniel.search.model.SearchResultEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;


@RunWith(SpringRunner.class)
@WebFluxTest(controllers = {FileGrepController.class})
@ContextConfiguration(classes = SearchApplication.class)
@ComponentScan(basePackages = {"com.daniel"})
public class ControllerTest {



    @Autowired
    private FileGrepController controller;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileGrepService service;

    private WebTestClient webClient;


    @Before
    public void before() {
        webClient = WebTestClient.bindToController(controller).build();

    }

    private void overrideSearchConfig() throws IOException, NoSuchFieldException, IllegalAccessException {
        Resource fileResource = new ClassPathResource("default-config.json");
        File configFile = fileResource.getFile();
        SearchConfig config = objectMapper.readValue(configFile, SearchConfig.class);
        // add this project as base directory
        config.setSearchDirectories(Collections.singletonList(new File(".").getCanonicalFile().getAbsolutePath()));

        // set the private field with different config
        Field configField = FileGrepService.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(service, config);
    }



    @Test
    public void testSearch() throws Exception {

        overrideSearchConfig();


        String TERM_TEST = "public void testSearch()";

        List<SearchResultEvent> events = webClient.get().uri("/search/" + TERM_TEST)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectBodyList(SearchResultEvent.class)
            .returnResult()
            .getResponseBody();

        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());

        String baseDir = new File(".").getCanonicalPath();
        Assert.assertEquals(baseDir + "/src/test/java/com/daniel/search/service/ControllerTest.java", events.get(0).getFile());

        Assert.assertTrue(events.get(1).getLast());
    }


}
