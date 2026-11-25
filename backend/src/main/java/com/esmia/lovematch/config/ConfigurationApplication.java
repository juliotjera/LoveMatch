package com.esmia.lovematch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfigurationApplication implements WebMvcConfigurer {

    @Bean
    public OpenAPI documentation() {
        return new OpenAPI().info(new Info()
            .title("LoveMatch — API")
            .version("1.0.0")
            .description("Profils, score de compatibilité expliqué, likes, matchs mutuels, messagerie, modération. "
                + "Authentification par cookie HttpOnly (LM_SESSION) ; requêtes d'écriture protégées par CSRF (X-XSRF-TOKEN)."));
    }

    // fichiers de l'interface dont le nom contient une empreinte (main-XXXX.js) : gardés un an par le navigateur,
    // un seul téléchargement sur un réseau lent ; index.html, ngsw-worker.js et les dictionnaires restent toujours à jour
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registre) {
        CacheControl unAn = CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable();
        registre.addResourceHandler("/main-*.js", "/chunk-*.js", "/polyfills-*.js", "/styles-*.css")
            .addResourceLocations("classpath:/static/").setCacheControl(unAn);
        registre.addResourceHandler("/media/**").addResourceLocations("classpath:/static/media/").setCacheControl(unAn);
    }

    // routes de l'interface Angular (rechargement d'une page profonde) : renvoyer index.html
    @Override
    public void addViewControllers(ViewControllerRegistry registre) {
        for (String route : new String[] {"/connexion", "/inscription", "/decouvrir", "/matchs", "/matchs/{id}",
                "/profil", "/notifications", "/admin", "/confidentialite"}) {
            registre.addViewController(route).setViewName("forward:/index.html");
        }
    }
}
