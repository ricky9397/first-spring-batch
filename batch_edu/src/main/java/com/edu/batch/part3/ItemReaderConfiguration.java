package com.edu.batch.part3;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ItemReaderConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	
	@Bean
	public Job itemReaderJob() throws Exception {
		return jobBuilderFactory.get("itemReaderJob")
				.incrementer(new RunIdIncrementer())
				.start(this.jpaStep())
				.build();
	}

	@Bean
	public Step jpaStep() throws Exception {
		return stepBuilderFactory.get("jpaStep")
				.<Person, Person>chunk(10)
				.reader(this.jpaCursorItemReader())
				.writer(itemWriter())
				.build();
	}
	
	private ItemWriter<Person> itemWriter() {
		return items -> log.info(items.stream()
				.map(Person::getName)
				.collect(Collectors.joining(",")));
	}

	private JpaCursorItemReader<Person> jpaCursorItemReader() throws Exception{
		JpaCursorItemReader<Person> itemReader = new JpaCursorItemReaderBuilder<Person>()
				.name("jpaCursorItemReader")
				.entityManagerFactory(entityManagerFactory)
				.queryString("select p from Person p")
				.build();
		
		itemReader.afterPropertiesSet();
		
		return itemReader;
		
	}
	
}
