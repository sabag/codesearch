package com.daniel.search.service;

import com.daniel.search.model.ViewRequest;
import com.daniel.search.model.ViewResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class FileViewTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    public void testFileView() throws Exception {

        String baseDir = new File(".").getCanonicalPath();
        String file = baseDir + "/src/test/java/com/daniel/search/service/FileViewTest.java";

        ViewRequest req = new ViewRequest("testFileView", file);

        MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.post("/view")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        String jsonContent = result.getResponse().getContentAsString();
        Assert.assertNotNull(jsonContent);
        ViewResponse viewResponse = objectMapper.readValue(jsonContent, ViewResponse.class);
        Assert.assertNull(viewResponse.getError());
        Assert.assertEquals(file, viewResponse.getFile());
        Assert.assertNotNull(viewResponse.getContent());
        String[] lines = viewResponse.getContent().split(FileGrepService.LINE_SEPARATOR);
        Assert.assertTrue(lines.length > 10);
        System.out.println("\n\nresult = \n\n" + viewResponse.getContent());

    }

}
