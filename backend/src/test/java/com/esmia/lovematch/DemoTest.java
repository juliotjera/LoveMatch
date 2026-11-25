package com.esmia.lovematch;

import static org.assertj.core.api.Assertions.assertThat;

import com.esmia.lovematch.entity.Utilisateur;
import com.esmia.lovematch.enumeration.Role;
import com.esmia.lovematch.service.AuthService;
import com.esmia.lovematch.service.DemoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// « Essayer sans compte » ouvre toujours les mêmes comptes, même après une recréation de la démonstration
@SpringBootTest(properties = "lovematch.demo=true")
class DemoTest {

    @Autowired DemoService demo;
    @Autowired AuthService auth;

    @Test
    @DisplayName("Après recréation de la démonstration, le compte membre est toujours Fanja et la modération reste admin")
    void comptesDeDemonstrationStables() {
        demo.preparer();
        demo.preparer();

        Utilisateur membre = auth.compteDemo(Role.MEMBRE);
        assertThat(membre.getPrenom()).isEqualTo("Fanja");
        assertThat(membre.getRole()).isEqualTo(Role.MEMBRE);

        Utilisateur moderation = auth.compteDemo(Role.ADMIN);
        assertThat(moderation.getRole()).isEqualTo(Role.ADMIN);
        assertThat(moderation.isDemo()).isTrue();
    }
}
