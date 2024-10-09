package br.com.alura.ScreenMatch;

import br.com.alura.ScreenMatch.principal.Principal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScreenMatchApplicationTests implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenMatchApplicationTests.class,args);
	}
	@Override
	public void run(String... args) throws Exception {
		Principal principal = new Principal();
		principal.exibeMenu();
	}
}
