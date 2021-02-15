package com.experian.responsevalidation;

import java.io.IOException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class SSHUtil {
	/* Set Up Sshj */
	public SSHClient setupSshj() {
		String remoteHost = Constants.RemoteHost;
		String userName = Constants.UserName;
		String password = Constants.Password;
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		System.out.println("ADDHost Verified");
		try {
			client.connect(remoteHost);
			System.out.println("SSH Connected ");
			client.authPassword(userName, password);
			System.out.println("SSH Auth Credentials ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return client;
	}

	public ChannelSftp setupJsch() throws JSchException {
		Session jschSession = null;
		try {
			JSch jsch = new JSch();
			String remoteHost = Constants.RemoteHost;
			String userName = Constants.UserName;
			String password = Constants.Password;
			
			jschSession = jsch.getSession(userName, remoteHost);
			jschSession.setPassword(password);
			
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			jschSession.setConfig(config);
			jschSession.connect();
		
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (ChannelSftp) jschSession.openChannel("sftp");

	}

}
