# Spring Batch Example (with Kotlin)

- Simple Chunk-Oriented Job
- Parallel Job (w/ Partitioner)
- Simple Tasklet Job

## Getting Started

1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop)
2. run `scripts/prerequisites.sh`
3. run `./gradlew bootRun`
4. If you run only one job, edit `spring.batch.job.names` in `application.yml`

**Example)**

```yaml
spring:
  batch:
    job:
      names: SimpleFetchJob
```

5. Put JobParameters for `ParallelJob`

```
fileNames=1.txt,2.txt,3.txt
```