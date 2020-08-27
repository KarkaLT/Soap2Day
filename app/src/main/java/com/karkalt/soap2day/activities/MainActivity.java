package com.karkalt.soap2day.activities;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.karkalt.soap2day.R;
import com.karkalt.soap2day.fragments.DownloadsFragment;
import com.karkalt.soap2day.fragments.HomeFragment;
import com.karkalt.soap2day.fragments.SearchFragment;
import com.karkalt.soap2day.themeableMediaRouter.TamableMediaRouteDialogFactory;

import static com.karkalt.soap2day.utils.Utils.isConnected;
import static com.karkalt.soap2day.utils.Utils.setupCast;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    BottomNavigationView bottomNavigationView;

    HomeFragment homeFragment = new HomeFragment();
    SearchFragment searchFragment = new SearchFragment();
    DownloadsFragment downloadsFragment = new DownloadsFragment();

    FrameLayout frameLayout;
    RelativeLayout relativeLayout;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SpannableString string = new SpannableString(getTitle());
        string.setSpan(new StyleSpan(Typeface.BOLD), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(string);

        frameLayout = findViewById(R.id.fragment_container);
        relativeLayout = findViewById(R.id.no_connection);

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        assert uiModeManager != null;
        if (uiModeManager.getCurrentModeType() != Configuration.UI_MODE_TYPE_TELEVISION) {
            setupCast(this);
        } else {
            setMargins(frameLayout, 48, 27, 48, 27);
        }


        if (!isConnected(this)) {
            frameLayout.setVisibility(View.INVISIBLE);
            relativeLayout.setVisibility(View.VISIBLE);
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setTitle(R.string.app_name);
                    frameLayout.setVisibility(View.VISIBLE);
                    relativeLayout.setVisibility(View.GONE);
                    selectedFragment = homeFragment;
                    break;
                case R.id.navigation_search:
                    setTitle(R.string.search);
                    frameLayout.setVisibility(View.VISIBLE);
                    relativeLayout.setVisibility(View.GONE);
                    selectedFragment = searchFragment;
                    break;
                case R.id.navigation_downloads:
                    frameLayout.setVisibility(View.VISIBLE);
                    relativeLayout.setVisibility(View.GONE);
                    setTitle(R.string.downloads);
                    selectedFragment = downloadsFragment;
                    break;
            }
            assert selectedFragment != null;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            if (!isConnected(this) && item.getItemId() != R.id.navigation_downloads) {
                frameLayout.setVisibility(View.INVISIBLE);
                relativeLayout.setVisibility(View.VISIBLE);
            }
            return true;
        });
        bottomNavigationView.setOnNavigationItemReselectedListener(item -> {});


        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
        }
        webView.loadUrl("https://soap2day.to");
        webView.addJavascriptInterface(new JavaScriptInterface(), "HtmlViewer");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webView.loadUrl("javascript:function waitForTitle(i){document.title.includes(i)?window.HtmlViewer.showWebView():setTimeout(function(){waitForTitle(i)},100)}function waitForTitle1(i){document.title.includes(i)?window.HtmlViewer.hideWebView():setTimeout(function(){waitForTitle1(i)},100)}waitForTitle(\"Attention Required! | Cloudflare\"),waitForTitle1(\"SOAP2DAY\");");
            }
        });
    }

    class JavaScriptInterface {

        @JavascriptInterface
        public void showWebView() {
            runOnUiThread(() -> {
                webView.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.GONE);
            });

        }

        @JavascriptInterface
        public void hideWebView() {
            runOnUiThread(() -> {
                webView.setVisibility(View.GONE);
                if (webView != null) {
                    webView.destroy();
                    webView = null;
                }
                bottomNavigationView.setVisibility(View.VISIBLE);
            });
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        MediaRouteButton mediaRouteButton = (MediaRouteButton) menuItem.getActionView();
        mediaRouteButton.setDialogFactory(new TamableMediaRouteDialogFactory());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
}