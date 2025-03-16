package com.example.service.imp;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImp extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Resource
    AmqpTemplate amqptemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    FlowUtils utils;
    @Resource
    PasswordEncoder encoder;

    // 自定义Security登录验证判断
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if ( account == null )
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    // 查找邮箱或者用户是否存在
    public Account findAccountByNameOrEmail(String text){    // 传入的是用户名或邮箱
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    // 注册用户
    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String username = vo.getUsername();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);   // 取验证码
        if (code == null) return "请先获取验证码";
        if (!code.equals(vo.getCode())) return "验证码输入错误";
        if (this.existsAccountByEmail(email)) return "此邮箱已被注册";
        if (this.existsAccountByUsername(username)) return "此用户名已被使用";

        String password = encoder.encode(vo.getPassword()); // 加密
        Account account = new Account(null, username, password, email, "user", new Date()); // 觉得默认为user，注册时间为现在
        if (this.save(account)) {
            stringRedisTemplate.delete(key);    // 删除key
            return null;
        } else {
          return "内部错误，请联系管理员";
        }
    }

    // 判断邮件、用户名是否已经注册
    private boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }
    private boolean existsAccountByUsername (String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }

    // 注册邮件验证码
    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()){        // 防止被多次调用，做锁
            if (!this.verifyLimit(ip)) return "请求频繁，请稍后再试";
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            amqptemplate.convertAndSend("mail",data);   // 放入队列
            // 存放验证码到redis
            stringRedisTemplate.opsForValue().set(Const.VERIFY_EMAIL_DATA + email,
                    String.valueOf(code),
                    3, TimeUnit.MINUTES);
            return null;
        }
    }

    private  boolean verifyLimit(String ip) {
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key, 60);   // 限制60s
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVO(email,vo.getCode()));
        if(verify != null) return verify;
        String password = encoder.encode(vo.getPassword());
        boolean update = this.update().eq("email",email).set("password",password).update();
        if(update){
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);    // 删除key
        }
        return null;
    }

    // 判断验证码
    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if(code == null) return "请先获取验证码";
        if(!code.equals(vo.getCode())) return "验证码错误，请重新输入";
        return null;
    }
}
