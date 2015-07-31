package me.calebjones.blogsite.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import me.calebjones.blogsite.MainActivity;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.database.SharedPrefs;
import me.calebjones.blogsite.network.PostLoader;
import me.calebjones.blogsite.util.AuthValidate;
import me.calebjones.blogsite.util.FBConnect;

public class LoginActivity extends AppCompatActivity {

    private static final String DUMMY_CREDENTIALS = "user@test.com:hello";
    private static final String TAG = "The Jones Theory - LA";

    CallbackManager callbackManager;

    private UserLoginTask userLoginTask = null;
    private Toolbar toolbar;
    private View loginFormView;
    private View progressView;
    private EditText usernameTextView;
    private EditText passwordTextView;
    private TextView signupButton;
    public Button loginButton;
    public LoginButton FaceBookButton;
    public final CollapsingToolbarLayout collapsingToolbar = null;
    public AppBarLayout appBarLayout;
    public CoordinatorLayout coordinatorLayout;
    public Context context;
    public String cookie;
    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?category=blog&number=15";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();

        FaceBookButton = (LoginButton) findViewById(R.id.login_button);
        FaceBookButton.setReadPermissions(Arrays.asList("email"));
        // Other app specific specialization

        // Callback registration
        FaceBookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code


                //If first run set to false.
                if(SharedPrefs.getInstance().getFirstRun()){
                    SharedPrefs.getInstance().setFirstRun(false);
                }

                AccessToken token = AccessToken.getCurrentAccessToken();
                //GET AUTH COOKIE

