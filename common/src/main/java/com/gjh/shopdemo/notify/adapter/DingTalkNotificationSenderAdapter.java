package com.gjh.shopdemo.notify.adapter;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.gjh.shopdemo.notify.NotificationSender;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component("dingtalk")
@Slf4j
public class DingTalkNotificationSenderAdapter implements NotificationSender {

    @Value("${dingtalk.custom.robot.token}")
    private String CUSTOM_ROBOT_TOKEN ;

    @Value("${dingtalk.custom.robot.secret}")
    private String SECRET;

    @Override
    public void send(String message, List<String> receivers) {
        try {
            Long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + SECRET;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(Base64.getEncoder().encodeToString(signData), String.valueOf(StandardCharsets.UTF_8));

            DingTalkClient client = new DefaultDingTalkClient(
                    "https://oapi.dingtalk.com/robot/send?sign=" + sign + "&timestamp=" + timestamp);
            OapiRobotSendRequest req = new OapiRobotSendRequest();
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent(message);
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtUserIds(receivers);
            req.setMsgtype("text");
            req.setText(text);
            req.setAt(at);
            OapiRobotSendResponse rsp = client.execute(req, CUSTOM_ROBOT_TOKEN);
            log.info("钉钉消息发送结果: {}", rsp.getBody());
        } catch (ApiException e) {
            log.error("钉钉消息发送失败, errCode={}, errMsg={}", e.getErrCode(), e.getErrMsg(), e);
            throw new BaseException("钉钉消息发送失败: " + e.getErrMsg());
        } catch (Exception e) {
            log.error("钉钉消息发送失败", e);
            throw new BaseException("钉钉消息发送失败");
        }
    }
}
