package com.mss.backOffice.services;
import com.google.common.base.Throwables;
import com.jcraft.jsch.*;
import com.mss.backOffice.controller.SmtSwapController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@Service
public class SshClientService {
	private final Logger logger = LoggerFactory.getLogger(SshClientService.class);

	public String executeCommandWithPassword(String host, String username, String password, String command) {
	    JSch jSch = new JSch();
	    Session session = null;
	    String result = "";

	    try {
	        session = jSch.getSession(username, host, 22);
	        session.setConfig("StrictHostKeyChecking", "no");
	        session.setPassword(password);
	        session.connect();

	        Channel channel = session.openChannel("exec");
	        ((ChannelExec) channel).setCommand(command);

	        InputStream in = channel.getInputStream();
	        OutputStream out = channel.getOutputStream();
	        ((ChannelExec) channel).setErrStream(System.err);
	        channel.connect();

	        out.write((password + "\n").getBytes());
	        out.flush();

	        byte[] tmp = new byte[1024];
	        while (true) {
	            while (in.available() > 0) {
	                int i = in.read(tmp, 0, 1024);
	                if (i < 0) break;
	                result += new String(tmp, 0, i);
	            }
	            if (channel.isClosed()) {
	                if (in.available() > 0) continue;
	                result += "\nexit-status: " + channel.getExitStatus() + "\n";
	                break;
	            }
	            Thread.sleep(1000);
	        }

	        channel.disconnect();
	        session.disconnect();

	    } catch (Exception e) {
	    	logger.info(Throwables.getStackTraceAsString ( e ) );
	        
	    }
	    return result;
	}
}
