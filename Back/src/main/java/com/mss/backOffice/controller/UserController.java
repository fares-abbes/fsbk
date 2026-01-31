package com.mss.backOffice.controller;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.googlecode.mp4parser.h264.model.VUIParameters;
import com.mss.backOffice.message.request.LoginForm;
import com.mss.backOffice.message.request.SignUpForm;
import com.mss.backOffice.message.response.JwtResponse;
import com.mss.unified.entities.AgenceAdministration;
import com.mss.unified.entities.Bank;
import com.mss.unified.entities.Privilege;
import com.mss.unified.entities.Role;
import com.mss.unified.entities.User;
import com.mss.unified.entities.UserPermission;
import com.mss.unified.entities.UserPermissionId;
import com.mss.unified.entities.UserType;
import com.mss.unified.entities.VueAction;
import com.mss.unified.entities.VueActionBkp;
import com.mss.unified.repositories.AgenceAdministrationRepository;
import com.mss.unified.repositories.BankRepository;
import com.mss.unified.repositories.GroupeVueRepository;
import com.mss.unified.repositories.PrivilegeRepository;
import com.mss.unified.repositories.RoleRepository;
import com.mss.unified.repositories.UserPermissionRepository;
import com.mss.unified.repositories.UserRepository;
import com.mss.unified.repositories.UserTypeRepository;
import com.mss.unified.repositories.VueActionBkpRepo;
import com.mss.unified.repositories.VueActionRepository;
import com.mss.backOffice.request.AddAccess;
import com.mss.backOffice.request.AddAllVue;
import com.mss.backOffice.request.AddVue;
import com.mss.backOffice.request.Child;
import com.mss.backOffice.request.Child_;
import com.mss.backOffice.request.DispalyVue;
import com.mss.backOffice.request.FilterUsers;
import com.mss.backOffice.request.ForgetPasswordRequest;
import com.mss.backOffice.request.GetTokenAmplitudeResponse;
import com.mss.backOffice.request.Nav;
import com.mss.backOffice.request.UpdateFirstUser;
import com.mss.backOffice.request.UpdatePasswordUser;
import com.mss.backOffice.request.UpdateUser;
import com.mss.backOffice.request.UserDisplay;
import com.mss.backOffice.request.VuePrivilege;
import com.mss.backOffice.request.VueValuePer;
import com.mss.backOffice.security.jwt.JwtProvider;
import com.mss.backOffice.services.AccountService;
import com.mss.backOffice.services.CustomUserDetailsService;
import com.mss.backOffice.services.GetTokenAmplitudeService;
import com.mss.backOffice.services.MyEmailService;
import com.mss.backOffice.services.PasswordService;
import com.mss.backOffice.services.SetPermissionPerUser;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.security.PermitAll;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserRepository userRepository;
	@Autowired
	public PasswordEncoder passwordEncoder;

	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	JwtProvider jwtProvider;
	@Autowired
	PrivilegeRepository privilegeRepository;
	@Autowired
	MyEmailService myEmailService;
	@Autowired
	BankRepository bankRepository;
	@Autowired
	VueActionBkpRepo vueActionRepository;
	@Autowired
	public CustomUserDetailsService custom;
	@Autowired
	UserPermissionRepository userPermissionRepository;
	@Autowired
	SetPermissionPerUser setPermissionPerUser;
	@Autowired
	GroupeVueRepository groupeVueRepository;
	@Autowired
	PasswordService passwordService;
	@Autowired
	UserTypeRepository userTypeRepo;

	@Autowired
	VueActionBkpRepo vueActionBkpRepo;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	private ModelMapper modelMapper = new ModelMapper();
	private static final Gson gson = new Gson();
	@Value("${jwt.header}")
	private String tokenHeader;
	@Autowired
	private AgenceAdministrationRepository agenceAdministrationRepository;

	@PostMapping(value = "/signin", consumes = "application/json")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {

//    logger.info("Authentification User: " + loginRequest.getUserName() + " " + passwordEncoder
//   
		if (!userRepository.findByUserName(loginRequest.getUserName()).isPresent()) {
			return ResponseEntity.badRequest().body(gson.toJson("WRONG CREDENTIALS"));
		}
		User user = userRepository.findByUserName(loginRequest.getUserName()).get();

		if (user.getFirstUser() && user.getPasswordUpdated() == 0) {
			return ResponseEntity.badRequest().body(gson.toJson("FIRST USER MUST CHANGE EMAIL AND PASSWORD"));
		}

		if (user.getAttempt() == 3) {
			user.setActivated(false);
			userRepository.save(user);
			return ResponseEntity.badRequest().body(gson.toJson("USER BLOCKED"));

		}

		if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
			int attempts = user.getAttempt();
			user.setAttempt(attempts + 1);
			userRepository.save(user);
			return ResponseEntity.badRequest().body(gson.toJson("WRONG CREDENTIALS"));

		}

		// User user = userRepository.findByUserName(loginRequest.getUserName()).get();
		if (!user.getActivated() && user.getLastLogin() != null) {
			return ResponseEntity.badRequest().body(gson.toJson("USER BLOCKED"));

		}
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String testFirstLogin = "false";
		if (user.getPasswordUpdated() != null) {
			if (user.getPasswordUpdated() == 0)
				testFirstLogin = "true";
		}

		if (user.getCreatedAt() != null && testFirstLogin.equals("true")) {
			long difference_In_Time = new Date().getTime() - user.getCreatedAt().getTime();
			long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
			if (difference_In_Minutes > 15) {
				user.setTimeElapsed(1);
				userRepository.save(user);
				return ResponseEntity.badRequest().body(gson.toJson("TIME ELAPSED"));
			}

		}

		String jwt = jwtProvider.generateToken(authentication);

		// User user = userRepository.findByUserName(loginRequest.getUserName()).get();
		// String jwt = jwtProvider.createToken(user.getUserName(), user.getRoles());
		if (user.getLastLogin() == null) {
			user.setStatus(true);
		}

		List<String> authorities = new ArrayList<>();
		for (Role role : user.getRoles()) {
			authorities.add(role.getName().toString());
		}
		user.setLastLogin(user.getNewLogin());
		user.setNewLogin(new Date());
		user.setActivated(true);
		user.setAttempt(0);
		userRepository.save(user);
		return ResponseEntity.ok(new JwtResponse(testFirstLogin, jwt, user.getUserName(), authorities,
				jwtProvider.getExpirationDateFromToken(jwt), setPermissionPerUser.getPermissions(user.getUserName()),
				user.getUserCode()));

	}

	@PostMapping(value = "/signup")
	// @PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {
		// logger.info(signUpRequest.toString());

		if (userRepository.existsByUserName(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(gson.toJson("Username is already taken!"));

		}

		if (userRepository.existsByUserEmail(signUpRequest.getEmail().toLowerCase())) {
			return ResponseEntity.badRequest().body(gson.toJson("Email is already in use!"));

		}

		// Creating user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail().toLowerCase());
		// String shortId = RandomString.make(12);
		String password = passwordService.generatePassayPassword();
		user.setPassword(passwordEncoder.encode(password));
		user.setFirstName(signUpRequest.getFirstName());
		user.setLastName(signUpRequest.getLasName());
		user.setBankId(signUpRequest.getBank());
		user.setPasswordUpdated(0);
		user.setCreatedAt(new Date());
		user.setUserType(signUpRequest.getType());
		user.setIdAgence(signUpRequest.getIdAgence());
		user.setFirstUser(false);
		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		for (String s : strRoles) {
			Role r = roleRepository.findById(Long.parseLong(s)).get();
			roles.add(r);
		}

		user.setRoles(roles);
		try {
			myEmailService.sendOtpMessage(user.getUserEmail(), "MONETIQUE MS-SPARK",
					"Bonjour,\r\n\r\n"
					+ "Nous vous informons que  votre compte utilisateur MS-SPARK a été créé;\r\n\r\n"
					+ "Nom Utilisateur : "+user.getUserName()+"\r\n\r\n"
					+ "Mot de passe    : "+ password+"\r\n\r\n"
					+"Mot de passe à modifier après la première connexion\r\n\r\n"
					+ "Cordialement"

//					+ " you can find below your credentials for the portail , userName :" + user.getUserName()
//							+ " and your password is : " + password
							
					
					);
		} catch (Exception e) {
			String stackTrace = Throwables.getStackTraceAsString(e);

			logger.info(stackTrace);
			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}
		userRepository.save(user);
		// logger.info(user.toString());

		return ResponseEntity.accepted().body(gson.toJson("User registered successfully!"));
	}

	@PostMapping(value = "/registerFirstUser")
	// @PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> registerFirstUser(@Valid @RequestBody SignUpForm signUpRequest) {
		// logger.info(signUpRequest.toString());
		// System.out.println(signUpRequest.toString());
