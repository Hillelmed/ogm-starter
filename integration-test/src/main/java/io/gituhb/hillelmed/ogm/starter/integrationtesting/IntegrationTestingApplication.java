package io.gituhb.hillelmed.ogm.starter.integrationtesting;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.hillelmed.ogm.starter.exception.OgmRuntimeException;
import io.gituhb.hillelmed.ogm.starter.integrationtesting.model.GenericRepo;
import io.gituhb.hillelmed.ogm.starter.integrationtesting.model.RepoApplicationFile;
import io.gituhb.hillelmed.ogm.starter.integrationtesting.model.RepoPomFile;
import io.gituhb.hillelmed.ogm.starter.integrationtesting.repo.GenericRepoCrudRepository;
import io.gituhb.hillelmed.ogm.starter.integrationtesting.repo.MyRepoAppForSpesificFile;
import io.gituhb.hillelmed.ogm.starter.integrationtesting.repo.MyRepoForSpesificFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class IntegrationTestingApplication implements CommandLineRunner {

    @Autowired
    private ApplicationContext appContext;


    public static void main(String[] args) {
        SpringApplication.run(IntegrationTestingApplication.class, args);
    }

    @Override
    public void run(String... args) {

//        String[] beans = appContext.getBeanDefinitionNames();
//        Arrays.sort(beans);
//        for (String bean : beans) {
//            System.out.println(bean);
//        }

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        long time = System.currentTimeMillis();
        MyRepoForSpesificFile myRepoForSpesificFile = appContext.getBean(MyRepoForSpesificFile.class);
        RepoPomFile repoPomFile = myRepoForSpesificFile.getByRepositoryAndRevision("PartyApp.git", "dev");
        ((ObjectNode) repoPomFile.getPomXml()).set("version", TextNode.valueOf("2.0.0-hillel-popo"));

        myRepoForSpesificFile.update(repoPomFile);

        System.out.println("Time taken update pom.xml: " + (System.currentTimeMillis() - time));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        time = System.currentTimeMillis();
        RepoPomFile repoPomFileNew = new RepoPomFile();
        repoPomFileNew.setRepo("PartyApp.git");
        repoPomFileNew.setBranch("test");
        ObjectNode currentNode = new ObjectNode(JsonNodeFactory.instance);

        currentNode
                .put("Puppy", true)
                .put("Apple", 2)
                .put("Jet", "Li");

        repoPomFileNew.setPomXml(currentNode);
        myRepoForSpesificFile.sync(repoPomFileNew);
        System.out.println("Time taken create pom.xml file with new objectNode: " + (System.currentTimeMillis() - time));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        time = System.currentTimeMillis();
        GenericRepoCrudRepository c = appContext.getBean(GenericRepoCrudRepository.class);

        GenericRepo applicationFile = c.getByRepositoryAndRevision("FaceForwardUtils.git", "test");
        applicationFile.getFiles().remove("tst/operations/popo.md");
        applicationFile.getFiles().put("tst/operations/test.md", "Helloworld");
        c.sync(applicationFile);
        System.out.println("Time taken update application.yaml: " + (System.currentTimeMillis() - time));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        time = System.currentTimeMillis();
        MyRepoAppForSpesificFile d = appContext.getBean(MyRepoAppForSpesificFile.class);
        RepoApplicationFile applicationFileNew = new RepoApplicationFile();
        applicationFileNew.setRepo("PartyApp.git");
        applicationFileNew.setBranch("test");


        applicationFileNew.setApplicationYaml(currentNode);
        try {
            d.update(applicationFileNew);
        } catch (OgmRuntimeException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Time taken update not exist file application.yaml: " + (System.currentTimeMillis() - time));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    }

}
