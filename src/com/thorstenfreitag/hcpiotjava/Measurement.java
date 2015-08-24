package com.thorstenfreitag.hcpiotjava;

import static javax.persistence.GenerationType.AUTO;

import java.io.Serializable;

import java.sql.Timestamp;

import javax.persistence.Entity;

import javax.persistence.GeneratedValue;

import javax.persistence.Id;

import javax.persistence.NamedQueries;

import javax.persistence.NamedQuery;

 

@Entity

@NamedQueries({

        @NamedQuery(name = "AllMeasurements", query = "select m from Measurement m"),

        @NamedQuery(name = "LastSensorReading", query = "select m from Measurement m where m.sensorId = :paramSensorId and m.storedAt =  (SELECT MAX(r.storedAt) from Measurement r where r.sensorId = :paramSensorId)"),

        @NamedQuery(name = "LastReadingsFromSensor", query = "select p from Measurement p where p.sensorId = :paramSensorId order by p.storedAt DESC") })

public class Measurement implements Serializable {

 

    private static final long serialVersionUID = 1L;

 

    public Measurement() {}

    
  //Important: in Gson, the names of variables must be same and even the dataType should be correct otherwise it creates problems	
    public Measurement(String event, long sensorId) {
    	this.event = event;
    	this.sensorId = sensorId;
    }

    @Id

    @GeneratedValue(strategy = AUTO)

    private Long id;

    private String event;

    private Timestamp storedAt;

    private long sensorId;

 

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getEvent() {return event;}

    public void setEvent(String event) {this.event = event;}

    public Timestamp getStoredAt() {return storedAt;}

    public void setStoredAt(Timestamp dateStored) {this.storedAt = dateStored;}

    public static long getSerialversionuid() {return serialVersionUID;}

    public long getSensorId() {return sensorId;}

    public void setSensorId(long param) {this.sensorId = param;}

}
