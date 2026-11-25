package mg.lovematch.app;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Toast;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // La WebView ne télécharge rien d'elle-même : « Télécharger mes données » (fichier JSON) est confié au
        // gestionnaire de téléchargements d'Android, avec le cookie de session pour que le serveur reconnaisse le membre.
        bridge.getWebView().setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            if (!url.startsWith("https://")) {
                return;
            }
            String nom = URLUtil.guessFileName(url, contentDisposition, mimeType);
            DownloadManager.Request demande = new DownloadManager.Request(Uri.parse(url))
                .setMimeType(mimeType)
                .addRequestHeader("User-Agent", userAgent)
                .setTitle(nom)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            String cookies = CookieManager.getInstance().getCookie(url);
            if (cookies != null) {
                demande.addRequestHeader("Cookie", cookies);
            }
            // Android 10 et plus : dossier Téléchargements, sans permission ; avant : dossier propre à l'application
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                demande.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nom);
            } else {
                demande.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, nom);
            }
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(demande);
            Toast.makeText(this, "⬇ " + nom, Toast.LENGTH_SHORT).show();
        });
    }
}
