package dev.minz.springbatchexample.fetchjob

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource

@Configuration
class SimpleFetchJobManager(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    companion object {
        private const val JOB_NAME = "SimpleFetchJob"
    }

    @Bean
    fun simpleFetchJob(
        @Qualifier("simpleFetchStep") fetchStep: Step
    ): Job =
        jobBuilderFactory[JOB_NAME]
            .preventRestart()
            .start(fetchStep)
            .build()

    @Bean
    fun simpleFetchStep(
        @Qualifier("simpleFileItemReader") itemReader: FlatFileItemReader<String>
    ): Step =
        stepBuilderFactory["$JOB_NAME.fetch"]
            .chunk<String, String>(100)
            .reader(itemReader)
            .processor(
                ItemProcessor {
                    println(it)
                    it
                }
            )
            .writer {
                println(it)
            }
            .build()

    @Bean
    fun simpleFileItemReader(): FlatFileItemReader<String> =
        FlatFileItemReaderBuilder<String>()
            .saveState(false)
            .lineMapper { line, _ -> line }
            .resource(FileSystemResource("a.txt"))
            .build()
}
