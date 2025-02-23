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
package io.micronaut.configuration.mongo.coroutine;


import com.mongodb.kotlin.client.coroutine.MongoClient;
import io.micronaut.configuration.mongo.core.DefaultMongoConfiguration;
import io.micronaut.configuration.mongo.core.MongoSettings;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;

/**
 * Factory for the default {@link MongoClient}. Creates the injectable {@link Primary} bean
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@Requires(beans = DefaultMongoConfiguration.class)
@Factory
public class DefaultCoroutineMongoClientFactory {


    /**
     * Factory Method for creating a client.
     * @param mongoConfiguration mongoConfiguration
     * @return mongoClient
     */
    @Bean(preDestroy = "close")
    @Refreshable(MongoSettings.PREFIX)
    @Primary
    public MongoClient mongoClient(DefaultMongoConfiguration mongoConfiguration) {
        return MongoClient.Factory.create(mongoConfiguration.buildSettings(), null);
    }
}
