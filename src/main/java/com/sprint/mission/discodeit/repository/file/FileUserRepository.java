package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileUserRepository implements UserRepository {

    // 유저 하나당 파일 하나로 저장시킴
    private final Path directory;

    // 직렬화 파일 확장자
    private static final String EXTENSION = ".ser";

    public FileUserRepository() {
        // 프로젝트 실행경로 기준으로 유저 전용 디렉토리를 생성시킴
        this.directory = Paths.get(
                System.getProperty("user.dir"),
                "file-data-map",
                User.class.getSimpleName()
        );

        // 저장폴더가 없으면 생성
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
    public User save(User user) {
        // save는 항상 덮어쓰기
        Path path = resolvePath(user.getId());

        try (ObjectOutputStream oos =
                     new ObjectOutputStream(Files.newOutputStream(path))) {

            oos.writeObject(user);
            return user;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        Path path = resolvePath(id);

        // 파일이 없으면? 데이터도 없다~~~
        if (Files.notExists(path)) {
            return Optional.empty();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(Files.newInputStream(path))) {

            return Optional.of((User) ois.readObject());

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        // 디렉토리에 있는 모든 ser파일을 돌아댕김
        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(directory, "*" + EXTENSION)) {

            for (Path path : stream) {
                try (ObjectInputStream ois =
                             new ObjectInputStream(Files.newInputStream(path))) {
                    users.add((User) ois.readObject());
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    @Override
    public boolean existsById(UUID id) {
        // 파일 존재여부는 데이터의 존재여부다 이 말이다
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
