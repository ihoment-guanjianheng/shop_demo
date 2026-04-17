package com.gjh.shopdemo.context;

import com.gjh.shopdemo.pojo.vo.UserInfoVO;
import lombok.Data;

@Data
public class AuthContext {

    private static final ThreadLocal<UserInfoVO> userInfo = new ThreadLocal<>();

    public static void setCurrentUser(UserInfoVO user) {
        userInfo.set(user);
    }

    public static UserInfoVO getCurrentUser() {
        return userInfo.get();
    }

    public static void removeCurrentUser() {
        userInfo.remove();
    }

}
