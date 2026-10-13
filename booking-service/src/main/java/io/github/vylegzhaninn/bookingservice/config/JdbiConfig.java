package io.github.vylegzhaninn.bookingservice.config;

import io.github.vylegzhaninn.bookingservice.repository.BookingRepository;
import io.github.vylegzhaninn.bookingservice.repository.RoomRepository;
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
    public RoomRepository roomRepository(Jdbi jdbi) {
        return jdbi.onDemand(RoomRepository.class);
    }

    @Bean
    public BookingRepository bookingRepository(Jdbi jdbi) {
        return jdbi.onDemand(BookingRepository.class);
    }
}
