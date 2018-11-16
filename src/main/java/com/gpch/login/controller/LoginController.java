package com.gpch.login.controller;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.gpch.login.model.User;
import com.gpch.login.service.EmailService;
import com.gpch.login.service.UserService;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    
    @RequestMapping(value={"/", "/login"}, method = RequestMethod.GET)
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }
    
    @RequestMapping(value={"/forgotpassword"}, method = RequestMethod.GET)
    public ModelAndView forgotpassword(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("forgotpassword");
        return modelAndView;
    }
    
    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public ModelAndView resetPassword(@Valid User user, BindingResult bindingResult,HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserByEmail(user.getEmail());
        if (userExists == null) {
            bindingResult
                    .rejectValue("email", "error.user",
                            "User not found with the email provided");
        }else {
        	userExists.setResetToken(UUID.randomUUID().toString());
        	userService.saveUser(userExists);
        	String appUrl = request.getScheme() + "://" + request.getServerName();
			
			// Email message
			SimpleMailMessage passwordResetEmail = new SimpleMailMessage();
			passwordResetEmail.setFrom("support@demo.com");
			passwordResetEmail.setTo(user.getEmail());
			passwordResetEmail.setSubject("Password Reset Request");
			passwordResetEmail.setText("To reset your password, click the link below:\n" + appUrl
					+ "/reset?token=" +userExists.getResetToken());
			
			emailService.sendEmail(passwordResetEmail);

			// Add success message to view
			modelAndView.addObject("successMessage", "A password reset link has been sent to " + user.getEmail());
	
        }
        modelAndView.setViewName("forgotpassword");
        return modelAndView;
    }


    @RequestMapping(value="/registration", method = RequestMethod.GET)
    public ModelAndView registration(){
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserByEmail(user.getEmail());
        if (userExists != null) {
            bindingResult
                    .rejectValue("email", "error.user",
                            "There is already a user registered with the email provided");
        }
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration");
        } else {
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "User has been registered successfully");
            modelAndView.addObject("user", new User());
            modelAndView.setViewName("registration");

        }
        return modelAndView;
    }

    @RequestMapping(value="/admin/home", method = RequestMethod.GET)
    public ModelAndView home(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("userName", "Welcome " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
        modelAndView.addObject("adminMessage","Content Available Only for Users with Admin Role");
        modelAndView.setViewName("admin/home");
        return modelAndView;
    }
    
    @ModelAttribute("roleList")
    public Map<String,String> getRoleList(){
    	Map<String,String> roles=userService.getRoleDropDown();
    	return roles;
    }
    
   


}
