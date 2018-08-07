package com.tale.controller.admin;

import com.blade.ioc.annotation.Inject;
import com.blade.kit.EncryptKit;
import com.blade.kit.StringKit;
import com.blade.mvc.annotation.Param;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PostRoute;
import com.blade.mvc.http.Request;
import com.blade.mvc.ui.RestResponse;
import com.tale.annotation.SysLog;
import com.tale.bootstrap.TaleConst;
import com.tale.controller.BaseController;
import com.tale.model.entity.Users;
import com.tale.service.OptionsService;
import com.tale.service.SiteService;
import lombok.extern.slf4j.Slf4j;

/**
 * 后台控制器
 * Created by biezhi on 2017/2/21.
 */
@Slf4j
@Path(value = "admin", restful = true)
public class SystemController extends BaseController {

    @Inject
    private OptionsService optionsService;

    @Inject
    private SiteService siteService;

    @SysLog("保存个人信息")
    @PostRoute("add_user")
    public RestResponse addUser(@Param String username, @Param String password, @Param String screenName, @Param String email, Request request) {
        if (StringKit.isBlank(username) || StringKit.isBlank(password)
                || StringKit.isBlank(email) || StringKit.isBlank(screenName)) {
            return RestResponse.fail("请确认信息输入完整");
        }

        if (password.length() < 6 || password.length() > 14) {
            return RestResponse.fail("请输入6-14位密码");
        }

        Users newUser = new Users();
        newUser.setScreenName(screenName);
        newUser.setEmail(email);

        String pwd = EncryptKit.md5(username + password);

        newUser.setPassword(pwd);
        newUser.setUsername(username);
        newUser.setCreated((int) (System.currentTimeMillis() / 1000));
        newUser.save();

        return RestResponse.ok();
    }

    @SysLog("保存个人信息")
    @PostRoute("save_profile")
    public RestResponse saveProfile(@Param String screenName, @Param String email, Request request) {
        Users users = this.user();
        if (StringKit.isNotBlank(screenName) && StringKit.isNotBlank(email)) {
            Users temp = new Users();
            temp.setScreenName(screenName);
            temp.setEmail(email);
            temp.updateById(users.getUid());

            users.setScreenName(screenName);
            users.setEmail(email);
            request.session().attribute(TaleConst.LOGIN_SESSION_KEY, users);
        }
        return RestResponse.ok();
    }

    @SysLog("修改登录密码")
    @PostRoute("password")
    public RestResponse upPwd(@Param String old_password, @Param String password, Request request) {
        Users users = this.user();
        if (StringKit.isBlank(old_password) || StringKit.isBlank(password)) {
            return RestResponse.fail("请确认信息输入完整");
        }

        if (!users.getPassword().equals(EncryptKit.md5(users.getUsername() + old_password))) {
            return RestResponse.fail("旧密码错误");
        }
        if (password.length() < 6 || password.length() > 14) {
            return RestResponse.fail("请输入6-14位密码");
        }

        Users temp = new Users();
        String pwd = EncryptKit.md5(users.getUsername() + password);
        temp.setPassword(pwd);
        temp.updateById(users.getUid());
        return RestResponse.ok();
    }

}
