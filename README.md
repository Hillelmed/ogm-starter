# OGM (Object Git Mapping)

Java Spring starter that work with git like ORM but for git mapping
Support Java 17 and 21

## Setup

The starter will scan your base package from `@SpringBootApplication` And search interface that have
annotations `@Repository`
And extend to `GitRepository<T>` Interface and send T `@GitModel`
Example of `GitModel` class:

```
@GitModel
@Data
public class GitPomFile {

    @GitRepository
    private String repo;

    @GitRevision
    private String branch;

    @GitFile(path = "pom.xml", type = FileType.XML)
    private JsonNode pomXml;

}
```

Example of GitRepository class

```
@Repository
public interface MyRepoForSpesificFile extends GitRepository<GitPomFile> {
}
```

We can get all the files from repo by `@GitFiles` annotations and `Map<String,String>`
Example of Call load change the version and update the Git:

```
@GitModel
@Data
public class MyRepo {

    @GitRepository
    private String repo;

    @GitRevision
    private String branch;

    @GitFiles
    private Map<String,String> files;

}
```

Example of uses (Load file from git and change the version and push):

```
MyRepoForSpesificFile myRepoForSpesificFile = appContext.getBean(MyRepoForSpesificFile.class);
RepoPomFile repoPomFile = myRepoForSpesificFile.getByRepositoryAndRevision("PartyApp.git", "dev");
((ObjectNode) repoPomFile.getPomXml()).set("version", TextNode.valueOf("2.0.0-hillel-test"));
myRepoForSpesificFile.update(repoPomFile);
```

**Note**: Instead of appContext.getBean() we can use constructor injection or @Autowired

## Latest release

Can be found in maven like so:

```
<dependency>
  <groupId>io.github.hillelmed</groupId>
  <artifactId>ogm-starter</artifactId>
  <version>X.Y.Z</version>
  <classifier>sources|tests|javadoc|all</classifier> (Optional)
</dependency>
```

## Documentation

* javadocs Missing

## Property based setup

Ogm repository instances do NOT need to supply the endPoint or credentials as a part of instantiating the repository
object.
Instead, one can supply them through system properties, environment variables, or a combination
of the two. System properties will be searched first and if not found, will attempt to
query the environment.

Setting the `endpoint` can be done with any of the following (searched in order):

- `ogm.endpoint`
- `OGM_ENDPOINT`

When none is found, the endpoint is set to `http://127.0.0.1:7990`.

Setting the `user` can be done with any of the following (searched in order):

- `ogm.user`
- `OGM_USER`

Setting the `Password` can be done with any of the following (searched in order):

- `ogm.password`
- `OGM_PASSWORD`

When none is found, no authentication is used (anonymous).

## Examples

Missing

## Components

- Spring Core
- Reflection
- Lombok \- used to create immutable value types both
- Proxy bean

## Testing

Missing

### Integration tests settings

Missing

# Additional Resources

* [Proxy-bean](https://www.baeldung.com/java-dynamic-proxies)
* [Reflection](https://www.baeldung.com/reflections-library)
* [Spring](https://spring.io/projects/spring-framework)
