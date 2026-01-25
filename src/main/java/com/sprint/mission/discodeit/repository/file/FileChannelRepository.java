package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileChannelRepository implements ChannelRepository {

    private final Path directory;
    private static final String EXTENSION = ".ser";

    public FileChannelRepository() {
        this.directory = Paths.get(
                System.getProperty("user.dir"),
                "file-data-map",
                Channel.class.getSimpleName()
        );

        if (Files.notExists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path resolvePath(UUID id) {
        return directory.resolve(id + EXTENSION);
    }

    @Override
    public Channel save(Channel channel) {
        Path path = resolvePath(channel.getId());

        try (ObjectOutputStream oos =
                     new ObjectOutputStream(Files.newOutputStream(path))) {

            oos.writeObject(channel);
            return channel;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        Path path = resolvePath(id);

        if (Files.notExists(path)) {
            return Optional.empty();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(Files.newInputStream(path))) {

            return Optional.of((Channel) ois.readObject());

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Channel> findAll() {
        List<Channel> channels = new ArrayList<>();

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(directory, "*" + EXTENSION)) {

            for (Path path : stream) {
                try (ObjectInputStream ois =
                             new ObjectInputStream(Files.newInputStream(path))) {
                    channels.add((Channel) ois.readObject());
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return channels;
    }

    @Override
    public boolean existsById(UUID id) {
        return Files.exists(resolvePath(id));
    }

    @Override
    public void deleteById(UUID id) {
        try {
            Files.deleteIfExists(resolvePath(id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
