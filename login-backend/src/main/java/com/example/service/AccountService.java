package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import jakarta.validation.constraints.Email;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {     // 集成UserDetailsService，实现security登录验证
    Account findAccountByNameOrEmail(String username);
    // 注册邮件验证码
    String registerEmailVerifyCode(String type, String email, String ip);
    // 注册邮件账户
    String registerEmailAccount(EmailRegisterVO vo);
    // 重置密码确认
    String resetConfirm(ConfirmResetVO vo);
    String resetEmailAccountPassword(EmailResetVO vo);

}
