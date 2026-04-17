package com.gjh.shopdemo.userauth.client.remote.client;

import com.gjh.shopdemo.userauth.client.remote.UserFeignRemote;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "user-auth-service")
public interface UserFeignRemoteClient extends UserFeignRemote {



}
