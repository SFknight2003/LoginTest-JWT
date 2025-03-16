package com.example.filter;

import com.example.utils.Const;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Const.ORDER_CORS)
public class CorsFilters extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
        this.addCorsHeader(request, response);   // 跨域相应
        chain.doFilter(request,response);
    }
    // 给所有请求添加请求头内容
    private  void addCorsHeader(HttpServletRequest request, HttpServletResponse response){
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));     // 允许跨域访问的地址
        response.addHeader("Access-Control-Allow-Methods","GET,HEAD,POST,PUT,DELETE,OPTIONS,TRACE,PATCH");  // 放行方法
        response.addHeader("Access-Control-Allow-Headers","Authorization, Content-Type");   // 允许类型
    }
}
