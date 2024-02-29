package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登陆
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路径匹配器 通配符 ** - index.html
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 检查路径是否匹配 需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 0.
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1.获得URI,定义放行路径
        String requestURI = request.getRequestURI();
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",  // 静态资源
                "/front/**"
        };
        // 2.检查是否放行
        boolean check = check(urls, requestURI);
        // 3.如果不需要处理，直接放行
        if (check) {
            log.info("不需要处理 {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        // 4.需要处理:检查登陆状态，已登陆则直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登陆 {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        // 5.未登陆 - 不是跳转到登陆：未登陆 则返回未登陆的结果，通过输出流方式向客户端页面响应数据
        log.info("用户未登陆 {}", requestURI);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }
}
