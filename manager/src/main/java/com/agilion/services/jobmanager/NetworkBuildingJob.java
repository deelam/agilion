package com.agilion.services.jobmanager;

import java.util.Date;

public class NetworkBuildingJob
{
    private String id;
    private String name;
    private Date startDate;
    private Date endDate;
    private String status;
    private JobState state;

    public NetworkBuildingJob(String id)
    {
        this.startDate = new Date();
        this.status = "Waiting";
        this.state = JobState.NEW;
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}