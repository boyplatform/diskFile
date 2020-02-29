package cn.Boy.DiskFile.common;




import cn.Boy.DiskFile.controller.CephInterfaceTestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration("CommonResourceHandler")
public class CommonResourceHandler implements WebMvcConfigurer {

    private final Log log = LogFactory.getLog(CommonResourceHandler.class);

    private String staticPathPattern="";
    public void setStaticPathPattern(String staticPathPattern){
        this.staticPathPattern=staticPathPattern;
    }

    private String staticSourceLoaction="";
    public void  setStaticSourceLocation(String staticSourceLoaction){
        this.staticSourceLoaction=staticSourceLoaction;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){

        log.trace(this.staticPathPattern+"|"+this.staticSourceLoaction);
        registry.addResourceHandler(this.staticPathPattern).addResourceLocations(this.staticSourceLoaction);


    }
}
