package com.gigaspaces.fdal.network;

/*
 * Copyright (c) 2008-2016, GigaSpaces Technologies, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.gigaspaces.logger.Constants;
import com.gigaspaces.lrmi.INetworkMapper;
import com.gigaspaces.lrmi.ServerAddress;
import com.j_spaces.kernel.SystemProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This mapping class will take existing configuration but map IP address
 * instead of ports
 *
 * @author Raney
 * @since 12.0
 */
public class NetworkMapAllPorts implements INetworkMapper {
    private static final Logger _logger = Logger.getLogger(Constants.LOGGER_LRMI);

    private static final String NETWORK_MAPPING_FILE = System.getProperty(SystemProperties.LRMI_NETWORK_MAPPING_FILE, "config/network_mapping.config");
    private static final String MALFORMED_FORMAT_MSG = "Unsupported format of network mapping file, " + "expected format is separated lines each contains a separate mapping: " + "<original ip>:<original port>,<mapped ip>:<mapped port> for instance 10.0.0.1:4162,212.321.1.1:3000";

    private final Map<String, String> _mapping = new HashMap<String, String>();

    private ConcurrentMap<ServerAddress, ServerAddress> _mappingCache = new ConcurrentHashMap<ServerAddress, ServerAddress>();

    public NetworkMapAllPorts() {
        try (InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(NETWORK_MAPPING_FILE)) {
            if (resourceAsStream == null) {
                if (_logger.isLoggable(Level.INFO))
                    _logger.info("Could not locate networking mapping file " + NETWORK_MAPPING_FILE + " in the classpath, no mapping created");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    // Rermarked line
                    if (line.startsWith("#"))
                        continue;

                    String[] split = line.split(",");

                    if (split.length != 2)
                        throw new IllegalArgumentException(MALFORMED_FORMAT_MSG);

                    ServerAddress originalServerAddress = getServerAddress(split[0].trim());
                    ServerAddress mappedServerAddress = getServerAddress(split[1].trim());

                    ServerAddress old = _mappingCache.putIfAbsent(originalServerAddress, mappedServerAddress);
                    if (old == null && _logger.isLoggable(Level.FINE))
                        _logger.fine("Adding mapping of " + originalServerAddress + " to " + mappedServerAddress);

                    String originalHostName = originalServerAddress.getHost();
                    String mappedHostName = mappedServerAddress.getHost();
                    if (_mapping.containsKey(originalHostName))
                        continue;

                    if (_logger.isLoggable(Level.FINE))
                        _logger.fine("Adding mapping of " + originalHostName + " to " + mappedHostName);

                    _mapping.put(originalHostName, mappedHostName);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while parsing the network mapping file " + NETWORK_MAPPING_FILE, e);
        }
    }

    private ServerAddress getServerAddress(String string) {
        String[] split = string.split(":");
        if (split.length != 2)
            throw new IllegalArgumentException(MALFORMED_FORMAT_MSG);

        return new ServerAddress(split[0], Integer.parseInt(split[1]));

    }

    @Override
    public ServerAddress map(ServerAddress serverAddress) {

        /***
         * Check Cached Map for existing entry
         */
        ServerAddress transformedAddress = _mappingCache.get(serverAddress);

        if (transformedAddress != null) {
            return transformedAddress;
        }

        String host = serverAddress.getHost();

        String transformedHost = _mapping.get(host);

        /***
         * No mapping, return original
         */
        if (transformedHost == null) {
            if (_logger.isLoggable(Level.FINEST))
                _logger.finest("No mapping exists for provided address " + serverAddress + " returning original address");
            return serverAddress;
        }
        Integer port = serverAddress.getPort();

        /***
         * Create New Entry in server map
         */
        if (_logger.isLoggable(Level.FINEST))
            _logger.finest("Mapping  address " + serverAddress + " to " + transformedHost + ":" + port);

        transformedAddress = new ServerAddress(transformedHost, port);
        _mappingCache.put(serverAddress, transformedAddress);

        return transformedAddress;
    }

    public class NetworkMappingRange {
        private String originalIP;
        private String mappedIP;
        private Integer startingPort;
        private Integer endingPort;

        public String getOriginalIP() {
            return originalIP;
        }

        public void setOriginalIP(String originalIP) {
            this.originalIP = originalIP;
        }

        public String getMappedIP() {
            return mappedIP;
        }

        public void setMappedIP(String mappedIP) {
            this.mappedIP = mappedIP;
        }

        public Integer getStartingPort() {
            return startingPort;
        }

        public void setStartingPort(Integer startingPort) {
            this.startingPort = startingPort;
        }

        public Integer getEndingPort() {
            return endingPort;
        }

        public void setEndingPort(Integer endingPort) {
            this.endingPort = endingPort;
        }

    }

}