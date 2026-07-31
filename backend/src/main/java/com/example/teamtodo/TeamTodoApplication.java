package com.example.teamtodo;

import com.example.teamtodo.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TeamTodoApplication {
  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(TeamTodoApplication.class, args);
    AppProperties properties = context.getBean(AppProperties.class);
    if (properties.isMigrateOnly()) {
      System.exit(SpringApplication.exit(context));
    }
  }
}
