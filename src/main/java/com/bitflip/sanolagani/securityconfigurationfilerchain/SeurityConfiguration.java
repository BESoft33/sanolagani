package com.bitflip.sanolagani.securityconfigurationfilerchain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Bean;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import com.bitflip.sanolagani.serviceimpl.CustomUserDetailService;

@Configuration
@EnableWebSecurity
public class SeurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	CustomUserDetailService customuserdetailservice;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .antMatchers("/login", "/register", "/otpverify", "/companyregister",
                                "/", "/raisecapital", "/companyverify",
                                "/resetpassword", "/updatepassword", "/forgotpassword",
                                "/changepassword", "/reset/reports", "/signup/admin",
                                "/saveadmin")
                        .permitAll()
                        .antMatchers("/admin/**").hasRole("ADMIN")
                        .antMatchers("/user/**").hasRole("USER")        
                        .antMatchers("/company/**").hasRole("COMPANY")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .loginProcessingUrl("/processlogin")
                        .permitAll())
                
                .logout(logout -> logout
                        .logoutSuccessUrl("/logout"))
                .exceptionHandling(withDefaults());

	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(customuserdetailservice)
				.passwordEncoder(passwordEncoder());

	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
				.antMatchers("/resources/**", "/static/**", "/css/**", "/images/**", "/documents/**",
						"classpath:/static/documents/**");
	}

}
