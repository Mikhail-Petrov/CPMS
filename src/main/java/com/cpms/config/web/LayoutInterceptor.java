package com.cpms.config.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Custom interceptor which places view name in model map with key 
 * viewTemplateParameterName and instead sets view name to be layoutTemplate.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
public class LayoutInterceptor extends HandlerInterceptorAdapter {
	
	private String layoutTemplate, viewTemplateParameterName;
	
	public LayoutInterceptor(String layoutTemplate, 
			String viewTemplateParameterName) {
		this.layoutTemplate = layoutTemplate;
		this.viewTemplateParameterName = viewTemplateParameterName;
	}
	
	@Override
	public void postHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView)
     throws Exception {
		if (modelAndView != null &&
				!modelAndView.getViewName().startsWith("redirect:") &&
				!modelAndView.getViewName().startsWith("forward:") &&
				response.getStatus() != 404 &&
				!"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			String view = modelAndView.getViewName();
			modelAndView.setViewName(layoutTemplate);
			modelAndView.addObject(viewTemplateParameterName, view);
		}
	}

}
