package com.mballem.curso.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class DemoSecurityApplication implements CommandLineRunner {

	public static void main(String[] args) {

		SpringApplication.run(DemoSecurityApplication.class, args);
	}

	@Autowired
	JavaMailSender sender;

	@Override
	public void run(String... args) throws Exception {
		SimpleMailMessage simple = new SimpleMailMessage();
		simple.setTo("Seu.email@outlook.com.br");
		simple.setText("Primeiro Teste se der bom deu!");
		simple.setSubject("Testando isso aqui");
		sender.send(simple);
	}
}
