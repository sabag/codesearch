package com.daniel.search.service;

import static com.daniel.search.service.FileGrepService.isChildOf;

import com.daniel.search.SearchApplication;
import com.daniel.search.model.SearchConfig;
import com.daniel.search.model.ViewRequest;
import com.daniel.search.model.ViewResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
public class ApplicationTests {


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileGrepService service;


    @Before
    public void before() throws IOException, NoSuchFieldException, IllegalAccessException {
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
    public void test1() throws IOException {
        Resource fileResource = new ClassPathResource("searchtest1.txt");
        String testFile = fileResource.getFile().getAbsolutePath();
        ViewResponse response = service.readFileContent(new ViewRequest("find me", testFile));
        Assert.assertNotNull(response);
        String content = response.getContent();
        System.out.println(content);
        Assert.assertNotNull(content);
        Assert.assertTrue(content.startsWith("line 11 first line"));
        Assert.assertTrue(content.endsWith("line 21 last line to see in results\n"));
        Assert.assertEquals("wrong number of lines in result", 11, content.split("\n").length);
    }

    @Test
    public void test2() throws IOException {
        Resource fileResource = new ClassPathResource("searchtest2.txt");
        String testFile = fileResource.getFile().getAbsolutePath();
        ViewResponse response = service.readFileContent(new ViewRequest("find me", testFile));
        Assert.assertNotNull(response);
        String content = response.getContent();
        System.out.println(content);

        Assert.assertFalse(content.isEmpty());
        Assert.assertTrue(content.startsWith("line 2 "));
        Assert.assertTrue(content.endsWith("line 29 C<---\n"));
        Assert.assertEquals("wrong number of lines in result", 28, content.split("\n").length);
    }

    @Test
    public void test3() throws IOException {
        Resource fileResource = new ClassPathResource("searchtest3.txt");
        String testFile = fileResource.getFile().getAbsolutePath();
        ViewResponse response = service.readFileContent(new ViewRequest("find me", testFile));
        String content = response.getContent();
        System.out.println(content);

        Assert.assertFalse(content.isEmpty());
        Assert.assertTrue(content.startsWith("line 1"));
        Assert.assertTrue(content.endsWith("line 30\n"));

        Assert.assertTrue(content.contains("line 8\n"));
        Assert.assertFalse(content.contains("line 9\n"));
        Assert.assertFalse(content.contains("line 10\n"));
        Assert.assertTrue(content.contains("line 11\n"));

        Assert.assertTrue(content.contains("line 21\n"));
        Assert.assertFalse(content.contains("line 22\n"));
        Assert.assertTrue(content.contains("line 23\n"));

        Assert.assertEquals("wrong number of lines in result", 27, content.split("\n").length);
    }


    @Test
    public void test5() {

        Path p1 = Path.of("/etc/hosts");
        Path p2 = Path.of("/Users/dsabag");
        Path p3 = Path.of("/Users/dsabag/work/untitled.txt");

        Assert.assertFalse(isChildOf(p1, p2));
        Assert.assertFalse(isChildOf(p1, p3));
        Assert.assertTrue(isChildOf(p2, p3));

        Assert.assertFalse(isChildOf(p3, p2));
        Assert.assertFalse(isChildOf(p3, p1));
        Assert.assertFalse(isChildOf(p2, p1));

        Assert.assertFalse(isChildOf(null, p3));
        Assert.assertFalse(isChildOf(p3, null));

    }


    @Test
    public void test6() {
        try {
            ViewResponse res = service.readFileContent(new ViewRequest("127.0.0.1", "/etc/hosts"));
            Assert.assertNotNull(res);
            Assert.assertNotNull("response should be error", res.getError());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testCommonPath1() {
        SearchConfig config = new SearchConfig();
        config.setSearchDirectories(Arrays.asList(
            "/Users/user/work",
            "  ",
            "",
            "/"
        ));
        String basePath = config.calculateCommonBasePath();
        Assert.assertEquals("/", basePath);
    }


    @Test
    public void testCommonPath2() {

        SearchConfig config = new SearchConfig();
        config.setSearchDirectories(Arrays.asList(
            "$HOME/mysource",
            "/Users/dsabag/work/backup/old",
            "/Users/dsabag/dev/project",
            "/Users/dsabag/notes",
            "/Users/dsabag/documents"
        ));
        String basePath = config.calculateCommonBasePath();
        Assert.assertEquals("/Users/dsabag", basePath);
    }

    @Test
    public void testCommonPath3() {
        SearchConfig config = new SearchConfig();
        config.setSearchDirectories(Arrays.asList(
            "/Users/user/dir1",
            "/Users/user/dir2",
            "/Users/user/dir3",
            "/Users/user/dir4"
        ));
        String basePath = config.calculateCommonBasePath();
        Assert.assertEquals("/Users/user", basePath);
    }

    @Test
    public void testCommonPath4() {
        SearchConfig config = new SearchConfig();
        config.setSearchDirectories(Arrays.asList(
            "/Users/user1/dir1",
            "/Users/user2/work",
            "/Users/",
            "/etc/conf"
        ));
        String basePath = config.calculateCommonBasePath();
        Assert.assertEquals("/", basePath);
    }

}
