package com.gigaspaces.fdal.controller;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainer;
import org.openspaces.pu.container.support.CompoundProcessingUnitContainer;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;

public class TestCluster {
    private static final Logger LOG = LoggerFactory.getLogger(TestCluster.class);

    private String configPath;
    private List<ApplicationContext> membersContexts;
    
    public TestCluster() {
    }

    public TestCluster(String configPath) {
        this.configPath = configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    @PostConstruct
    public void init() {
        Assert.hasText(configPath, "'configPath' property must be set");

        LOG.info("Starting the cluster: config = " + configPath);
        long time = System.currentTimeMillis();

        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setSchema("partitioned-sync2backup");
        clusterInfo.setNumberOfInstances(2);
        clusterInfo.setNumberOfBackups(0);

        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        try {
            provider.addConfigLocation(configPath);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to read cluster member config from path: " + configPath, exception);
        }
        provider.setClusterInfo(clusterInfo);
        ProcessingUnitContainer container = provider.createContainer();

        membersContexts = new ArrayList<ApplicationContext>();
        checkContext(container, membersContexts);
        
        time = System.currentTimeMillis() - time;
        LOG.info("Cluster initialization finished in {} seconds", oneDigit(time / 1000.0));
    }

    private void checkContext(ProcessingUnitContainer container, List<ApplicationContext> contexts) {
        if (container instanceof CompoundProcessingUnitContainer) {
            for (ProcessingUnitContainer actualContainer : ((CompoundProcessingUnitContainer) container).getProcessingUnitContainers()) {
                checkContext(actualContainer, contexts);
            }
        } else if (container instanceof ApplicationContextProcessingUnitContainer) {
            ResourceApplicationContext context = (ResourceApplicationContext) ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            LOG.info("Cluster member context state: active = {}, running = {}", context.isActive(), context.isRunning());
            contexts.add(context);
        }
    }

    public static String oneDigit(double value) {
        return new DecimalFormat("#0.0", DecimalFormatSymbols.getInstance(Locale.US)).format(value);
    }

    public List<ApplicationContext> getMembersContexts() {
        return membersContexts;
    }
}