                if (token != null) {
                    showProgress(true);
                    FBConnect fbAuth = new FBConnect(token.getToken()) {
                        @Override
                        protected void onPostExecute(Boolean result) {
                            //Do something with the JSON string
                            Log.v(TAG, "onPostExecute: " + result);

                            showProgress(false);

                            if (result) {
                                //DO THIS
                                SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 4);
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("AUTH_COOKIE", cookieStr);
                                edit.apply();
                            } else {
                                //DO THAT
                                LoginManager.getInstance().logOut();
                                Toast.makeText(getApplicationContext(), "Sorry, something happened please log back in.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    fbAuth.execute();
                }

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }

            @Override
            public void onCancel() {
                // App code
                Log.d(TAG, "Facebook: onCancel");
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d(TAG, "Facebook: onError - " + exception);
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setTitle("Sign In");

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        usernameTextView = (EditText) findViewById(R.id.email);
        usernameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    collapseToolbar();
                }

            }
        });

        passwordTextView = (EditText) findViewById(R.id.password);
        passwordTextView.setTransformationMethod(new PasswordTransformationMethod());
        passwordTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    collapseToolbar();
                }

            }
        });
        passwordTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    try {
                        initLogin();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });

        loginButton = (Button) findViewById(R.id.email_sign_in_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    initLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);


        signupButton = (TextView) findViewById(R.id.email_register);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Sign Up Activity activated.");
                // this is where you should start the signup Activity
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

        SharedPreferences prefs = this.getSharedPreferences("MyPref", 4);
        if (!prefs.getString("AUTH_COOKIE", "").equals("")) {
            showProgress(true);
            AuthValidate doAuth = new AuthValidate(prefs.getString("AUTH_COOKIE", "")) {
                @Override
                protected void onPostExecute(Boolean result) {
                    //Do something with the JSON string
                    Log.v(TAG, "onPostExecute: " + result);
                    showProgress(false);
                    if (result) {
                        usernameTextView.setVisibility(View.GONE);
                        passwordTextView.setVisibility(View.GONE);
                        loginButton.setVisibility(View.GONE);
                        signupButton.setVisibility(View.GONE);
                    } else {
                        //DO THAT
                    }
                }
            };
            doAuth.execute();
        }

    }

    @Override
    public void onResume() {
        SharedPreferences prefs = this.getSharedPreferences("MyPref", 4);
        if (!prefs.getString("AUTH_COOKIE", "").equals("")) {
            showProgress(true);
            AuthValidate doAuth = new AuthValidate(prefs.getString("AUTH_COOKIE", "")) {
                @Override
                protected void onPostExecute(Boolean result) {
                    //Do something with the JSON string
                    Log.v(TAG, "onPostExecute: " + result);
                    showProgress(false);
                    if (result) {
                        usernameTextView.setVisibility(View.GONE);
                        passwordTextView.setVisibility(View.GONE);
                        loginButton.setVisibility(View.GONE);
                        signupButton.setVisibility(View.GONE);
                    } else {
                        //DO THAT
                    }
                }
            };
            doAuth.execute();
        }
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void collapseToolbar(){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null) {
            behavior.onNestedFling(coordinatorLayout, appBarLayout, null, 0, 15000, true);
        }
    }

    /**
     * Validate Login form and authenticate.
     */
    public void initLogin() throws Exception {
        if (userLoginTask != null) {
            return;
        }

        usernameTextView.setError(null);
        passwordTextView.setError(null);

        String username = usernameTextView.getText().toString();
        String password = passwordTextView.getText().toString();

        boolean cancelLogin = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordTextView.setError(getString(R.string.invalid_password));
            focusView = passwordTextView;
            cancelLogin = true;
        }

        if (TextUtils.isEmpty(username)) {
            usernameTextView.setError(getString(R.string.field_required));
            focusView = usernameTextView;
            cancelLogin = true;
        } else if (!isEmailValid(username)) {
            usernameTextView.setError(getString(R.string.invalid_email));
            focusView = usernameTextView;
            cancelLogin = true;
        }

        if (cancelLogin) {
            // error in activity_login
            focusView.requestFocus();
        } else {
            // show progress spinner, and start background task to activity_login
            showProgress(true);
            userLoginTask = new UserLoginTask(username, password);
            userLoginTask.execute();
        }
    }

    private boolean isEmailValid(String email) {
        //add your own logic
        return email.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        //add your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the activity_login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Async Login Task to authenticate
     */
    public class UserLoginTask extends AsyncTask<String, Void, String> {
        HttpURLConnection urlConnection;

        private final String usernameStr;
        private final String passwordStr;
        public String cookie = null;
        public String error;
        public final String charset = "UTF-8";

        UserLoginTask(String username, String password) {
            usernameStr = username;
            passwordStr = password;
        }

        @Override
        protected String doInBackground(String... params) {
            //this is where you should write your authentication code
            // or call external service
            // following try-catch just simulates network access
            //http://localhost/api/user/generate_auth_cookie/?username=john&password=PASSWORD-HERE
            String result = "empty";
            StringBuilder sResult = new StringBuilder();
            Log.v(TAG, "doinbg");

            try {
                //Set up the Query and add in the aparamters
                String query = String.format("username=%s&password=%s",
                        URLEncoder.encode(usernameStr, charset),
                        URLEncoder.encode(passwordStr, charset));

                Log.v(TAG, "doInBg: Query -" + query);

                URL url = new URL("http://calebjones.me/api/user/generate_auth_cookie/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                Log.v(TAG, urlConnection.toString());
                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", "" +
                        Integer.toString(query.getBytes().length));
                urlConnection.setRequestProperty("Content-Language", "en-US");
                Log.v(TAG, "URL " + urlConnection.toString());

                DataOutputStream wr = new DataOutputStream(
                        urlConnection.getOutputStream());
                wr.writeBytes(query);
                wr.flush();
                wr.close();

                InputStream inP = new BufferedInputStream(urlConnection.getInputStream());
                Log.v(TAG, "InputStream: " + inP.toString());

                BufferedReader reader = new BufferedReader(new InputStreamReader(inP));
                Log.v(TAG, "Reader: " + reader.toString());
                String line;
                while ((line = reader.readLine()) != null) {
                    sResult.append(line);
                    Log.v(TAG, "doInBg: While Line -" + line);
                }
                result = sResult.toString();
//                result = parseCookie(result);

                JSONObject response = new JSONObject(result);
                cookie = response.optString("cookie");
                error = response.optString("error");



                if (!TextUtils.isEmpty(cookie)){
                    Log.i(TAG, "Cookie: " + cookie);

                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 4);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("AUTH_COOKIE", cookie);
                    edit.apply();
                }
            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                Log.v(TAG, "doInBg: urlConnection.disconnect();");
                urlConnection.disconnect();
            }

            Log.v(TAG, "doInBg: return");
            return cookie;

        }

        @Override
        protected void onPostExecute(String cookie) {
            userLoginTask = null;
            //stop the progress spinner
            showProgress(false);

            if (TextUtils.isEmpty(error) && !TextUtils.isEmpty(cookie)) {
                //  activity_login success and move to main Activity here.
                boolean previouslyStarted = SharedPrefs.getInstance().getFirstRun();

                //If first run set to false.
                if(previouslyStarted){
                    SharedPrefs.getInstance().setFirstRun(false);
                }

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Log.i(TAG, "Error:" + error);

                // activity_login failure
                passwordTextView.setError(error);
                passwordTextView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            userLoginTask = null;
            showProgress(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_skip) {
            SharedPreferences sharedPerf = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

            SharedPreferences.Editor sEdit = sharedPerf.edit();
            sEdit.putBoolean("prompt_logged_out", false);
            sEdit.apply();

            SharedPrefs.getInstance().setFirstRun(false);

            //Start activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
