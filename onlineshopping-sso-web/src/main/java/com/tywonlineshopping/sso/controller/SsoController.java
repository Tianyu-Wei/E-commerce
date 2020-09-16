package com.tywonlineshopping.sso.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tyw.onlineshopping.bean.UserInfo;
import com.tyw.onlineshopping.service.UserService;
import com.tyw.onlineshopping.util.JwtUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class SsoController {

    @Reference
    UserService userService;

    String jwtKey = "onlineshopping";


    @GetMapping("index")
    public String index(@RequestParam("originUrl") String originUrl, Model model) {
        model.addAttribute("originUrl", originUrl);
        return "index";
    }


    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request) {

        UserInfo userInfoExist = userService.login(userInfo);
        //1
        if (userInfoExist != null) {
            //制作token
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userInfoExist.getId());
            map.put("nickName", userInfoExist.getNickName());
            System.out.println(request.getRemoteAddr());
//            // 如果有 反向代理的话  ，要在反向代理中进行配置  把用户真实ip传递过来

            String ipAddr = request.getHeader("X-forwarded-for");
            String token = JwtUtil.encode(jwtKey, map, ipAddr);

            return token;
        }


        return "fail";
    }

    @GetMapping("verify")
    @ResponseBody
    public String verify(@RequestParam("token") String token, @RequestParam("currentIP") String currentIp) {
        //1 验证token
        Map<String, Object> userMap = JwtUtil.decode(token, jwtKey, currentIp);

        //2 验证缓存
        if (userMap != null) {
            String userId = (String) userMap.get("userId");
            Boolean isLogin = userService.verify(userId);
            if (isLogin) {
                return "success";
            }
        }
        return "fail";
    }
}
