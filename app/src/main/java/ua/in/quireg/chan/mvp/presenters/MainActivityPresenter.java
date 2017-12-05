package ua.in.quireg.chan.mvp.presenters;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Arcturus Mengsk on 12/1/2017, 10:33 AM.
 * 2ch-Browser
 */

public interface MainActivityPresenter {

    void onActivityAttached(AppCompatActivity activity);

    void onActivityDetached();

    void onBackPressed();

    void pushFragment(Fragment fragment);

    void navigateBoard(String website, String board);

    void navigateThread(String website, String board, String thread, String subject, String post, boolean preferDeserialized);

    void navigateGallery(Uri imageUri, String threadUrl);

    void navigateCatalog(String website, String board);
}
