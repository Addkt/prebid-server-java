package org.prebid.server.spring.config;

import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import org.prebid.server.execution.timeout.TimeoutFactory;
import org.prebid.server.geolocation.GeoLocationService;
import org.prebid.server.health.ApplicationChecker;
import org.prebid.server.health.DatabaseHealthChecker;
import org.prebid.server.health.GeoLocationHealthChecker;
import org.prebid.server.health.HealthChecker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@ConditionalOnProperty("status-response")
@ConditionalOnExpression("'${status-response}' != ''")
public class HealthCheckerConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "health-check.database", name = "enabled", havingValue = "true")
    HealthChecker databaseChecker(Vertx vertx,
                                  Pool pool,
                                  @Value("${health-check.database.refresh-period-ms}") long refreshPeriod) {

        return new DatabaseHealthChecker(vertx, pool, refreshPeriod);
    }

    @Bean
    @ConditionalOnExpression("${health-check.geolocation.enabled} == true and ${geolocation.enabled} == true")
    HealthChecker geoLocationChecker(Vertx vertx,
                                     @Value("${health-check.geolocation.refresh-period-ms}") long refreshPeriod,
                                     GeoLocationService geoLocationService,
                                     TimeoutFactory timeoutFactory,
                                     Clock clock) {

        return new GeoLocationHealthChecker(vertx, refreshPeriod, geoLocationService, timeoutFactory, clock);
    }

    @Bean
    HealthChecker applicationChecker(@Value("${status-response}") String statusResponse) {
        return new ApplicationChecker(statusResponse);
    }
}
