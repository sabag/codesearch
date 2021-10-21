package com.daniel.search.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class SearchConfig {

    private static final int DISPLAY_LINES_BEFORE_OCCURRENCE = 5;
    private static final int DISPLAY_LINES_AFTER_OCCURRENCE = 6;

    private String fileExtensionsPattern;

    /*
    this config supports the $HOME variable
     */
    private List<String> searchDirectories;

    private List<String> skipDirectoriesPatterns;

    private List<String> skipFilePatterns;

    private int displayLinesBefore;
    private int displayLinesAfter;


    public List<String> getSearchDirectories() {
        return searchDirectories.stream().filter(StringUtils::hasText).map(dir ->
            dir.replaceAll("\\$HOME", System.getProperty("user.home"))
        )
        .collect(Collectors.toList());
    }

    public String calculateCommonBasePath(){
        List<String> directories = getSearchDirectories();

        Optional<Integer> minPathCount = directories.stream().map(d -> Path.of(d)).map(Path::getNameCount).min(Integer::compareTo);
        if(minPathCount.isPresent()) {

            final AtomicInteger common = new AtomicInteger(minPathCount.get()-1);

            while(common.get() > 0) {
                Set<Path> pathCommon = directories.stream()
                    .map(d -> Path.of(d))
                    .map(p -> p.getName(common.get()))
                    .collect(Collectors.toSet());

                if (pathCommon.size() > 1) {
                    break;
                }
                common.decrementAndGet();
            }

            String path = "";
            if(common.get() > 0) {
                path = Path.of(directories.get(0)).subpath(0, common.get()).toString();
            }
            return "/" + path;
        }

        throw new IllegalStateException("cannot find common path path from config directories. check the configuration.");
    }
}
