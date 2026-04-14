package com.gjh.shopdemo.userauth.client.remote.client;

import com.gjh.shopdemo.userauth.client.remote.UserAuthFeignRemote;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "user-auth-service", path = "/api/user")
public interface UserAuthFeignRemoteClient extends UserAuthFeignRemote {


}
