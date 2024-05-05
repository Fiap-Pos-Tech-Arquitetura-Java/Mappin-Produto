package br.com.fiap.postech.mappin.produto.batch;

import br.com.fiap.postech.mappin.produto.entities.Produto;
import br.com.fiap.postech.mappin.produto.services.ProdutoService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.multipart.MultipartFile;

@Configuration
public class ExecucaoProgramadaConfiguration {

    private final ProdutoService produtoService;

    @Autowired
    public ExecucaoProgramadaConfiguration(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Bean
    public Job produtoExecucaoProgramadaBatchJob(JobRepository jobRepository, Step stepExecucaoProgramada, Step step2ExecucaoProgramada) {
        return new JobBuilder("cadastroProdutoLoteExecucaoProgramada", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(stepExecucaoProgramada)
                .next(step2ExecucaoProgramada)
                .build();
    }

    @Bean
    public Step stepExecucaoProgramada(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                     ItemReader<Produto> produtoExecucaoProgramadaBatchItemReader,
                     ItemWriter<Produto> produtoBatchExecucaoProgramadaItemWriter,
                     ItemProcessor<Produto, Produto> produtoExecucaoProgramadaBatchItemProcessor) {
        return new StepBuilder("step", jobRepository)
                .<Produto, Produto>chunk(16, platformTransactionManager)
                .reader(produtoExecucaoProgramadaBatchItemReader)
                .processor(produtoExecucaoProgramadaBatchItemProcessor)
                .writer(produtoBatchExecucaoProgramadaItemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step step2ExecucaoProgramada(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, Tasklet taskletExecucaoProgramada) {
        return new StepBuilder("log4fun", jobRepository)
                .tasklet(taskletExecucaoProgramada, platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet taskletExecucaoProgramada() {
        return ((contribution, chunkContext) -> {
            long wait = 100;
            System.out.printf("Waiting %d milliseconds...\n", wait);
            Thread.sleep(wait);
            return RepeatStatus.FINISHED;
        });
    }

    @Bean("produtoExecucaoProgramadaBatchItemReader")
    @StepScope
    public FlatFileItemReader<Produto> produtoExecucaoProgramadaBatchItemReader(@Value("#{jobParameters['novoArquivoProdutoCsv']}") String novoArquivoProdutoCsv) {
        BeanWrapperFieldSetMapper<Produto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Produto.class);
        return new FlatFileItemReaderBuilder<Produto>()
                .name("produtoItemReader")
                .resource(new FileSystemResource(novoArquivoProdutoCsv))
                .delimited()
                .names("nome", "quantidade", "preco")
                .fieldSetMapper(fieldSetMapper)
                .build();
    }

    @Bean
    public ItemWriter<Produto> produtoBatchExecucaoProgramadaItemWriter() {
        return (items) -> items.forEach(produtoService::save);
    }

    @Bean
    public ItemProcessor<Produto, Produto> produtoExecucaoProgramadaBatchItemProcessor() {
        return new ProdutoProcessor();
    }
}
