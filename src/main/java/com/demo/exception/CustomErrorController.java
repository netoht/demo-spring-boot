package com.demo.exception;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Mais informações sobre tratamento de erros:
 * http://docs.spring.io/spring-boot/docs/1.1.5.RELEASE/reference/htmlsingle/#boot-features-error-handling
 *
 * Podemos tambem nao configurar o erro: @EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
 * @author neto
 */
@RestController
public class CustomErrorController implements ErrorController {

    @Value("${error.path:/error}")
    private String errorPath;
    private final ErrorAttributes errorAttributes;

    public CustomErrorController() {
        this(new DefaultErrorAttributes());
    }

    public CustomErrorController(ErrorAttributes errorAttributes) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    public String getErrorPath() {
        return this.errorPath;
    }

//    @RequestMapping(value = { "${error.path:/error}" }, produces = { "text/html" })
//    public ModelAndView errorHtml(HttpServletRequest request) {
//        return new ModelAndView("error", getErrorAttributes(request, false));
//    }

    @RequestMapping({ "${error.path:/error}" })
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
        HttpStatus status = getStatus(request);
        return new ResponseEntity<Map<String, Object>>(body, status);
    }

    private boolean getTraceParameter(HttpServletRequest request) {
        String parameter = request.getParameter("trace");
        if (parameter == null) {
            return false;
        }
        return (!("false".equals(parameter.toLowerCase())));
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return this.errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        if (statusCode != null) {
            try {
                return HttpStatus.valueOf(statusCode.intValue());
            } catch (Exception ex) {
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}