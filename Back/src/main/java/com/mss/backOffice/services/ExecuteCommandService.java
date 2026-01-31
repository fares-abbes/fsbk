/**
 * 
 */
package com.mss.backOffice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Lenovo IdeaPad L3
 *
 */
@Service
public class ExecuteCommandService {
	@Autowired
	SshClientService sshClient;
	 @Autowired
	 PropertyService propertyService;
	
    public String executeRemoteCommand(String command) {
    	
        String host =propertyService.getServerHostIp();

        String username = propertyService.getServerHostUser();
        String password = propertyService.getServerHostPassword();
        
 
       return sshClient.executeCommandWithPassword(host, username, password, command);
       
    }
    
    public String executeRemoteCommandSatim(String command) {
    
        String host = propertyService.getServerSatimIp();

        String username = propertyService.getServerSatimUser();
        String password = propertyService.getServerSatimPassword();
   
       return sshClient.executeCommandWithPassword(host, username, password, command);
       
    }
    
}
