package com.yigwoo.web;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yigwoo.entity.User;
import com.yigwoo.service.AccountService;

/**
 * The controller which controls the registeration
 * procedure of a new user.
 * @author YigWoo
 *
 */

@Controller
@RequestMapping(value = "/register")
public class RegisterController {
	@Autowired
	private AccountService accountService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String registerForm() {
		return "register";
	}
	
	@RequestMapping(method = RequestMethod.POST) 
	public String register(@Valid User user,
			RedirectAttributes redirectAttributes) {
		accountService.registerUser(user);
		redirectAttributes.addFlashAttribute("username", user.getUsername());
		return "redirect:/login";
	}
}
