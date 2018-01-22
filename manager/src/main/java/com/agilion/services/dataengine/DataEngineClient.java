package com.agilion.services.dataengine;

import dataengine.api.Operation;

import java.util.List;
import java.util.Map;

public interface DataEngineClient
{
    public List<String> getSelectorTypes();

    public List<String> getDataSources();

    public List<Operation> listOperations();

    public NetworkBuildReceipt startNetworkBuild(String sessionID, String username, List<String> dataFilePaths, Map<String, Object> params)
            throws Exception;

    public boolean networkBuildIsDone(NetworkBuildReceipt receipt);
}
