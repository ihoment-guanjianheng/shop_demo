package com.gjh.shopdemo.userauth.client.remote.client;

import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.pojo.vo.UserInfoVO;
import com.gjh.shopdemo.userauth.client.remote.UserFeignRemote;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(name = "user-auth-service")
public interface UserFeignRemoteClient extends UserFeignRemote {



}
