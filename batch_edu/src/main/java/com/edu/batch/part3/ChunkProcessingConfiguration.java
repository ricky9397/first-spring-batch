package com.edu.batch.part3;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChunkProcessingConfiguration {
	
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public Job chunkProcessingJob() {
		return jobBuilderFactory.get("chunkProcessingJob")
				.incrementer(new RunIdIncrementer())
				.start(this.taskBaseStep())
				.next(this.chunkBaseStep())
				.build();
	}

	@Bean
	public Step chunkBaseStep() {
		return stepBuilderFactory.get("chunkBaseStep")
				.<String, String>chunk(10)
				.reader(itemReader())
				.processor(itemProcessor())
				.writer(itemWriter())
				.build();
	}
	
	private ItemWriter<String> itemWriter() {
		return items -> log.info("chunk item size : {}", items.size());
	}

	private ItemProcessor<String, String> itemProcessor() {
		return item -> item + ", String Batch";
	}
	
	private ItemReader<String> itemReader() {
		return new ListItemReader<>(getItems());
	}

	@Bean
	public Step taskBaseStep() {
		return stepBuilderFactory.get("taskBaseStep")
				.tasklet(this.tasklet())
				.build();
	}

	private Tasklet tasklet() {
		return (contribution, chunkContext) -> {
			List<String> items = getItems();
			log.info("task item size : {}", items.size());
			return RepeatStatus.FINISHED;
		};
	}

	private List<String> getItems() {
		List<String> items = new ArrayList<>();
		
		for (int i = 0; i < 100; i++) {
			items.add(i+ "Hello");
		}
		return items;
	}
}
