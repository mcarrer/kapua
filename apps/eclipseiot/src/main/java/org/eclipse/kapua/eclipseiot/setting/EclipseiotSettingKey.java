/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.eclipseiot.setting;

import org.eclipse.kapua.commons.setting.SettingKey;

/**
 * Broker settings
 */
public enum EclipseiotSettingKey implements SettingKey {

    /**
     * Verticle class implementation to instantiate
     */
    VERTICLE_CLASS_NAME("eclipseiot.verticle.class"),
    /**
     * Transport converter class
     */
    TRANSPORT_CONVERTER_CLASS_NAME("eclipseiot.converter.transport.class"),
    /**
     * Application converter class
     */
    APPLICATION_CONVERTER_CLASS_NAME("eclipseiot.converter.application.class"),
    /**
     * Processor class
     */
    PROCESSOR_CLASS_NAME("eclipseiot.processor.class");

    private String key;

    private EclipseiotSettingKey(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }
}
