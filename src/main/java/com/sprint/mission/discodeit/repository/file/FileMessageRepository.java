package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileMessageRepository implements MessageRepository {

    private final Path DIRECTORY;
    private static final String EXTENSION = ".ser";

    public FileMessageRepository() {
        this.DIRECTORY = Paths.get(
                System.getProperty("user.dir"),
                "file-data-map",
                Message.class.getSimpleName()
        );

        if (Files.notExists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    @Override
    public Message save(Message message) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(Files.newOutputStream(resolvePath(message.getId())))) {
            oos.writeObject(message);
            return message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> findById(UUID id) {
        Path path = resolvePath(id);
        if (Files.notExists(path)) return Optional.empty();

        try (ObjectInputStream ois =
                     new ObjectInputStream(Files.newInputStream(path))) {
            return Optional.of((Message) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findAll() {
        List<Message> messages = new ArrayList<>();

        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(DIRECTORY, "*" + EXTENSION)) {

            for (Path path : stream) {
                try (ObjectInputStream ois =
                             new ObjectInputStream(Files.newInputStream(path))) {
                    messages.add((Message) ois.readObject());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return messages;
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
