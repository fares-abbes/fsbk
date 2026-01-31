package com.mss.backOffice.services;

import com.mss.unified.entities.*;
import com.mss.unified.repositories.UserPermissionRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.VueActionBkpRepo;
import com.mss.unified.repositories.VueActionRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetPermissionPerUser {

	@Autowired
	UserRepository userRepository;
	@Autowired
	UserPermissionRepository userPermissionRepository;
	@Autowired
	VueActionBkpRepo vueActionRepository;

	public Map<String, Integer> getPermissions(String userName) {
		User user = userRepository.findByUserName(userName).get();
		List<UserPermission> userPermissions = new ArrayList<>();
		Map<String, Integer> permiss = new HashMap<>();
		Set<Role> roleSet = user.getRoles();
		for (Role r : roleSet) {
			userPermissions.addAll(userPermissionRepository.findByUserPermissionId_RoleId((int) (long) r.getId()));

		}
		try {
		for (UserPermission userPermission : userPermissions) {
			VueActionBkp vueAction = vueActionRepository.findByUrl(userPermission.getUserPermissionId().getVueID());
			if (vueAction == null) {
//				System.out.println("-------------------------------------");
//				System.out.println(userPermission.getUserPermissionId().getVueID());
//				System.out.println("-------------------------------------");

				continue;
			}
				permiss.put(vueAction.getLibelle(), userPermission.getValue());

		}	} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}


		return permiss;
	}

}
