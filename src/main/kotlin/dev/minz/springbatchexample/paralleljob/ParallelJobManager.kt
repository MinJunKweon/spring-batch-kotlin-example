package dev.minz.springbatchexample.paralleljob

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class ParallelJobManager(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    companion object {
        private const val THREAD_COUNT = 3
        private const val JOB_NAME = "ParallelJob"

        private const val CONTEXT_FILE_NAME = "fileName"
    }

    @Bean
    fun parallelJob(
        @Qualifier("parallelFetchManagerStep") managerStep: Step
    ): Job =
        jobBuilderFactory[JOB_NAME]
            .start(managerStep)
            .build()

    @Bean
    @JobScope
    fun parallelFetchManagerStep(
        @Qualifier("parallelPartitioner") partitioner: Partitioner,
        @Qualifier("asyncTaskExecutor") asyncTaskExecutor: TaskExecutor,
        @Qualifier("parallelItemReader") itemReader: FlatFileItemReader<String>
    ): Step =
        stepBuilderFactory["$JOB_NAME.fetch.manager"]
            .partitioner("partitioner", partitioner)
            .gridSize(THREAD_COUNT)
            .taskExecutor(asyncTaskExecutor)
            .step(parallelFetchWorkerStep(itemReader))
            .build()

    fun parallelFetchWorkerStep(itemReader: ItemReader<String>): Step =
        stepBuilderFactory["$JOB_NAME.worker"]
            .chunk<String, String>(100)
            .reader(itemReader)
            .processor(
                ItemProcessor {
                    println("worker : $it")
                    it
                }
            )
            .writer {
                println("write : $it")
            }
            .build()

    @Bean
    @JobScope
    fun parallelPartitioner(
        @Value("#{jobParameters[fileNames]}") fileNames: String
    ): Partitioner =
        Partitioner {
            fileNames.split(",").associate {
                "worker-$it" to ExecutionContext().apply {
                    put(CONTEXT_FILE_NAME, it)
                }
            }
        }

    @Bean
    @StepScope
    fun parallelItemReader(
        @Value("#{stepExecutionContext[$CONTEXT_FILE_NAME]}") fileName: String
    ): FlatFileItemReader<String> =
        FlatFileItemReaderBuilder<String>()
            .lineMapper { line, _ -> line }
            .resource(FileSystemResource("data/$fileName"))
            .saveState(false)
            .build()

    @Bean
    fun asyncTaskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = THREAD_COUNT
            maxPoolSize = THREAD_COUNT
        }
}
