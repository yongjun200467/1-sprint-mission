package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.service.ChannelService;

import java.util.UUID;
import java.io.Serializable;

public class Channel implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private String name;
    private ChannelType type;
    private final Long createdAt;
    private Long updatedAt;

    public Channel(String name, ChannelType type) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }
    public UUID getId() {return id;}
    public String getName() {return name;}
    public ChannelType getType() {return type;}
    public Long getCreatedAt() {return createdAt;}
    public Long getUpdatedAt() {return updatedAt;}

    public void update(String name, ChannelType type) {
        this.name = name;
        this.type = type;
        this.updatedAt = System.currentTimeMillis();
    }

}