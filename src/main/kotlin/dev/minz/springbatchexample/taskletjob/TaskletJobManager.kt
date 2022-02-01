package dev.minz.springbatchexample.taskletjob

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TaskletJobManager(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {
    companion object {
        private const val JOB_NAME = "TaskletJob"
    }

    @Bean
    fun taskletJob(
        @Qualifier("taskletStep") step: Step
    ): Job =
        jobBuilderFactory[JOB_NAME]
            .start(step)
            .build()

    @Bean
    @JobScope
    fun taskletStep(): Step =
        stepBuilderFactory["$JOB_NAME.tasklet"]
            .tasklet { _, _ ->
                println("Tasklet Job")
                RepeatStatus.FINISHED
            }
            .build()
}
