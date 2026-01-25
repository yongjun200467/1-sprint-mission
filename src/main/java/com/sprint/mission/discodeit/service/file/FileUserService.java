//User를 파일 하나로 저장하는 서비스

package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class FileUserService implements UserService {
    //FileUserService는 UserService라는 인터페이스를 따라 만든 공개클래스
    private final Path DIRECTORY;
    //파일을 저장할 디렉토리 경로를 담는 변경 불가능한 변수 - 선언만 하고 값은 없어서 생성자에서 초기화해야함
    private final String EXTENSION = ".ser";//.ser은 Serializable(직렬화)파일확장자이다.
    //파일 저장시에 사용할 직렬화 파일 확장자 - 선언과 동시에 값이 정해져 생성자에서 초기화할 필요 없음

    public FileUserService() {
    //FileUserService가 생성될때 실행되는 생성자 선언
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map", User.class.getSimpleName());
        //this.DIRECTORY = final이라서 여기서 값 설정
        //patgs.get = 내용들을 하나의 디렉토리 경로로 조합
        //System.getProperty("user.dir" = 현재 프로젝트 실행 경로
        //"file-data-map" = 데이터 저장용 폴더 이름
        //User.class.getSimpleName() = "User" 문자열 반환
//===유저 데이터를 저장할 디렉토리 경로를 생성하고 설정함===

        if (Files.notExists(DIRECTORY)) {
        //저장할 디렉토리가 있는지 확인
            try {
                Files.createDirectories(DIRECTORY);
                //필요한 디렉토리를 자동으로 생성(이미 존재하도 노에러)
            } catch (IOException e) {//자바에서 입출력 관련 예외를 처리하려고 만든 클래스
                throw new RuntimeException(e);
                //디렉토리 생성 실패시 프로그램 에러
            }
        }
    }
    //======FileUserService 생성 시에 유저 데이터를 저장할 폴더를 준비하고 확인======

    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }
    //UUID를 받아서 그에 맞는 파일 경로를 만들어주는 메서드

    @Override //생성//Override = 부모의 메서드를 재정의한다는 표시
    public User create(String username, String email, String phone) {
        User user = new User(username, email, phone); //새로운 유저 객체를 메모리에 생성
        Path path = resolvePath(user.getId()); //생성한 유저객체를 저장할 파일 경로를 설정
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());//파일에 바이트 단위 출력
                ObjectOutputStream oos = new ObjectOutputStream(fos)//객체를 직렬화해서 파일에 씀
        ) { //파일에 객체를 쓰기 위해서 출력 스트림을 준비하고 자동으로 닫음
            oos.writeObject(user);
        } catch (IOException e) { //예외처리
            throw new RuntimeException(e);
        }

        return user;
    }

    @Override //단건조회
    public User find(UUID userId) {
        User userNullable = null;
        Path path = resolvePath(userId);
        if (Files.exists(path)) {
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                userNullable = (User) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.ofNullable(userNullable)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    }

    @Override //다건조회
    public List<User> findAll() {
        try {
            return Files.list(DIRECTORY)
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (User) ois.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override //수정
    public User updated(UUID userId, String newUsername, String newEmail, String newPassword) {
        User userNullable = null;
        Path path = resolvePath(userId);
        if (Files.exists(path)) {
            try (
                    FileInputStream fis = new FileInputStream(path.toFile());
                    ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                userNullable = (User) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        User user = Optional.ofNullable(userNullable)
                .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
        user.update(newUsername, newEmail, newPassword);

        try(
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    @Override //삭제
    public void delete(UUID userId) {
        Path path = resolvePath(userId);
        if (Files.notExists(path)) {
            throw new NoSuchElementException("User with id " + userId + " not found");
        }
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}