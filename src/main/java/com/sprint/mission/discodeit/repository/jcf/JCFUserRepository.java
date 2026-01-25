package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.*;

public class JCFUserRepository implements UserRepository {

    // 실행중에만 유지되는 저장소
    private final Map<UUID, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        // 내부 보호를 위해서 복사본을 반환시킨당
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean existsById(UUID id) {
        return users.containsKey(id);
    }

    @Override
    public void deleteById(UUID id) {
        users.remove(id);
    }
}
