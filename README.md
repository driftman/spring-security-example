This is a example of using spring security authentication and how to wire your buisiness logic to it easily , you can also use your ORM configuration, without having to adapt your configuration to the spring one, with you are free and the SpringSecurityDaoAuthentication.java is independent to any configuration it use only simple features by accassing your Entities with minimal efforts. Inside the class you'll find how to wire you Buisiness Class by the Comments . READ THEM ! 


The Dependencies using MAVEN : 

	<!-- Spring Security -->
	<dependency>
		<groupId>org.springframework.security</groupId>
		<artifactId>spring-security-web</artifactId>
		<version>4.0.2.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>org.springframework.security</groupId>
		<artifactId>spring-security-config</artifactId>
		<version>4.0.2.RELEASE</version>
	</dependency>
	<dependency>
		<groupId>org.springframework.security</groupId>
		<artifactId>spring-security-core</artifactId>
		<version>4.0.2.RELEASE</version>
	</dependency>
