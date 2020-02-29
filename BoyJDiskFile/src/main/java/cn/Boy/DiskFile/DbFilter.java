package cn.Boy.DiskFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class DbFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(DbFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String customerDbGuid = request.getHeader("customerDbGuid");
        if(customerDbGuid == null){
            logger.warn("请求没有传递customerDbGuid参数");
        }else{
            logger.trace("本次请求customerDbGuid: {}", customerDbGuid);
            ThreadLocalContext.setCustomerDbGuid(customerDbGuid);
        }

        chain.doFilter(request, response);
    }
}
