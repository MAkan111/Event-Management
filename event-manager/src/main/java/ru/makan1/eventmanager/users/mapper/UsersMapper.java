package ru.makan1.eventmanager.users.mapper;

import ru.makan1.eventmanager.users.dto.UsersRegistration;
import ru.makan1.eventmanager.users.dto.UsersResponse;
import ru.makan1.eventmanager.users.entity.UsersEntity;
import ru.makan1.eventmanager.users.enums.UserRole;

public class UsersMapper {

    public static UsersEntity mapToUsersEntity(UsersRegistration usersRegistration) {
        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setLogin(usersRegistration.login());
        usersEntity.setPasswordHash(usersRegistration.password());
        usersEntity.setAge(usersRegistration.age());
        usersEntity.setRole(UserRole.USER);
        return usersEntity;
    }

    public static UsersResponse mapToUserResponse(UsersEntity usersEntity) {
        return new UsersResponse(
                usersEntity.getUserId(),
                usersEntity.getLogin(),
                usersEntity.getAge(),
                usersEntity.getRole()
        );
    }
}
