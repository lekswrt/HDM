package com.bluu.hdm.rest;

import com.bluu.hdm.rest.dao.interfaces.administration.IRoleDAO;
import com.bluu.hdm.rest.dao.interfaces.administration.IUserDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RestApplicationTests {

    @Test
    public void contextLoads() {
//	RoleVO roleRequest = new RoleVO();
//	roleRequest.setName("ADMIN");
//	roleService.save(mapper.convertValue(roleRequest, RoleEntity.class));
//
//	roleRequest = mapper.convertValue(roleService.findById(Long.parseLong("1")).get(), RoleVO.class);
//
//	String password = "admin";
//	String encrypted = Utils.encodePassword(password);
//	UserVO userRequest = new UserVO();
//
//	userRequest.setCreationDate(new Date(2018, 4, 24));
//	userRequest.setEmail("admin@admin.cl");
//	userRequest.setFirstname("Administrator");
//	userRequest.setLastname("System HDM");
//	userRequest.setUsername("admin");
//	userRequest.setPassword(encrypted);
//	userRequest.setIdRole(roleRequest);
//	userService.save(mapper.convertValue(userRequest, UserEntity.class));

    }

}
