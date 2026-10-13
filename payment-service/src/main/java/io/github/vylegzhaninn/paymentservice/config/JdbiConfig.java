package io.github.vylegzhaninn.paymentservice.config;

import io.github.vylegzhaninn.paymentservice.repository.PaymentRepository;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JdbiConfig {

    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource).installPlugin(new SqlObjectPlugin());
    }

    @Bean
    public PaymentRepository paymentRepository(Jdbi jdbi) {
        return jdbi.onDemand(PaymentRepository.class);
    }
}
