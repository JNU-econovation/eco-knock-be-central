package jnu.econovation.ecoknockbecentral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EcoKnockBeCentralApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcoKnockBeCentralApplication.class, args);
    }

}
