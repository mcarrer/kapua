/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.account.module;

import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

/**
 * 
 *
 */
public class AccountServiceModule extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceModule.class);

    private ServiceDiscovery discovery;
    private Record publishedRecord;

    @Override
    public void start() throws Exception {
        super.start();

        discovery = ServiceDiscovery.create(vertx);

        JsonObject metadata = new JsonObject()
                .put("provided-services", new JsonArray()
                        .add(AccountService.class.getName())
                        .add(AccountFactory.class.getName()));

        Record serviceRecord = AccountServiceProviderType.createRecord(AccountServiceModule.class.getName(), metadata);

        discovery.publish(serviceRecord, ar -> {
            if (ar.succeeded()) {
                publishedRecord = ar.result();
                LOGGER.warn("Successfull publication of record {}", serviceRecord.getName());
            } else {
                LOGGER.warn("Something wrong with the publication of record {}", serviceRecord.getName(), ar.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (publishedRecord != null) {
            discovery.unpublish(publishedRecord.getRegistration(),
                    ar -> {
                        if (!ar.succeeded()) {
                            LOGGER.warn("Something wrong with the unpublication of record {}" + publishedRecord.getName(), ar.cause());
                        }
                    });
        }
        if (discovery != null) {
            discovery.close();
        }
    }
}
