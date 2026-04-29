package org.lnu.teaching.web.application.design.deanery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class ConcurrencyConfig {
    @Bean
    public ThreadPoolTaskExecutor defaultThreadPoolTaskExecutor(@Value("${default.thread.pool.core.size}") int corePoolSize,
                                                                @Value("${default.thread.pool.max.size}") int maxPoolSize) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("default_task_executor_thread");
        executor.initialize();

        return executor;
    }

    @Bean
    public Scheduler defaultScheduler(ThreadPoolTaskExecutor defaultThreadPoolTaskExecutor) {
        return Schedulers.fromExecutor(defaultThreadPoolTaskExecutor);
    }
}
