package org.fineract.acknowledgement.template;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TemplateConfig {

    @Value("${velocity.success}")
    private String successKeyword;

    static VelocityEngine vc = new VelocityEngine();


    static {
        /* Initialisation of velocity */
        vc.setProperty(RuntimeConstants.RESOURCE_LOADER, "class,file");
        vc.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        vc.setProperty("runtime.log.logsystem.log4j.logger", "VELLOGGER");
        vc.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        vc.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        try {
            vc.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Bean
    public Template getMessageTemplate() throws Exception {
        return vc.getTemplate("/failure.vm");
    }

    @Bean
    public VelocityContext getVelocityContext(){
        return new VelocityContext();
    }



}
