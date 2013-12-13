package com.yigwoo.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.yigwoo.entity.Account;

/**
 * AccountDao is a standard Data Acess Object interface which
 * takes advantage of Spring Data JPA framework. The implementation
 * file is auto generated by Spring Data JPA framework.
 * For more infos, consult the reference manual of Spring Data JPA
 * @author YigWoo
 *
 */

public interface AccountDao extends Repository<Account, Long> {
	public Account save(Account account);
	public void delete(Account account);
	public void delete(Long id);
	public Account findOne(Long id);
	public List<Account> findAll();
	public Account findByUsername(String username);
	public Account findByEmail(String email);
}
