package com.cpms.config.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.cpms.security.RoleTypes;
import com.cpms.web.controllers.Security;

/**
 * Security config, which configures web application groups and permissions
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	/**
	 * Configures users sources
	 * 
	 * @param provider configured authentication providing DAO
	 * @param auth Spring configured builder
	 * @throws Exception
	 */
	@Autowired
	public void configAuthentication(
			@Qualifier("authenticationProvider") AuthenticationProvider provider,
			AuthenticationManagerBuilder auth)
			throws Exception {
		auth.inMemoryAuthentication()
			.withUser(Security.adminName)
			.password(Security.adminPassword)
			.roles(RoleTypes.MANAGER.toString());
		auth.authenticationProvider(provider);
	}
	
	/**
	 * @param userService service which provides user details
	 * @param passwordEncoder to encode user passwords
	 * @return configured authentication providing DAO
	 */
	@Autowired
	@Bean(name = "authenticationProvider")
	public AuthenticationProvider getAuthenticationProvider(
			@Qualifier("userDetailsService") UserDetailsService userService,
			@Qualifier("passwordEncoder") BCryptPasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}
	
	@Override
    public void configure(WebSecurity web) throws Exception {
      web
        .ignoring()
           .antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
      .csrf()
      		.and()
      .authorizeRequests()
          	.antMatchers("/").permitAll()
          	.antMatchers("/viewer","/viewer/**")
          		.permitAll()
             .antMatchers("/editor/skill", "/editor/skill/alternative",
              			"/editor/skill/delete", "/editor/skill/delete/force",
              			"/editor/profile",
              			"/editor/*/skillLevel", "/editor/skill/alternativeAsync")
              	.hasAnyRole(RoleTypes.EXPERT.toString(), RoleTypes.MANAGER.toString())
          	.antMatchers("/editor","/editor/**")
          		.hasRole(RoleTypes.MANAGER.toString())
          	.antMatchers("/security/me")
          		.authenticated()
          	.antMatchers("/security/register")
          		.hasRole(RoleTypes.MANAGER.toString())
          	.antMatchers("/applications/delete", "/applications/delete/**", 
          			"/applications/approve", "/applications/approve/**")
          		.hasRole(RoleTypes.MANAGER.toString())
          	.antMatchers("/applications/suggest", "/applications/suggest/**", 
              		"/applications/recall", "/applications/recall/**")
              	.hasRole(RoleTypes.EXPERT.toString())
          	.and()
      .formLogin()
          	.loginPage("/security/login")
          			.usernameParameter("username")
          			.passwordParameter("password")
          	.loginProcessingUrl("/security/login")
          	//.failureUrl("/security/login?error")
          	/*.failureHandler(new AuthenticationFailureHandler() {
				
				@Override
				public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
						AuthenticationException exception) throws IOException, ServletException {
					// TODO Auto-generated method stub
					throw exception;
				}
			})*/
          	.permitAll()
          	.defaultSuccessUrl("/", true)
          	.and()
      .logout()
      		.clearAuthentication(true)
      		.logoutUrl("/security/logout")
      		.permitAll()
      		.logoutSuccessUrl("/")
      		.invalidateHttpSession(true);
      
      CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
      encodingFilter.setEncoding("UTF-8");
      encodingFilter.setForceEncoding(true);

      http.addFilterBefore(encodingFilter,CsrfFilter.class);
    }

	@Bean(name = "passwordEncoder")
	public BCryptPasswordEncoder passwordEncoder(){
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}

}
