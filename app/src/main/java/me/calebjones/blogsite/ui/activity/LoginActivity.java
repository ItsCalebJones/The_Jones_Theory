package me.calebjones.blogsite.ui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

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

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.database.SharedPrefs;
import me.calebjones.blogsite.util.auth.AuthValidate;
import me.calebjones.blogsite.util.auth.FBConnect;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class LoginActivity extends AppCompatActivity {

    private static final String DUMMY_CREDENTIALS = "user@test.com:hello";
    private static final String TAG = "The Jones Theory - LA";

    CallbackManager callbackManager;

    private UserLoginTask userLoginTask = null;
    private Toolbar toolbar;
    private View loginFormView;
    private View progressView;
    private EditText usernameTextView, passwordTextView;
    private TextView signupButton;
    private ProgressDialog progressDialog;
    public Button loginButton;
    public LoginButton FaceBookButton;
    public AppBarLayout appBarLayout;
    public CoordinatorLayout coordinatorLayout;
    public Context context;
    public String cookie;
    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?category=blog&number=15";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("The Jones Theory", "Error with Read Contacts permission.");
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("The Jones Theory", "Error with Write Contacts permission.");
        }

        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(LoginActivity.this);

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
                if (SharedPrefs.getInstance().getFirstRun()) {
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

                startActivity(new Intent(LoginActivity.this, DownloadActivity.class));
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

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        usernameTextView = (EditText) findViewById(R.id.email);
        usernameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }

            }
        });

        passwordTextView = (EditText) findViewById(R.id.password);
        passwordTextView.setTransformationMethod(new PasswordTransformationMethod());
        passwordTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

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

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < android.os.Build.VERSION_CODES.LOLLIPOP){
            AppCompatButton signIn = (AppCompatButton) findViewById(R.id.email_sign_in_button);
            AppCompatButton signUp = (AppCompatButton) findViewById(R.id.email_register);
            ColorStateList csl = new ColorStateList(new int[][]{new int[0]}, new int[]{0xffffcc00});
            signIn.setSupportBackgroundTintList(csl);
            signUp.setSupportBackgroundTintList(csl);
        }

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
                LoginActivityPermissionsDispatcher.showContactsWithCheck(LoginActivity.this);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        LoginActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    void showContacts() {
        LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));

    }

    // Option
    @OnShowRationale({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    void showRationaleForContact(PermissionRequest request) {
        showRationaleDialog(R.string.permission_contact_rationale, request);
    }

    // Option
    @OnPermissionDenied({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    void onContactDenied() {
        Toast.makeText(this, "Feel free to register at http://calebjones.me", Toast.LENGTH_SHORT).show();
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Sure!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@NonNull DialogInterface dialog, int which) {
                request.proceed();
            }
        })
                .setNegativeButton(R.string.permission_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
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
    public void showProgress(final boolean show) {
        if (show == true) {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();
        } else {
            progressDialog.dismiss();
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


                if (!TextUtils.isEmpty(cookie)) {
                    Log.i(TAG, "Cookie: " + cookie);

                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 4);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("AUTH_COOKIE", cookie);
                    edit.apply();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
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
                if (previouslyStarted) {
                    SharedPrefs.getInstance().setFirstRun(false);
                }

                Intent intent = new Intent(LoginActivity.this, DownloadActivity.class);
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
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d("The Jones Theory", "Menu item: " + id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_skip) {
            SharedPreferences sharedPerf = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

            SharedPreferences.Editor sEdit = sharedPerf.edit();
            sEdit.putBoolean("prompt_logged_out", false);
            sEdit.apply();

            SharedPrefs.getInstance().setFirstRun(false);

            //Start activity
            Intent intent = new Intent(this, DownloadActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
