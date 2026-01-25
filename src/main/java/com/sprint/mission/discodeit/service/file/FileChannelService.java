package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {
    private final Path DIRECTORY;
    private static final String EXTENSION = ".ser";

    public FileChannelService() {
    this.DIRECTORY = Paths. get(
            System.getProperty("user.dir"),
            "file-data-map",
            Channel.class.getSimpleName()
    );

    try {
        Files.createDirectories(DIRECTORY);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

private Path resolvePath(UUID id) {
    return DIRECTORY.resolve(id.toString() + EXTENSION);
}
    @Override
    public Channel create(String name, ChannelType type) {
        Channel channel = new Channel(name, type);
        Path path = resolvePath(channel.getId());

        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return channel;
    }

    @Override
    public Channel find(UUID channelId) {
        Path path = resolvePath(channelId);

        if (Files.notExists(path)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(path.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (Channel) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Channel> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (Channel) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Channel update(UUID channelId, String name, ChannelType type) {
        Channel channel = find(channelId);
        if (channel == null) {
            return null;
        }

        channel.update(name, type);
        Path path = resolvePath(channelId);

        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return channel;
    }

    @Override
    public void delete(UUID channelId) {
        Path path = resolvePath(channelId);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}




