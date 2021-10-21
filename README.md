## Project My Search


### Features
- Search source code
- Search notes
- Search work shell scripts


### Configuration
the configuration is a json file that should be located as $HOME/.mysearch/config.json.
if such file exists, it overrides the following default
```json
{
  "fileExtensionsPattern":
    ".*\\.(java|c|h|cpp|properties|xml|yaml|yml|json|html|css|js|go|txt|sql|sh|py|proto|md|jsp|jspf|csv)",

  "searchDirectories": [
    "$HOME/mysource"
  ],

  "skipDirectoriesPatterns": [
    ".*/.git",
    ".*/\\.idea",
    ".*/target",
    ".*/go/bin",
    ".*/go/pkg"
  ],

  "skipFilePatterns": [
    ".*\\.pyc",
    ".*\\.class",
    ".*/\\.exe",
    ".*/\\.bin",
    ".*/\\.obj"
  ],

  "displayLinesBefore": 5,
  "displayLinesAfter": 6

}
```
Notes:
- skipDirectoriesPatterns **MUST** end without forward slash


### Install
```bash
git clone git clone https://github.com/sabag/codesearch.git
cd search
mvn clean install
./run.sh [PORT]
```
Java 11 must be used to build and to run.
default port is 8080, you can specify the port as argument to run.sh.
Browse to http://localhost:8080



### TODO
* ~~ver 2.1 - show N lines before and after each occurrence of the search term~~
* ~~ver 2.1 - create test with test file~~
* ~~ver 3.0 - multiple base directories~~
* ~~ver 3.0 - configuration json file~~
* ~~ver 3.1 - must check that view file is under one of the base directories~~
* ~~ver 3.2 - highlight search term in content~~
* ~~ver 3.3 - live reload for configuration file~~
* ~~ver 4.0 - optimize search speed - send results as SSE (Server-Sent Events)~~
* ~~added bootstrap styles~~ 
* ~~hide the common path from results~~
* ver 5.0 - optimize search speed - parallel searching (needed?)
* ver 6.0 - editor for configuration (maybe file watcher is enough?)


### References

* [Access Data](https://www.thymeleaf.org/doc/articles/springmvcaccessdata.html)
* [Templates - Tutorial](https://www.thymeleaf.org/doc/tutorials/2.1/usingthymeleaf.html#inlining)
* [Templates - Forms](https://attacomsian.com/blog/spring-boot-thymeleaf-form-handling)
* [Templates - Iterate Collections](https://attacomsian.com/blog/thymeleaf-iterate-map-list-set-array)
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

* [jsTree](https://www.jstree.com/)
* [Refresh jsTree](https://exceptionshub.com/how-can-i-refresh-the-contents-of-a-jstree.html)
* [Displaying And Highlighting Source code in HTML Page](http://qnimate.com/displaying-and-highlighting-source-code-in-html-page/)

* [WebFlux with Server-Sent Events](https://mkyong.com/spring-boot/spring-boot-webflux-server-sent-events-example/)
* [Reactor 3 Reference Guide](https://projectreactor.io/docs/core/release/reference/index.html)
* [Streaming Data with Spring Boot RESTful Web Service](https://technicalsand.com/streaming-data-spring-boot-restful-web-service/)
* [spring-webflux-websocket github example](https://github.com/atilio-araujo/spring-webflux-websocket)

* [Bootstrap Reference](https://getbootstrap.com/docs/4.0/components/buttons)


### SpringBoot Banner
generate a Ascii Art banner for log startup using this [Online Generator](https://springhow.com/spring-boot-banner-generator/) 
Fonts demo can be found [here](http://www.figlet.org/examples.html).

