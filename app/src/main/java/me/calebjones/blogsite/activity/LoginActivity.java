package me.calebjones.blogsite.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import me.calebjones.blogsite.MainActivity;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.loader.PostLoader;

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String DUMMY_CREDENTIALS = "user@test.com:hello";

    private UserLoginTask userLoginTask = null;
    private Toolbar toolbar;
    private View loginFormView;
    private View progressView;
    private AutoCompleteTextView usernameTextView;
    private EditText passwordTextView;
    private TextView signUpTextView;
    public final CollapsingToolbarLayout collapsingToolbar= null;
    public AppBarLayout appBarLayout;
    public CoordinatorLayout coordinatorLayout;
    public Context context;
    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?category=blog&number=15";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setTitle("Sign In");

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);


        new PostLoader().execute(mURL);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);

        usernameTextView = (AutoCompleteTextView) findViewById(R.id.email);
        usernameTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true) {
                    collapseToolbar();
                }

            }
        });
        loadAutoComplete();

        passwordTextView = (EditText) findViewById(R.id.password);
        passwordTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true)
                {
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

        Button loginButton = (Button) findViewById(R.id.email_sign_in_button);
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

        Button faceButton = (Button) findViewById(R.id.email_facebook);
        faceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast.makeText(getApplicationContext(), "Not available yet! =(", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);

        //adding underline and link to signup textview
        signUpTextView = (TextView) findViewById(R.id.email_register);
        signUpTextView.setPaintFlags(signUpTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        Linkify.addLinks(signUpTextView, Linkify.ALL);

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("LoginActivity", "Sign Up Activity activated.");
                // this is where you should start the signup Activity
                 LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                // response = UserRegistrationTask("Caman9119", "caman9119@charter.net", "dd9d4bf59e", "1481");

            }
        });
    }

    public void collapseToolbar(){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if(behavior!=null) {
            behavior.onNestedFling(coordinatorLayout, appBarLayout, null, 0, 10000, true);
        }
    }

    private void loadAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        usernameTextView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
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
            Log.v("The Jones Theory", "doinbg");

            try {
                //Set up the Query and add in the aparamters
                String query = String.format("username=%s&password=%s",
                        URLEncoder.encode(usernameStr, charset),
                        URLEncoder.encode(passwordStr, charset));

                Log.v("The Jones Theory", "doInBg: Query -" + query);

                URL url = new URL("http://calebjones.me/api/user/generate_auth_cookie/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                Log.v("The Jones Theory", urlConnection.toString());
                urlConnection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("Content-Length", "" +
                        Integer.toString(query.getBytes().length));
                urlConnection.setRequestProperty("Content-Language", "en-US");
                Log.v("The Jones Theory", "URL " + urlConnection.toString());

                DataOutputStream wr = new DataOutputStream(
                        urlConnection.getOutputStream());
                wr.writeBytes(query);
                wr.flush();
                wr.close();

                InputStream inP = new BufferedInputStream(urlConnection.getInputStream());
                Log.v("The Jones Theory", "InputStream: " + inP.toString());

                BufferedReader reader = new BufferedReader(new InputStreamReader(inP));
                Log.v("The Jones Theory", "Reader: " + reader.toString());
                String line;
                while ((line = reader.readLine()) != null) {
                    sResult.append(line);
                    Log.v("The Jones Theory", "doInBg: While Line -" + line);
                }
                result = sResult.toString();
//                result = parseCookie(result);

                JSONObject response = new JSONObject(result);
                cookie = response.optString("cookie");
                error = response.optString("error");



                if (!TextUtils.isEmpty(cookie)){
                    Log.i("The Jones Theory", "Cookie: " + cookie);

                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 4);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("AUTH_COOKIE", cookie);
                    edit.apply();
                }
            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                Log.v("The Jones Theory", "doInBg: urlConnection.disconnect();");
                urlConnection.disconnect();
            }

            Log.v("The Jones Theory", "doInBg: return");
            return cookie;

        }

        @Override
        protected void onPostExecute(String cookie) {
            userLoginTask = null;
            //stop the progress spinner
            showProgress(false);

            if (TextUtils.isEmpty(error) && !TextUtils.isEmpty(cookie)) {
                //  activity_login success and move to main Activity here.
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 4);
                boolean previouslyStarted = prefs.getBoolean("PREVIOUSLY_STARTED_KEY", false);
                Log.d("The Jones Theory-D", "Success = " + Boolean.toString(previouslyStarted));
                if(!previouslyStarted){
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean("PREVIOUSLY_STARTED_KEY", Boolean.TRUE);
                    edit.commit();
                }
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);


            } else {
                Log.i("The Jones Theory", "Error:" + error);

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

            SharedPreferences prefs = this.getSharedPreferences("MyPref", 4);
            boolean previouslyStarted = prefs.getBoolean("PREVIOUSLY_STARTED_KEY", false);
            Log.d("The Jones Theory-D", "Skip = " + Boolean.toString(previouslyStarted));
            if(!previouslyStarted){
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("PREVIOUSLY_STARTED_KEY", Boolean.TRUE);
                edit.apply();
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("Category", "blog");
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