//	  
		if ((userRepository.findAll()).size() > 0) {
			return ResponseEntity.badRequest().body(gson.toJson("Not allowed!"));
		}

//	  if ((userRepository.findAll()).size()>0) {
//		  return ResponseEntity.badRequest().body(gson.toJson("Not allowed!"));
//	  }

		// Creating user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail().toLowerCase());
		String password = passwordService.generatePassayPassword();
		user.setPassword(passwordEncoder.encode(password));
		user.setFirstName(signUpRequest.getFirstName());
		user.setLastName(signUpRequest.getLasName());
		user.setBankId(signUpRequest.getBank());
		user.setPasswordUpdated(0);
		user.setCreatedAt(new Date());
		user.setUserType(signUpRequest.getType());
		user.setIdAgence(signUpRequest.getIdAgence());
		user.setFirstUser(true);

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		for (String s : strRoles) {
			Role r = roleRepository.findById(Long.parseLong(s)).get();
			roles.add(r);
		}

		user.setRoles(roles);

		userRepository.save(user);
		// logger.info(user.toString());

		return ResponseEntity.accepted().body(gson.toJson("User registered successfully!"));
	}

	@GetMapping("/getUser/{id}")
	public ResponseEntity<User> getEmployeeById(@PathVariable(value = "id") String userId) {
		User user = userRepository.findByUserCode(Integer.parseInt(userId));
		logger.info(user.toString());
		return ResponseEntity.ok().body(user);
	}

	@GetMapping("/getPermission/{idRole}")
	public ResponseEntity<?> getPermissionByRole(@PathVariable(value = "idRole") String idRole) {
		List<UserPermission> userPermissions = new ArrayList<>(
				userPermissionRepository.findByUserPermissionId_RoleId(Integer.parseInt(idRole)));
		List<VueValuePer> permiss = new ArrayList<>();
		for (UserPermission userPermission : userPermissions) {
			VueActionBkp vueAction = vueActionRepository.findByUrl(userPermission.getUserPermissionId().getVueID());
			VueValuePer vueValuePer = new VueValuePer(vueAction.getUrl(), vueAction.getLibelle(),
					userPermission.getValue(), vueAction.getGroupe());
			permiss.add(vueValuePer);
		}
		if (permiss.isEmpty()) {
			return ResponseEntity.badRequest().body(gson.toJson("NO PERMISSION"));

		}
		permiss = permiss.stream().sorted(Comparator.comparing(VueValuePer::getGroupe)).collect(Collectors.toList());
		logger.info(permiss.toString());
		return ResponseEntity.ok().body(permiss);
	}

	@GetMapping("/getPermission")
	public ResponseEntity<?> getPermission() {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		// System.out.println(" nammmmmeee " + name);
		// String id = userPrincipal.getId();
		if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
			return null;

		}
		User user = userRepository.findByUserNameOrUserEmail(name, name).get();
		Map<String, Integer> permiss = setPermissionPerUser.getPermissions(user.getUserName());
		if (permiss.isEmpty()) {
			return ResponseEntity.badRequest().body(gson.toJson("NO PERMISSION"));

		}
		logger.info(permiss.toString());
		return ResponseEntity.ok().body(permiss);
	}

	@PutMapping("/reloadDate/{id}")
	public ResponseEntity<User> ReloadEmployee(@PathVariable(value = "id") String userId) {
		User user = userRepository.findByUserCode(Integer.parseInt(userId));
		user.setCreatedAt(new Date());
		user.setTimeElapsed(0);
		userRepository.save(user);
		return ResponseEntity.ok(user);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<User> updateEmployee(@PathVariable(value = "id") String userId,
			@Valid @RequestBody UpdateUser userDetails) {
		logger.info(userDetails.toString());
		if (userDetails.getType()!=null) {
			User employee = userRepository.findByUserCode(Integer.parseInt(userId));

			employee.setLastName(userDetails.getLastName());
			employee.setFirstName(userDetails.getFirstName());
			if (!Strings.isNullOrEmpty(userDetails.getEmail())) {
				employee.setUserEmail(userDetails.getEmail());
			}
			Set<Role> roles = new HashSet<>();

			if (!userDetails.getRole().isEmpty()) {
				for (String s : userDetails.getRole()) {
					Role r = roleRepository.findById(Long.parseLong(s)).get();
					roles.add(r);
				}
			}
			employee.setUserType(userDetails.getType());
			employee.setIdAgence(userDetails.getIdAgence());
			employee.setRoles(roles);
			// employee.setUserType(userDetails.getType());
			final User updatedEmployee = userRepository.save(employee);
			logger.info(updatedEmployee.toString());
			return ResponseEntity.ok(updatedEmployee);
		}else {
			return ResponseEntity.badRequest().body(null);

		}
		
		
	}

	@GetMapping("getAllUsers")
	public List<UserDisplay> getAllUsers() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		// System.out.println(" nammmmmeee " + name);
		// String id = userPrincipal.getId();
		if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
			return null;

		}
		User user = userRepository.findByUserNameOrUserEmail(name, name).get();
		List<User> users = userRepository.findAll();
		List<UserDisplay> userDisplays = new ArrayList<>();
		for (User u : users) {

			if (!user.getUserName().equals(u.getUserName())) {
				UserType userType = new UserType();

				if (userTypeRepo.findById(u.getUserType()).isPresent()) {
					userType = userTypeRepo.findById(u.getUserType()).get();
				}
				UserDisplay userDisplay = new UserDisplay(u.getUserCode(), u.getUserName(), u.getUserEmail(),
						u.getFirstName(), u.getLastName(), u.getAttempt(), u.getLastLogin(), u.getActivated(),
						u.getStatus(), u.getRoles(), u.getTimeElapsed(), u.getUserType(), u.getIdAgence());
				Bank bank = bankRepository.findById(u.getBankId()).get();

				userDisplay.setBankId(bank.getTagBank());
				userDisplays.add(userDisplay);
			}
		}

		/*
		 * userDisplays = userDisplays.stream()
		 * 
		 * .filter(e -> Strings.isNullOrEmpty(userFilter.getUsername()) || e
		 * .getUserName() .equals(userFilter.getUsername()))
		 * 
		 * .filter(e -> userFilter.getStatus()==null ||
		 * java.util.Objects.equals(e.getStatus(),userFilter.getStatus()))
		 * 
		 * 
		 * 
		 * 
		 * .collect(Collectors.toList());
		 */
		logger.info(userDisplays.toString());
		return userDisplays;
	}

	@PostMapping("getAllUsersFiltred")
	public List<UserDisplay> getAllUsersFiltred(@RequestBody FilterUsers filterUsers) {
		String userName = "";
		if (!filterUsers.getUserName().equals("")) {
			userName = userName + filterUsers.getUserName().trim();
		}
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		// System.out.println(" nammmmmeee " + name);
		// String id = userPrincipal.getId();
		if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
			return null;

		}
		User user = userRepository.findByUserNameOrUserEmail(name, name).get();

		List<User> users = new ArrayList<>();
		if (filterUsers.getRole() != 0) {
			Optional<Role> role = roleRepository.findById(filterUsers.getRole());
			users = userRepository.getByRoleAndUserNameFilter(userName, role.get());
		} else {
			users = userRepository.getByUserName(userName);
		}
		List<UserDisplay> userDisplays = new ArrayList<>();
		for (User u : users) {

			if (!user.getUserName().equals(u.getUserName())) {
				UserType userType = new UserType();

				if (userTypeRepo.findById(u.getUserType()).isPresent()) {
					userType = userTypeRepo.findById(u.getUserType()).get();
				}
				UserDisplay userDisplay = new UserDisplay(u.getUserCode(), u.getUserName(), u.getUserEmail(),
						u.getFirstName(), u.getLastName(), u.getAttempt(), u.getLastLogin(), u.getActivated(),
						u.getStatus(), u.getRoles(), u.getTimeElapsed(), u.getUserType(), u.getIdAgence());
				Bank bank = bankRepository.findById(u.getBankId()).get();

				userDisplay.setBankId(bank.getTagBank());
				userDisplays.add(userDisplay);
			}
		}

		/*
		 * userDisplays = userDisplays.stream()
		 * 
		 * .filter(e -> Strings.isNullOrEmpty(userFilter.getUsername()) || e
		 * .getUserName() .equals(userFilter.getUsername()))
		 * 
		 * .filter(e -> userFilter.getStatus()==null ||
		 * java.util.Objects.equals(e.getStatus(),userFilter.getStatus()))
		 * 
		 * 
		 * 
		 * 
		 * .collect(Collectors.toList());
		 */
		logger.info(userDisplays.toString());
		return userDisplays;
	}

	/*
	 * @PutMapping("changeRole/{id}") public ResponseEntity<User>
	 * changeRole(@RequestParam String role,
	 * 
	 * @PathVariable(value = "id") String userId) { User employee =
	 * userRepository.findByUserCode(Integer.parseInt(userId)); Set<Role> strRoles =
	 * employee.getRoles(); Set<Role> roles = new HashSet<>(); switch (role) { case
	 * "ADMIN": Role adminRole = roleRepository.findByName(RoleName.ADMIN)
	 * .orElseThrow(() -> new
	 * RuntimeException("Fail! -> Cause: User Role not find."));
	 * roles.add(adminRole);
	 * 
	 * break; case "AGENT": Role pmRole = roleRepository.findByName(RoleName.AGENT)
	 * .orElseThrow(() -> new
	 * RuntimeException("Fail! -> Cause: User Role not find.")); roles.add(pmRole);
	 * 
	 * break; default: Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
	 * .orElseThrow(() -> new
	 * RuntimeException("Fail! -> Cause: User Role not find."));
	 * roles.add(userRole); } employee.setRoles(roles);
	 * userRepository.save(employee); return ResponseEntity.ok(employee); }
	 */
	@GetMapping("roles")
	public List<Role> getAllRole() {

		logger.info(roleRepository.findAll().toString());

		return roleRepository.findAll();
	}

	@PutMapping("activate/{id}")
	public ResponseEntity<User> activateUser(@PathVariable(value = "id") String userId) {
		User employee = userRepository.findByUserCode(Integer.parseInt(userId));
		employee.setActivated(true);
		employee.setAttempt(0);

		userRepository.save(employee);
		logger.info(employee.toString());

		return ResponseEntity.ok(employee);
	}

	@PutMapping("blocked/{id}")
	public ResponseEntity<User> bloquedUser(@PathVariable(value = "id") String userId) {
		User employee = userRepository.findByUserCode(Integer.parseInt(userId));

		employee.setActivated(false);
		userRepository.save(employee);
		logger.info(employee.toString());

		return ResponseEntity.ok(employee);
	}

	@PostMapping("addUserType")
	public ResponseEntity<UserType> addUserType(@RequestBody UserType userType) {
		userTypeRepo.save(userType);
		return ResponseEntity.ok(userType);

	}

	@GetMapping("/getAllUsersType")
	public List<UserType> getUserType() {
		return userTypeRepo.findAll();
	}
	/*
	 * @PostMapping("addRole") public ResponseEntity<Role> addRole(@RequestBody
	 * AddRole addRole) { Set<Privilege> privileges = new HashSet<>(); Set<String>
	 * strPrivileges = addRole.getPrivilege();
	 * 
	 * strPrivileges.forEach(priv -> { Privilege privilege =
	 * privilegeRepository.findByName(priv); privileges.add(privilege);
	 * 
	 * });
	 * 
	 * Role role = new Role(); switch (addRole.getName()) { case "ADMIN":
	 * role.setName(RoleName.ADMIN); role.setPrivileges(privileges);
	 * 
	 * break; case "AGENT": role.setName(RoleName.AGENT);
	 * role.setPrivileges(privileges);
	 * 
	 * break; default: role.setName(RoleName.ROLE_USER);
	 * role.setPrivileges(privileges); }
	 * 
	 * roleRepository.save(role); return ResponseEntity.ok(role);
	 * 
	 * }
	 */

	@PostMapping("addPrivilege")
	public ResponseEntity<Privilege> addRole(@RequestBody Privilege privilege) {
		privilegeRepository.save(privilege);
		logger.info(privilege.toString());

		return ResponseEntity.ok(privilege);
	}

	/**
	 * @PostMapping("addGroupeVue") public ResponseEntity<GroupeVue>
	 * addGroupeVue(@RequestBody Privilege privilege) {
	 * privilegeRepository.save(privilege); return ResponseEntity.ok(privilege); }
	 */
	@GetMapping("privileges")
	public List<Privilege> privileges() {

		logger.info(privilegeRepository.findAll().toString());

		return privilegeRepository.findAll();
	}

	@GetMapping(value = "${jwt.route.authentication.refresh}")
	@PermitAll
	public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request, HttpServletResponse res) {

		String authToken = request.getHeader(tokenHeader);
		final String token = authToken.substring(7);
		String username = jwtProvider.getUsernameFromToken(token);
		User user = userRepository.findByUserNameOrUserEmail(username, username).get();
		if (!jwtProvider.canTokenBeRefreshed(token, user.getLastUserUpdate())) {
			return ResponseEntity.badRequest().body(gson.toJson("WRONG CREDENTIALS"));
		} else {
			String refreshedToken = jwtProvider.refreshToken(token);
			return ResponseEntity.ok(new JwtResponse(refreshedToken, user.getUserName(),
					jwtProvider.getExpirationDateFromToken(refreshedToken),
					setPermissionPerUser.getPermissions(user.getUserName())));
		}
	}

	@DeleteMapping("deleteUser/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable(value = "id") Integer type) {

		User bin = userRepository.findById(type).get();
		userRepository.delete(bin);

		return ResponseEntity.ok().body(gson.toJson("MCC deleted successfully!"));

	}

	@PostMapping("addAccess")

	public ResponseEntity<Role> addRole(@RequestBody AddAccess addAccess) {
		Role role = new Role();
		Set<VueAction> vueActions = new HashSet<>();
		if (roleRepository.findByName(addAccess.getRoleName()).isPresent()) {
			Role rolee = roleRepository.findByName(addAccess.getRoleName()).get();
			List<VuePrivilege> vuePrivileges = addAccess.getVueAction();
			Set<UserPermission> privileges = new HashSet<>();
			for (VuePrivilege i : vuePrivileges) {
				VueActionBkp vueAction = vueActionRepository.findByUrl(i.getIdVue());
				UserPermissionId userPermissionId = new UserPermissionId((int) (long) rolee.getId(),
						vueAction.getUrl());
				UserPermission userPermission = new UserPermission(userPermissionId, i.getPrivilege());
				userPermissionRepository.save(userPermission);
				privileges.add(userPermission);
				/*
				 * vueActions.add(vueAction); vueAction.setPrivileges(i.getPrivilege());
				 * vueActions.add(vueAction);
				 */
			}
			return ResponseEntity.ok(rolee);
		}
		role.setName(addAccess.getRoleName());
		Role role1 = roleRepository.save(role);
		List<VuePrivilege> vuePrivileges = addAccess.getVueAction();
		Set<UserPermission> privileges = new HashSet<>();
		for (VuePrivilege i : vuePrivileges) {
			VueActionBkp vueAction = vueActionRepository.findByUrl(i.getIdVue());
			UserPermissionId userPermissionId = new UserPermissionId((int) (long) role1.getId(), vueAction.getUrl());
			UserPermission userPermission = new UserPermission(userPermissionId, i.getPrivilege());
			userPermissionRepository.save(userPermission);
			privileges.add(userPermission);
		}

		return ResponseEntity.ok(role);
	}

	@PostMapping("addVue")
	public ResponseEntity<String> addVue(@RequestBody AddVue addAccess) {

		VueActionBkp vueAction = new VueActionBkp();
		vueAction.setLibelle(addAccess.getLibelle());

		VueActionBkp vueAction1 = vueActionRepository.save(vueAction);
		List<User> users = userRepository.findAll();
		for (User u : users) {
			for (Role r : u.getRoles()) {
				List<UserPermission> userPermissions = userPermissionRepository
						.findByUserPermissionId_RoleId((int) (long) r.getId());
				UserPermissionId userPermissionId = new UserPermissionId((int) (long) r.getId(), vueAction1.getUrl());
				VuePrivilege i = new VuePrivilege(vueAction1.getUrl(), 0);
				UserPermission userPermission = new UserPermission(userPermissionId, i.getPrivilege());

				userPermissionRepository.save(userPermission);
			}
		}
		return ResponseEntity.ok("added ");

	}

	@GetMapping("/getAllVue")
	public List<DispalyVue> getVue() {
		List<DispalyVue> dispalyVues = new ArrayList<>();
		for (VueActionBkp va : vueActionRepository.findAll().stream()
				.sorted(Comparator.comparing(VueActionBkp::getGroupe)).collect(Collectors.toList())) {
			DispalyVue dispalyVue = new DispalyVue(va.getUrl(), va.getLibelle(), va.getGroupe(), null);
			dispalyVues.add(dispalyVue);
		}
		return dispalyVues;
	}

	@GetMapping("/getUserConnect")
	public ResponseEntity<User> getUserConnect(HttpServletRequest request) {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		// String id = userPrincipal.getId();
		if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
			return ResponseEntity.ok(new User());

		}
		User user = userRepository.findByUserNameOrUserEmail(name, name).get();
		return ResponseEntity.ok(user);
	}

	@GetMapping("/getUserAgency")
	public AgenceAdministration getUserAgency() {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<User> user = userRepository.findByUserNameOrUserEmail(name, name);
		// String id = userPrincipal.getId();
		if (!user.isPresent())
			return null;

		if (user.get().getUserType() != 1)
			return null;
		Optional<AgenceAdministration> agency = agenceAdministrationRepository.findByIdAgence(user.get().getIdAgence());
		if (!agency.isPresent())
			return null;

		return agency.get();
	}

	@PutMapping("changePassword")
	public ResponseEntity<String> changePassword(@RequestBody UpdatePasswordUser updatePasswordUser) {

		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		// String id = userPrincipal.getId();
		if (!userRepository.findByUserNameOrUserEmail(name, name).isPresent()) {
			return ResponseEntity.badRequest().body(gson.toJson("USER NOT FOUND"));

		}
		User user = userRepository.findByUserNameOrUserEmail(name, name).get();
		if (!passwordEncoder.matches(updatePasswordUser.getOldPassword(), user.getPassword())) {
			return ResponseEntity.badRequest().body(gson.toJson("OLD PASSWORD IS WRONG"));

		}
		if (passwordEncoder.matches(updatePasswordUser.getNewPassword(), user.getPassword())) {
			return ResponseEntity.badRequest().body(gson.toJson("NEW PASSWORD CAN'T BE THE SAME AS THE PREVIOUS ONE"));

		}
		if (updatePasswordUser.getNewPassword().contains(user.getFirstName())
				|| updatePasswordUser.getNewPassword().contains(user.getLastName())
				|| updatePasswordUser.getNewPassword().contains(user.getUserName())
				|| updatePasswordUser.getNewPassword().contains(user.getUserEmail())) {
			return ResponseEntity.badRequest().body(gson.toJson("WRONG PATTERN"));

		}
		if (!passwordService.isPasswordValid(updatePasswordUser.getNewPassword())) {
			return ResponseEntity.badRequest().body(gson.toJson("PASSWORD MUST MEET COMPLEXITY REQUIREMENT."));
		}
		user.setPassword(passwordEncoder.encode(updatePasswordUser.getNewPassword()));
		user.setLastUserUpdate(new Date());
		user.setPasswordUpdated(1);
		userRepository.save(user);
		// logger.info(user.toString());

		return ResponseEntity.ok().body(gson.toJson("CHANGED"));
	}

	@PutMapping("changeEmailAndPasswordForFirstUser")
	public ResponseEntity<String> changeEmailAndPasswordForFirstUser(@RequestBody UpdateFirstUser updateFirstUser) {

//    String name = SecurityContextHolder.getContext().getAuthentication().getName();
//	  if ((userRepository.findAll()).size()>0) {
//		  return ResponseEntity.badRequest().body(gson.toJson("Not allowed!"));
//	  }

		User user = userRepository
				.findByUserNameOrUserEmail(updateFirstUser.getUserName(), updateFirstUser.getUserName()).get();

		if (passwordEncoder.matches(updateFirstUser.getNewPassword(), user.getPassword())) {
			return ResponseEntity.badRequest().body(gson.toJson("NEW PASSWORD CAN'T BE THE SAME AS THE PREVIOUS ONE"));
		}

		if (updateFirstUser.getNewPassword().contains(user.getFirstName())
				|| updateFirstUser.getNewPassword().contains(user.getLastName())
				|| updateFirstUser.getNewPassword().contains(user.getUserName())
				|| updateFirstUser.getNewPassword().contains(user.getUserEmail())) {
			return ResponseEntity.badRequest().body(gson.toJson("WRONG PATTERN"));

		}
		if (!passwordService.isPasswordValid(updateFirstUser.getNewPassword())) {
			return ResponseEntity.badRequest().body(gson.toJson("PASSWORD MUST MEET COMPLEXITY REQUIREMENT."));
		}
		user.setPassword(passwordEncoder.encode(updateFirstUser.getNewPassword()));
		user.setLastUserUpdate(new Date());
		user.setUserEmail(updateFirstUser.getEmail());
		user.setPasswordUpdated(1);
		try {
			myEmailService.sendOtpMessage(user.getUserEmail(), "First access",
					"Your email address and password for the " + user.getUserName()
							+ " user have been saved successfully.");
		} catch (MessagingException | UnsupportedEncodingException e) {

			return ResponseEntity.badRequest().body(gson.toJson("Email is not valid"));
		}

		userRepository.save(user);
		// logger.info(user.toString());

		return ResponseEntity.ok().body(gson.toJson("CHANGED"));
	}

	@PostMapping("forgetPassword")
	public ResponseEntity<String> forgetPassword(@RequestBody ForgetPasswordRequest addAccess) {
		logger.info(addAccess.toString());

		String shortId = RandomString.make(12);
		String password = passwordService.generatePassayPassword();

		if (!userRepository
				.findByUserNameOrUserEmail(addAccess.getUserName().toLowerCase(), addAccess.getUserName().toLowerCase())
				.isPresent()) {

			return ResponseEntity.badRequest().body("USER NOT FOUND");
		}

		User user = userRepository.findByUserNameOrUserEmail(addAccess.getUserName(), addAccess.getUserName()).get();
		user.setPassword(passwordEncoder.encode(password));
		user.setLastUserUpdate(new Date());

		userRepository.save(user);
		try {
			myEmailService.sendOtpMessage(user.getUserEmail(), "New Credentials",
					" you can find below your credentials for the portail , userName :" + user.getUserName()
							+ " and your password is : " + password);
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok().body(gson.toJson("updated successfully "));

	}

	@PostMapping("addVues")
	public ResponseEntity<String> addVues(@RequestBody AddAllVue addAccess) {

		for (String s : addAccess.getVue()) {
			if (!vueActionRepository.existsByLibelle(s)) {

				VueActionBkp vueAction = new VueActionBkp();
				vueAction.setGroupe(addAccess.getGroupe().toUpperCase());
				vueAction.setLibelle(s.toUpperCase());

				VueActionBkp vueAction1 = vueActionRepository.save(vueAction);
				List<User> users = userRepository.findAll();
				for (User u : users) {
					for (Role r : u.getRoles()) {
						/*
						 * List<UserPermission> userPermissions = userPermissionRepository
						 * .findByUserPermissionId_RoleId((int) (long) r.getId());
						 */
						UserPermissionId userPermissionId = new UserPermissionId((int) (long) r.getId(),
								vueAction1.getUrl());
						VuePrivilege i = new VuePrivilege(vueAction1.getUrl(), 0);
						UserPermission userPermission = new UserPermission(userPermissionId, i.getPrivilege());
						userPermissionRepository.save(userPermission);
					}
				}
			}

		}
		return ResponseEntity.ok("added ");

	}
	
	
	@PostMapping("addNav")
	public void addNav(@RequestBody Set<Nav> navSet) {
		logger.info("begin add nav");
		System.out.println("fefefef");
		Set<VueActionBkp> vueActionBkpList = new HashSet<>();
		for (Nav nav : navSet) {
			for (Child child : nav.getChildren()) {
				for (Child_ childChild : child.getChildren()) {
					if (childChild.getUrl() != null)
						vueActionBkpList
								.add(new VueActionBkp(childChild.getName(), child.getName(), childChild.getUrl()));
				}
				if (child.getUrl() != null)
					vueActionBkpList.add(new VueActionBkp(child.getName(), nav.getName(), child.getUrl()));
			}
		}
        
		List<VueActionBkp> allFromDb = vueActionBkpRepo.findAll();
		System.out.println("list 0    "+allFromDb.size());
		System.out.println("list 00    "+vueActionBkpList.size());
		Set<VueActionBkp> vueActionBkpListToAdd =new HashSet<>(vueActionBkpList) ;
		
		List<VueActionBkp> vueActionBkpListToRemove =new ArrayList<>(allFromDb);
		for (VueActionBkp vueActionBkp : vueActionBkpList) {
			vueActionBkpListToRemove.removeIf(vueActionBkp1 -> vueActionBkp1.getGroupe().equals(vueActionBkp.getGroupe())
					&& vueActionBkp1.getLibelle().equals(vueActionBkp.getLibelle())
					&& vueActionBkp1.getUrl().equals(vueActionBkp.getUrl()));
		}
		List<UserPermission> permissionsToDelete = new ArrayList<>();
		for (VueActionBkp vueActionBkp : vueActionBkpListToRemove) {
			List<UserPermission> byUserPermissionId_vueID = userPermissionRepository
					.findByUserPermissionId_VueID(vueActionBkp.getUrl());
			byUserPermissionId_vueID.forEach(userPermission -> permissionsToDelete.add(new UserPermission(
					new UserPermissionId(userPermission.getUserPermissionId().getRoleId(), vueActionBkp.getUrl()),
					userPermission.getValue())));
		}
		vueActionBkpRepo.deleteAll(vueActionBkpListToRemove);
		System.out.println("list 002    "+vueActionBkpList.size());
		System.out.println("list 02    "+allFromDb.size());
		System.out.println("list 2    "+vueActionBkpListToRemove.size());
		userPermissionRepository.deleteAll(permissionsToDelete);
		
			for (VueActionBkp vueActionBkp : allFromDb) {
				vueActionBkpListToAdd.removeIf(vueActionBkp1 -> vueActionBkp1.getGroupe().equals(vueActionBkp.getGroupe())
						&& vueActionBkp1.getLibelle().equals(vueActionBkp.getLibelle())
						&& vueActionBkp1.getUrl().equals(vueActionBkp.getUrl()));
			}
			List<Role> roleList= roleRepository.findAll();
			List<UserPermission> permissionsToAdd = new ArrayList<>();
			vueActionBkpListToAdd.forEach(vueActionBkp -> permissionsToAdd
					.add(new UserPermission(new UserPermissionId(1, vueActionBkp.getUrl()), 2)));
			for(Role role: roleList) {
				if(role.getId()!=1) {
					Integer id = role.getId().intValue();
					vueActionBkpListToAdd.forEach(vueActionBkp ->permissionsToAdd
				.add(new UserPermission(new UserPermissionId(id, vueActionBkp.getUrl()), 0)));}
			}
			System.out.println("list 1    "+vueActionBkpListToAdd.size());
			vueActionBkpRepo.saveAll(vueActionBkpListToAdd);
			userPermissionRepository.saveAll(permissionsToAdd);
		
			
		
			
//          vueActionBkpRepo.deleteAll(allFromDb);
		/*
		 * for (VueActionBkp vueActionBkp : allFromDb) {
		 * vueActionBkpList.removeIf(vueActionBkp1 ->
		 * vueActionBkp1.getGroupe().equals(vueActionBkp.getGroupe()) &&
		 * vueActionBkp1.getLibelle().equals(vueActionBkp.getLibelle())); }
		 * vueActionBkpRepo.saveAll(vueActionBkpList);
		 */
		logger.info("end add nav");
	}

	@GetMapping("getAllVues")
	public List<VueActionBkp> getAllVues() {
		return vueActionBkpRepo.findAll();
	}

}
