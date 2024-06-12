package com.yvolabs.securedocs;

import com.yvolabs.securedocs.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author Yvonne N
 *
 * @apiNote {@code @EnableJpaAuditing} will allow classes annotated with Example - @EntityListeners, @EventListener to work
 * @implNote  {@code @EnableAsync} - used if we have any @Async in the project
 * @see com.yvolabs.securedocs.entity.Auditable  - @EntityListeners
 * @see com.yvolabs.securedocs.event.listener.UserEventListener - @EventListener
 * @see com.yvolabs.securedocs.service.impl.EmailServiceImpl - @Async
 */

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    /**
     * @implNote Just Run Once to seed some roles to db, left for info purpose
     */
    @Bean
    public CommandLineRunner dbInitializeRoles(RoleRepository roleRepository) {
        return args -> {

            //Just Run Once to seed some roles to db, left for info purpose

/*
            RequestContext.setUserId(0L); // sets the context userId @see Auditable::createdBy, userId 0 will be the System User

            RoleEntity userRole = RoleEntity.builder()
                    .name(Authority.USER.name())
                    .authorities(Authority.USER)
                    .build();
            roleRepository.save(userRole);

            RoleEntity adminRole = RoleEntity.builder()
                    .name(Authority.ADMIN.name())
                    .authorities(Authority.ADMIN)
                    .build();
            roleRepository.save(adminRole);

            RoleEntity superAdminRole = RoleEntity.builder()
                    .name(Authority.SUPER_ADMIN.name())
                    .authorities(Authority.SUPER_ADMIN)
                    .build();
            roleRepository.save(superAdminRole);

            RoleEntity managerRole = RoleEntity.builder()
                    .name(Authority.MANAGER.name())
                    .authorities(Authority.MANAGER)
                    .build();
            roleRepository.save(managerRole);

            RequestContext.start(); // clears the context userId



 */
        };

    }

}
