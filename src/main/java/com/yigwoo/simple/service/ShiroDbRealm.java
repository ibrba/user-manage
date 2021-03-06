package com.yigwoo.simple.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yigwoo.simple.domain.Account;
import com.yigwoo.simple.domain.Role;
import com.yigwoo.simple.util.Encodes;

public class ShiroDbRealm extends AuthorizingRealm {
	public static class ShiroUser implements Serializable {
		private static final long serialVersionUID = -7499871275405557055L;
		private int id;
		private String username;
		private String email;
		private Date birthday;
		private int age;
		private List<String> roles;
		private Date registerDate;

		public ShiroUser(int id, String username, String email, Date birthday, int age,
				List<String> roles, Date registerDate) {
			this.id = id;
			this.username = username;
			this.email = email;
			this.birthday = birthday;
			this.age = age;
			this.roles = roles;
			this.registerDate = registerDate;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ShiroUser other = (ShiroUser) obj;
			if (username == null) {
				if (other.username != null) {
					return false;
				}
			} else if (!username.equals(other.username)) {
				return false;
			}
			return true;
		}

		public String getEmail() {
			return email;
		}
		
		public void setEmail(String email) {
			this.email = email;
		}

		public int getId() {
			return id;
		}

		public Date getRegisterDate() {
			return registerDate;
		}

		public List<String> getRoles() {
			return roles;
		}

		public String getUsername() {
			return username;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(username);
		}

		@Override
		public String toString() {
			return username;
		}

		public Date getBirthday() {
			return birthday;
		}

		public void setBirthday(Date birthday) {
			this.birthday = birthday;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}
	
	static Logger logger = LoggerFactory.getLogger(ShiroDbRealm.class);

	protected AccountService accountService;
	
	/**
	 * Authentication bussiness method
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken authToken) throws AuthenticationException {
		UsernamePasswordToken token = (UsernamePasswordToken) authToken;
		Account account = accountService.getAccountByUsername(token
				.getUsername());
		if (account != null) {
			logger.debug("{} has {} roles", account.getUsername(), account.getRoles().size());
			List<String> roleList = accountService.extractStringRoleList(account.getRoles());
			byte[] salt = Encodes.decodeHex(account.getSalt());
			SimpleAuthenticationInfo authcInfo = new SimpleAuthenticationInfo(new ShiroUser(account.getId(),
					account.getUsername(), account.getEmail(), account.getBirthday(), account.getAge(), roleList,
					account.getRegisterDate()), account.getPassword(),
					ByteSource.Util.bytes(salt), getName());
			return authcInfo;
		} else {
			return null;
		}
	}
	
	/**
	 * Do authorization bussiness
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
		ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
		List<Role> roles = accountService.getAccountByUsername(shiroUser.getUsername()).getRoles();
		List<String> roleList = accountService.extractStringRoleList(roles);
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		logger.debug("Current User roles {}", roles.toString());
		info.addRoles(roleList);
		return info;
	}

	@PostConstruct
	public void initCredentialsMatcher() {
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(
				AccountService.HASH_ALGORITHM);
		matcher.setHashIterations(AccountService.HASH_ITERATION_COUNT);
		setCredentialsMatcher(matcher);
	}

	@Autowired
	public void setAccountService(AccountService accountService) {
		this.accountService = accountService;
	}
}
