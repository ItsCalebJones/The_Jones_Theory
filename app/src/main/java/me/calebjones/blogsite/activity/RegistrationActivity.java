package me.calebjones.blogsite.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
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

public class RegistrationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //Public Vars
    public String uNonce;

    //Private Vars
    private Toolbar toolbar;
    private View regiFormView;
    private View progressView;
    private AutoCompleteTextView emailTextView;
    private EditText usernameTextView;
    private EditText passwordTextView;
    private EditText confirmTextView;
    private doRegister doRegisterTask = null;
    private getNonce getNonceTask = null;

    int showPbar = 0;

    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?category=blog&number=15";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        getNonceTask = new getNonce();
        getNonceTask.execute();

        new PostLoader().execute(mURL);

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.regi_toolbar);
        setSupportActionBar(toolbar);

        emailTextView = (AutoCompleteTextView) findViewById(R.id.registerEmail);
        loadAutoComplete();



        usernameTextView = (EditText) findViewById(R.id.registerUsername);
        usernameTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        //Checks password field.
        passwordTextView = (EditText) findViewById(R.id.registerPassword);
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

        //Checks EditText
        confirmTextView = (EditText) findViewById(R.id.confirmPassword);
        confirmTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        //Sets up the Submit button.
        Button loginButton = (Button) findViewById(R.id.submit_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    initLogin();
                    Toast.makeText(getApplicationContext(), "Submit!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Sets up the Recover button.
        Button recoverButton = (Button) findViewById(R.id.email_forgot);
        recoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Toast.makeText(getApplicationContext(), "Recover!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        regiFormView = findViewById(R.id.email_register_form);
        progressView = findViewById(R.id.register_progress);

            if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.myPrimaryDarkColor));
        }


    }

    /**
     * Validate Login form and authenticate.
     */
    public void initLogin() throws Exception {
        if (doRegisterTask != null) {
            return;
        }

        emailTextView.setError(null);
        passwordTextView.setError(null);
        confirmTextView.setError(null);
        usernameTextView.setError(null);

        String email = emailTextView.getText().toString();
        String username = usernameTextView.getText().toString();
        String password = passwordTextView.getText().toString();
        String confword = confirmTextView.getText().toString();

        boolean cancelLogin = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)){
            passwordTextView.setError(getString(R.string.field_required));
            focusView = passwordTextView;
            cancelLogin = true;
        } else if (!isPasswordValid(password)){
            passwordTextView.setError(getString(R.string.invalid_password));
            focusView = passwordTextView;
            cancelLogin = true;
        }
        if (TextUtils.isEmpty(username)) {
            Log.i("The Jones Theory", username);
            usernameTextView.setError(getString(R.string.field_required));
            focusView = usernameTextView;
            cancelLogin = true;
        } else if (!isUsernameValid(username)) {
            Log.i("The Jones Theory", "Skipped: " + username);
            usernameTextView.setError(getString(R.string.invalid_username));
            focusView = usernameTextView;
            cancelLogin = true;
        }
        if (TextUtils.isEmpty(email)) {
            emailTextView.setError(getString(R.string.field_required));
            focusView = emailTextView;
            cancelLogin = true;
        } else if (!isEmailValid(email)) {
            emailTextView.setError(getString(R.string.invalid_email));
            focusView = emailTextView;
            cancelLogin = true;
        }
        if (TextUtils.isEmpty(confword)){
            confirmTextView.setError(getString(R.string.field_required));
            focusView = confirmTextView;
            cancelLogin = true;
        }
        else if (!password.equals(confword)){
            Log.i("The Jones Theory", password + " != " + confword);
            confirmTextView.setError("Passwords do not match.");
            focusView = confirmTextView;
            cancelLogin = true;
        }

        if (cancelLogin) {
            // error in activity_login
            focusView.requestFocus();
        } else {
            // show progress spinner, and start background task to activity_login
            showProgress(true);
//            hideSoftKeyboard(RegistrationActivity.this);
            doRegisterTask = new doRegister(username, email, uNonce, password);
            doRegisterTask.execute();
            Toast.makeText(getApplicationContext(), "Login!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmailValid(String email) {
        //add your own logic
        return email.contains("@");
    }

    private boolean isUsernameValid(String username) {
        //add your own logic
        Log.i("The Jones Theory", "isValid " + username + " length: " + username.length());
        return username.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        //add your own logic
        return password.length() > 4;
    }

    private void loadAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
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
                new ArrayAdapter<String>(RegistrationActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        emailTextView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
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
            SharedPreferences sharedPerf = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
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

    public class getNonce extends AsyncTask<String, Void, String> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(RegistrationActivity.this);
            dialog.setMessage("Checking connection...");
            dialog.show();
            }

        @Override
        protected String doInBackground(String... args) {

            InputStream inputStream = null;
            String result = "empty";
            HttpURLConnection urlConnection = null;
            Log.v("The Jones Theory", "Before");

            try {
                Log.v("The Jones Theory", "try");
                URL url = new URL("http://calebjones.me/api/get_nonce/?controller=user&method=register");

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");

                Log.v("The Jones Theory", "URL " + urlConnection.toString());

                int statusCode = urlConnection.getResponseCode();
                Log.v("The Jones Theory", "Status:  " + statusCode);

                if (statusCode ==  200) {

                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    result = response.toString();
                    result = parseNonce(result);
                    return result;
                }else{
                    result = "empty";
                }
            }catch( Exception e) {
                e.printStackTrace();
            }
            Log.v("The Jones Theory", "doInBg: return" + result);
            return result;
        }

        private String parseNonce(String result) {
            String Nonce = null;
            try {
                JSONObject response = new JSONObject(result);
                Nonce = response.optString("nonce");

                String error = response.optString("error");
                Log.i("The Jones Theory", error);
                if (!error.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Error Getting Token: " + error, Toast.LENGTH_SHORT).show();
                }
                Log.v("The Jones Theory", Nonce);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Nonce;
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            //Do something with the JSON string
            Log.v("The Jones Theory", "onPostExecute: " + result);
            uNonce = result;
        }
    }

    public class doRegister extends AsyncTask<String, Void, String> {
        HttpURLConnection urlConnection;

        public final String emailStr;
        public final String passwordStr;
        public final String mNonceStr;
        public final String usernameStr;
        public final String charset = "UTF-8";
        public String Nonce = null;
        public String error;

        doRegister(String username, String email, String mNonce, String password) {
            emailStr = email;
            passwordStr = password;
            mNonceStr = mNonce;
            usernameStr = username;
            Log.v("The Jones Theory", "Constructor: " + emailStr + passwordStr + mNonceStr);
        }

        @Override
        protected String doInBackground(String... args) {

            String result = "empty";
            StringBuilder sResult = new StringBuilder();

            try {
                //Set up the Query and add in the aparamters
                String query = String.format("username=%s&email=%s&nonce=%s&display_name=%s&user_pass=%s",
                        URLEncoder.encode(usernameStr, charset),
                        URLEncoder.encode(emailStr, charset),
                        URLEncoder.encode(mNonceStr, charset),
                        URLEncoder.encode(usernameStr, charset),
                        URLEncoder.encode(passwordStr, charset));

                Log.v("The Jones Theory", "doInBg: Query -" + query);

                URL url = new URL("http://calebjones.me/api/user/register/");
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
                Nonce = response.optString("cookie");
                error = response.optString("error");
                Log.i("The Jones Theory", error);


                SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 4);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("AUTH_COOKIE", Nonce);
                edit.apply();
            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                Log.v("The Jones Theory", "doInBg: urlConnection.disconnect();");
                urlConnection.disconnect();
            }

            Log.v("The Jones Theory", "doInBg: return");
            return Nonce;
        }

        @Override
        protected void onPostExecute(String result) {
            //Do something with the JSON string
            showProgress(false);

            if (!TextUtils.isEmpty(error)){
                if (error.contains("E-mail")){
                    emailTextView.setError(error);
                } else if (error.toLowerCase().contains("username")){
                    usernameTextView.setError(error);
                } else if (error.toLowerCase().contains("password")){
                    passwordTextView.setError(error);
                    confirmTextView.setError(error);
                } else {
                    emailTextView.setText("");
                    emailTextView.requestFocus();
                    usernameTextView.setText("");
                    passwordTextView.setText("");
                    confirmTextView.setText("");
                }
                Toast.makeText(getApplicationContext(), "Error Registering: " + error, Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                intent.putExtra("Category", "blog");
                startActivity(intent);
            }
            Log.v("The Jones Theory", "onPostExecute: " + result);
        }

        private String parseCookie(String result) {
            String Nonce = null;
            try {
                JSONObject response = new JSONObject(result);
                Nonce = response.optString("cookie");
                String error = response.optString("error");
                Log.i("The Jones Theory", error);
                if (!TextUtils.isEmpty(error)){
                    Toast.makeText(getApplicationContext(), "Error Registering: " + error, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return Nonce;
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
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

            regiFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            regiFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    regiFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            regiFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
