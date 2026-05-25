package com.io.begstd.dice;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EntityScan(basePackages = { "com.io.begstd.dice.model.domain" })
@ComponentScan("com.io.begstd.dice")
@EnableAspectJAutoProxy
@Profile("!integration")
public class BaseDiceProjectApplication {
    
	public static void main(String[] args) {
		new SpringApplicationBuilder(BaseDiceProjectApplication.class)
				.properties("spring.config.name=application-dice")
				.run(args);
	}
}
