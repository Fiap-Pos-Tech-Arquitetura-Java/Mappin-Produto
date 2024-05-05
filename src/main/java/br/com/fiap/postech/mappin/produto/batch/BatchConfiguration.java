package br.com.fiap.postech.mappin.produto.batch;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.services.ProdutoService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    private final ProdutoService produtoService;

    @Autowired
    public BatchConfiguration(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Bean
    public Job produtoBatchJob(JobRepository jobRepository, Step step, Step step2) {
        return new JobBuilder("cadastroProdutoLoteInicializacao", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .next(step2)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                     ItemReader<Produto> produtoBatchItemReader,
                     ItemWriter<Produto> produtoBatchItemWriter,
                     ItemProcessor<Produto, Produto> produtoBatchItemProcessor) {
        return new StepBuilder("step", jobRepository)
                .<Produto, Produto>chunk(16, platformTransactionManager)
                .reader(produtoBatchItemReader)
                .processor(produtoBatchItemProcessor)
                .writer(produtoBatchItemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, Tasklet tasklet) {
        return new StepBuilder("log4fun", jobRepository)
                .tasklet(tasklet, platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet tasklet() {
        return ((contribution, chunkContext) -> {
            long wait = 100;
            System.out.printf("Waiting %d milliseconds...\n", wait);
            Thread.sleep(wait);
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public ItemReader<Produto> produtoBatchItemReader() {
        BeanWrapperFieldSetMapper<Produto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Produto.class);
        return new FlatFileItemReaderBuilder<Produto>()
                .name("produtoItemReader")
                .resource(new ClassPathResource("produto.csv"))
                .delimited()
                .names("nome", "quantidade", "preco")
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

    @Bean
    public ItemWriter<Produto> produtoBatchItemWriter() {
        return (items) -> items.forEach(produtoService::save);
    }

    @Bean
    public ItemProcessor<Produto, Produto> produtoBatchItemProcessor() {
        return new ProdutoProcessor();
    }
}
