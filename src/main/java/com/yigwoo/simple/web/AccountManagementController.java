package com.yigwoo.simple.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Maps;
import com.yigwoo.simple.domain.Account;
import com.yigwoo.simple.service.AccountService;
import com.yigwoo.simple.service.ShiroDbRealm.ShiroUser;

/**
 * Administration Controller. 'superuser' could see all users(include
 * 'superuser', 'admin', 'user') list add/remove 'admin'/'user' account. 'admin'
 * could see all 'user' users and himself
 * 
 * @author YigWoo
 * 
 */
@Controller
@RequestMapping(value = "/manage")
public class AccountManagementController {
	public static final String COMMON_USER = "common user";
	public static final String SUPER_ADMIN = "super admin";
	public static final String ADMIN = "admin";
	public static final String UNAUTHORIZED = "errors/unauthorized";
	public static final String MANAGE_USERS_LIST = "manage/usersList";
	public static final String MANAGE_CREATE_USER = "manage/createUser";
	public static final String MANAGE_EDIT_USER = "manage/editUser";

	private static final Map<String, String> userTypes = Maps
			.newLinkedHashMap();
	static {
		userTypes.put("common user", COMMON_USER);
		userTypes.put("admin", ADMIN);
	}

	private Logger logger = LoggerFactory
			.getLogger(AccountManagementController.class);

	@Autowired
	AccountService accountService;

	@RequiresRoles("admin")
	@RequestMapping(value = "users", method = RequestMethod.GET)
	public String getAllUsersList(
			@RequestParam(value = "page", defaultValue = "1") int pageNumber,
			@RequestParam(value = "page.size", defaultValue = "5") int pageSize,
			@RequestParam(value = "sortColumn", defaultValue = "id") String sortColumn,
			@RequestParam(value = "sortDirection", defaultValue = "ASC") String sortDirection,
			Model model) {
		List<Account> accounts= accountService.getAllAccounts();
		List<ShiroUser> users = buildShiroUsersFromAccounts(accounts);
		logUsers(users);
		model.addAttribute("users", users);
		//model.addAttribute("sortColumn", sortColumn);
		//model.addAttribute("sortDirection", sortDirection);
		return MANAGE_USERS_LIST;
	}

	private void logUsers(List<ShiroUser> users) {
		for (int i = 0; i < users.size(); i++)
			logger.debug("ShiroUser: {}", users.get(i).getUsername());
	}

	@RequiresRoles("super admin")
	@RequestMapping(value = "users/create", method = RequestMethod.GET)
	public String createUserForm(Model model) {
		model.addAttribute("userTypes", userTypes);
		return MANAGE_CREATE_USER;
	}

	@RequiresRoles("super admin")
	@RequestMapping(value = "users/create", method = RequestMethod.POST)
	public String createUser(@Valid Account account,
			RedirectAttributes redirectAttributes) {
		logger.info("New Admin {} created", account.getUsername());
		accountService.createAdminAccount(account);
		redirectAttributes.addFlashAttribute("message",
				"Successfully create a new admin");
		return "redirect:/manage/users";
	}

	@RequiresRoles("super admin")
	@RequestMapping(value = "users/edit/{id}", method = RequestMethod.GET)
	public String editUser(@PathVariable("id") int id, Model model) {
		model.addAttribute("account", accountService.getAccountById(id));
		return MANAGE_EDIT_USER;
	}

	@RequiresRoles("super admin")
	@RequestMapping(value = "users/edit", method = RequestMethod.POST)
	public String editUser(@Valid @ModelAttribute("account") Account accountModel,
			RedirectAttributes redirectAttributes) {
		accountService.updateAccount(accountModel);
		redirectAttributes.addFlashAttribute("message",
				"User information updated.");
		return "redirect:/manage/users";
	}

	@RequiresRoles("super admin")
	@RequestMapping(value = "users/delete/{id}")
	public String deleteUser(@PathVariable("id") int id,
			RedirectAttributes redirectAttributes) {
		accountService.deleteAccount(id);
		String message = "Successfully delete a user";
		redirectAttributes.addFlashAttribute("message", message);
		return "redirect:/manage/users";
	}

	@RequestMapping(value = "checkEmail/{id}")
	@ResponseBody
	public String isEmailAvailable(@RequestParam("email") String email,
			@PathVariable("id") int id) {
		logger.debug("{} {}", email, id);
		Account account = accountService.getAccountByEmail(email);
		if (account == null || account.getId() == id) {
			return "true";
		} else {
			return "false";
		}
	}
	
	private List<ShiroUser> buildShiroUsersFromAccounts(List<Account> accounts) {
		List<ShiroUser> shiroUsers = new ArrayList<ShiroUser>();
		for (Account account : accounts) {
			List<String> roles =   accountService.extractStringRoleList(account.getRoles());
			ShiroUser user = new ShiroUser(account.getId(), account.getUsername(), 
					account.getEmail(), account.getBirthday(), account.getAge(),
					roles, account.getRegisterDate());
			shiroUsers.add(user);
		}
		return shiroUsers;
	}
}
