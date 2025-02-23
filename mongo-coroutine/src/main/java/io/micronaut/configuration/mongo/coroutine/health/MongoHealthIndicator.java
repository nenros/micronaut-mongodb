/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.mongo.coroutine.health;

import com.mongodb.kotlin.client.coroutine.MongoClient;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.aggregator.HealthAggregator;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.micronaut.configuration.mongo.coroutine.health.MongoHealthIndicator.HEALTH_INDICATOR_NAME;

/**
 * A {@link HealthIndicator} for MongoDB.
 *
 * @author graemerocher
 * @since 1.0
 */
@Singleton
@Requires(beans = MongoClient.class)
@Requires(beans = HealthEndpoint.class)
@Requires(property = HealthEndpoint.PREFIX + "." + HEALTH_INDICATOR_NAME + ".enabled", notEquals = StringUtils.FALSE)
public class MongoHealthIndicator implements HealthIndicator {
    static final String HEALTH_INDICATOR_NAME = "mongodb";

    private final BeanContext beanContext;
    private final HealthAggregator<?> healthAggregator;
    private final MongoClient[] mongoClients;

    /**
     * @param beanContext beanContext
     * @param healthAggregator healthAggregator
     * @param mongoClients The mongo clients
     */
    public MongoHealthIndicator(BeanContext beanContext, HealthAggregator<?> healthAggregator, MongoClient... mongoClients) {
        this.beanContext = beanContext;
        this.healthAggregator = healthAggregator;
        this.mongoClients = mongoClients;
    }

    @Override
    public Publisher<HealthResult> getResult() {

//        List<BeanRegistration<MongoClient>> registrations = getRegisteredConnections();
//
//        Flux<HealthResult> healthResults = Flux.fromIterable(registrations)
//                .flatMap(this::checkRegisteredMongoClient)
//                .onErrorResume(throwable -> Flux.just(buildStatusDown(throwable, HEALTH_INDICATOR_NAME)));
        final var status = HealthResult.builder("mongo");
        status.status(HealthStatus.UP);

        return this.healthAggregator.aggregate(HEALTH_INDICATOR_NAME, Flux.just(status.build()));
    }

    private List<BeanRegistration<MongoClient>> getRegisteredConnections() {
        return Arrays.stream(mongoClients)
                .map(beanContext::findBeanRegistration)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

//    private Publisher<HealthResult> checkRegisteredMongoClient(BeanRegistration<MongoClient> registration) {
//        MongoClient mongoClient = registration.getBean();
//        String databaseName = "mongodb (" + registration.getIdentifier().getName() + ")";
//
//        Flux<Map<String, String>> databasePings = Flux.from(pingMongo(mongoClient))
//                .map(this::getVersionDetails)
//                .timeout(Duration.of(10, ChronoUnit.SECONDS))
//                .retry(3);
//
//        return databasePings.map(detail -> buildStatusUp(databaseName, detail))
//                .onErrorResume(throwable -> Flux.just(buildStatusDown(throwable, databaseName)));
//    }
//
//    private Document pingMongo(MongoClient mongoClient) {
//Flux.
//        return mongoClient.getDatabase("admin").runCommandDocumentWithSession(new BsonDocument("buildinfo", new BsonInt64(1)),  null, null);
//    }
//
//    private Map<String, String> getVersionDetails(Document document) {
//        String version = document.get("version", String.class);
//        if (version == null) {
//            throw new IllegalStateException("Mongo version not found");
//        }
//        return Collections.singletonMap("version", version);
//    }

    private HealthResult buildStatusUp(String name, Map<String, String> details) {
        HealthResult.Builder builder = HealthResult.builder(name);
        builder.status(HealthStatus.UP);
        builder.details(details);
        return builder.build();
    }

    private HealthResult buildStatusDown(Throwable throwable, String name) {
        HealthResult.Builder builder = HealthResult.builder(name);
        builder.status(HealthStatus.DOWN);
        builder.exception(throwable);
        return builder.build();
    }
}
