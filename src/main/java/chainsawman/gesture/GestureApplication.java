package chainsawman.gesture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GestureApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestureApplication.class, args);
	}

}
