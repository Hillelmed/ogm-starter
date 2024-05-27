# Spring Starter for Object-Git Mapping OGM (Object Git Mapping)

![Alt text](/attachments/OgmExamplePic.png)

**Java Spring starter library that linked your code to Git like ORM**
**Support Java 17 and 21**

## Overview
This project aims to provide a Spring Starter package that facilitates seamless integration between Spring Boot applications and Git repositories, particularly focusing on object-git mapping. By leveraging this starter, developers can efficiently manage and interact with Git repositories within their Spring applications, abstracting away the complexities of Git operations and object mapping.

## Key Features
- **Spring Integration**: The starter seamlessly integrates with Spring Boot applications, enabling developers to leverage familiar Spring concepts and patterns.
- **Object-Git Mapping**: Facilitates mapping between Java objects and Git repository data, simplifying the storage and retrieval of application data from Git.
- **Git Operations Abstraction**: Abstracts away the intricacies of Git operations such as commits, branches, and merges, providing a more intuitive interface for developers.
- **Customizable Configuration**: Offers customizable configuration options to adapt to various Git repository structures and requirements.
- **Efficient Version Control**: Enables efficient version control of application data by leveraging Git's robust versioning capabilities.
- **Developer Productivity**: Enhances developer productivity by automating common Git-related tasks and reducing boilerplate code.

## Use Cases
- **Document Management Systems**: Ideal for applications requiring versioned document storage and management, where Git serves as the underlying repository.
- **Configuration Management**: Useful for managing application configurations stored in Git repositories, providing versioned configuration management capabilities.
- **Collaborative Development**: Facilitates collaborative development workflows by enabling teams to work on shared codebases with Git-backed storage.

## Getting Started
To get started with the Spring Starter for Object-Git Mapping, simply include the starter dependency in your Spring Boot project's `pom.xml` or `build.gradle` file. Then, configure the necessary properties to connect to your Git repository, and you're ready to start mapping objects to Git data seamlessly.

Add to your dependencies: (Maven example: Latest release)

```
<dependency>
  <groupId>io.github.hillelmed</groupId>
  <artifactId>ogm-starter</artifactId>
  <version>X.Y.Z</version>
  <classifier>sources|tests|javadoc|all</classifier> (Optional)
</dependency>
```

The starter will scan your base package from `@SpringBootApplication` annotation And search interface that
extend to `GitCrudRepository<T>` Interface with `@GitModel` `<T> generic type`
Example of `GitModel` class that class will be the `<T>`:

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

Example of GitRepository class:

```
public interface MyRepoForSpesificFile extends GitCrudRepository<GitPomFile> {
}
```

We can get all the files from repo by `@GitFiles` annotations and `GitRepositoryMap`
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
    private GitRepositoryMap files;

}
```

Example of uses (Get file from git, change the version of the pom.xml file and push):

```
MyRepoForSpesificFile myRepoForSpesificFile = appContext.getBean(MyRepoForSpesificFile.class);
RepoPomFile repoPomFile = myRepoForSpesificFile.getByRepositoryAndRevision("PartyApp.git", "dev");
((ObjectNode) repoPomFile.getPomXml()).set("version", TextNode.valueOf("2.0.0-hillel-test"));
myRepoForSpesificFile.update(repoPomFile);
```

**Note**: Instead of appContext.getBean() we can use constructor injection or @Autowired

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

- Spring Starter
- Lombok \- used to create immutable value types both
- Proxy bean
- JGit (eclipse)
- Jackson-databind (JSON, YAML, XML)

## Testing

Missing

### Integration tests settings

Missing

## Contribution
Contributions to the project are welcome! Whether it's bug fixes, new features, or documentation enhancements, feel free to contribute to make this project even better. Please refer to the project's GitHub repository for contribution guidelines and instructions.

# Additional Resources

* [Proxy-bean](https://www.baeldung.com/java-dynamic-proxies)
* [Reflection](https://www.baeldung.com/reflections-library)
* [Spring](https://spring.io/projects/spring-framework)
