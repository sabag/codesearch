package com.daniel.search.service;


import com.daniel.search.model.SearchConfig;
import com.daniel.search.model.SearchRequest;
import com.daniel.search.model.SearchResultEvent;
import com.daniel.search.model.ViewRequest;
import com.daniel.search.model.ViewResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.FluxSink;

@Service
@Slf4j
public class FileGrepService {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");


    static boolean isChildOf(Path parent, Path child) {
        if (parent == null)
            return false;
        Path pt = child;
        while (pt != null && !parent.equals(pt)) {
            pt = pt.getParent();
        }
        return pt != null;
    }


    @Value("${com.daniel.user-config-file}")
    private String userConfigFilename;

    @Autowired
    private ObjectMapper objectMapper;




    private File userConfigFile;
    private SearchConfig config;
    private String commonPath;

    @PostConstruct
    public void init() {
        userConfigFile = new File(System.getProperty("user.home"), userConfigFilename);
        loadConfig();
        new Thread(this::startConfigFileWatch).start();
    }


    ViewResponse readFileContent(ViewRequest request) throws IOException {
        String filename = request.getFile();

        // check that requested file is under one of the base directories
        boolean allowAccess = false;
        Path child = Path.of(filename);
        for (String base : config.getSearchDirectories()) {
            Path parent = Path.of(base);
            if (isChildOf(parent, child)) {
                allowAccess = true;
                break;
            }
        }
        if(!allowAccess){
            return new ViewResponse(new IllegalAccessException("view file is prohibited"));
        }

        // this call throws "java.nio.charset.MalformedInputException: Input length = 1"
        //return Files.lines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8).collect(Collectors.joining(System.getProperty("line.separator")));

        List<String> lines = new ArrayList<>();
        List<Integer> occurrences = new ArrayList<>();
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
                if (searchTermInLine(line, request.getSearchTerm())) {
                    occurrences.add(lineCount);
                }
                lineCount++;
            }
        }

        //
        // cut portions of the content around (before and after) the occurrences, make sure not to overlap
        //
        int start;
        int stop = 0;
        final StringBuilder content = new StringBuilder();
        for (int i = 0; i < occurrences.size(); i++) {

            int lineIndex = occurrences.get(i);

            start = Math.max(i == 0 ? 0 : stop, lineIndex - config.getDisplayLinesBefore());
            stop = Math.min(lines.size(), lineIndex + config.getDisplayLinesAfter());

            lines.stream()
                .skip(start)
                .limit(stop - start)
                .forEach(ln -> content.append(ln).append(LINE_SEPARATOR));
        }

        return new ViewResponse(request.getFile(), content.toString());
    }


    private boolean searchFile(String filename, String term, Charset charset) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (searchTermInLine(line, term)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean searchTermInLine(String line, String term) {
        return line.contains(term);
    }


    private void loadConfig()  {
        // allow override config from $HOME/.mysearch/config.json
        try {
            config = objectMapper.readValue(userConfigFile, SearchConfig.class);
            log.debug("loaded search config file");

        } catch (Exception e) {
            log.error("failed to load config file '{}', error:{}, message:{}",
                userConfigFile.getAbsolutePath(), e.getClass(), e.getMessage());

            // default config from classpath
            try {
                Resource fileResource = new ClassPathResource("default-config.json");
                File configFile = fileResource.getFile();
                config = objectMapper.readValue(configFile, SearchConfig.class);
                log.debug("loaded default config file");

            } catch (IOException e1) {
                log.error("failed to load default config json, something is clearly wrong here, application will not run properly from now", e1);
            }
        }
    }


    private void startConfigFileWatch() {
        if(userConfigFile.isFile() && userConfigFile.exists()) {
            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                Path watchPath = Path.of(userConfigFile.getParent());
                watchPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

                log.debug("starting config file watcher...");
                for ( ; ; ) {

                    // wait for key to be signaled
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException x) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        Path changedFilePath = watchPath.resolve(filename);
                        if(changedFilePath.equals(userConfigFile.toPath())) {
                            loadConfig();
                        }

                        log.trace("Operation: " + kind + " On file: "+ changedFilePath + " is done");

                    }

                    // Reset the key -- this step is critical if you want to
                    // receive further watch events.  If the key is no longer valid,
                    // the directory is inaccessible so exit the loop.
                    boolean valid = key.reset();
                    if (!valid) {
                        log.error("config watcher is invalid, changes will not be detected anymore");
                        break;
                    }
                }

            } catch (IOException e) {
                log.error("failed to start the config file watcher.", e);
            }
        }
    }


    //
    // Server-Sent Events implementation
    //

    void sourceSearchAsEvents(FluxSink<SearchResultEvent> sink, SearchRequest request) {

        List<String> searchDirectories = config.getSearchDirectories();
        if (searchDirectories == null || searchDirectories.isEmpty()) {
            sink.complete();
            return;
        }

        //
        // calculate the common path of all the search directories
        // to be able to remove it from results display (for convenience)
        //
        this.commonPath = config.calculateCommonBasePath();

        for (String base : searchDirectories) {

            File directory = new File(base);
            if (directory.exists() && directory.isDirectory()) {

                log.debug("start searching base directory: {}", base);
                long time = System.currentTimeMillis();
                searchBaseDirectoryWithSink(sink, base, request.getTerm());
                long took = System.currentTimeMillis() - time;
                log.debug("finished searching base directory: {}, time: {} ms", base, took);

            } else {
                log.warn("ignore non directory entry: {}", base);
            }
        }

        sink.next(SearchResultEvent.LAST);
        sink.complete();

        log.info("event stream of search finished");

    }


    private void searchBaseDirectoryWithSink(FluxSink<SearchResultEvent> sink, String baseDirectory, String searchTerm) {

        try {

            Files.walkFileTree(Paths.get(baseDirectory), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String directoryName = dir.toFile().getAbsolutePath();
                    // skip subtree
                    for (String pat : config.getSkipDirectoriesPatterns()) {
                        if (directoryName.matches(pat)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }

                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return super.postVisitDirectory(dir, exc);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.toFile().getAbsolutePath();

                    for (String pat : config.getSkipFilePatterns()) {
                        if (fileName.matches(pat)) {
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    if (fileName.matches(config.getFileExtensionsPattern())) {
                        try {
                            if (searchFile(fileName, searchTerm, StandardCharsets.UTF_8)) {
                                log.debug("found matching file '{}'", fileName);
                                sink.next(new SearchResultEvent(fileName, commonPath));
                            }
                        } catch (Exception e) {
                            log.error("error searching inside file {}. reason {}: {}", fileName, e.getClass().getName(), e.getMessage());
                        }
                    }
                    return super.visitFile(file, attrs);
                }
            });

        } catch (IOException e) {
            sink.error(e);
        }

    }

}
