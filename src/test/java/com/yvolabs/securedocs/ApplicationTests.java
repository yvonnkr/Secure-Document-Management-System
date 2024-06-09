package com.yvolabs.securedocs;

import com.yvolabs.securedocs.resource.UserResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class ApplicationTests {
    @Autowired
    private UserResource userResource;

    @Test
    void contextLoads() {
        assertThat(userResource).isNotNull();
    }

}
