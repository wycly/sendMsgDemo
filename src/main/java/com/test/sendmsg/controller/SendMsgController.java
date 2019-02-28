package com.test.sendmsg.controller;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.test.util.SendMsgUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Author:
 * Date:2019/2/25 11:38
 */
@Controller
@RequestMapping("/test")
public class SendMsgController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送短信
     * @param phone   手机号
     * @return
     * @throws ClientException
     */
    @ResponseBody
    @PostMapping("/sendMsg")
    public Map sendMsg(String phone) throws ClientException {
        HashMap<String, Object> resultMap = new HashMap<>();
        String signName = "爱翼保";
        String templeCode = "SMS_143862675";
        HashMap<String, String> map = new HashMap<>();
        String code = getCheckCode(6);
        System.out.println(code);
        map.put("code",code);  //这个code是你模板中定义的变量值
//        redisTemplate.boundHashOps("smsCode").put(phone,code);
        redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);  //保存到Redis中有效时长为5分钟
        String templeParam  = JSON.toJSONString(map);
        SendSmsResponse response = SendMsgUtil.sendSms(phone, signName, templeCode, templeParam );
//        SendSmsResponse response = SendMsgUtil.sendSms(phone, signName, templeCode, templeParam , "");
        if ("ok".equalsIgnoreCase(response.getCode())){
            resultMap.put("message","发送成功");
        }else {
            resultMap.put("message","发送失败");
        }
        return resultMap;
    }

    /**
     * 检验验证码
     * @param phone   手机号
     * @param code    验证码
     * @return
     */
    @ResponseBody
    @PostMapping("/checkCode")
    public Map checkCode(String phone,String code){
        HashMap<String, Object> resultMap = new HashMap<>();
        String smsCode = (String) redisTemplate.opsForValue().get(phone);
        if (smsCode == null){
            resultMap.put("失败","验证码错误");
            return resultMap;
        }
//        String smsCode = (String) redisTemplate.boundHashOps("smsCode").get(phone);
        if (smsCode.equals(code)){
            resultMap.put("成功","输入的验证码与Redis中的验证码一致");
            return resultMap;
        }else {
            resultMap.put("错误","输入的验证码与Redis中的验证码一致不一致");
            return resultMap;
        }
    }

    //生成验证码
    public static String getCheckCode(int length){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            //将随机生成的单个数字转换成字符串进行拼接
            sb.append(getNumber()+"");
        }
        return sb.toString();
    }
    //随机生成一个数字(0,1,2,3,4,5,6,7,8,9)
    public static int  getNumber(){
        Random random = new Random();
        //random.nextInt(int n),随机生成一个[0,n)的数,包含0,不包含n
        int unicode = random.nextInt(10);
        return unicode;
    }
}
