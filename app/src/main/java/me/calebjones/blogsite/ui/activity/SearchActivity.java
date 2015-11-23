/*
 * Copyright 2015, Tanmay Parikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.calebjones.blogsite.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import me.calebjones.blogsite.BlogsiteApplication;
import me.calebjones.blogsite.MainActivity;
import me.calebjones.blogsite.R;
import me.calebjones.blogsite.content.database.DatabaseManager;
import me.calebjones.blogsite.content.models.Posts;
import me.calebjones.blogsite.util.BlipUtils;


public class SearchActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    private DatabaseManager databaseManager;
    private RecyclerView recyclerView;
    private EditText searchBar;
    private View home, clear;
    private SearchListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        databaseManager = new DatabaseManager(this);

        recyclerView = (RecyclerView) findViewById(R.id.results);
        StaggeredGridLayoutManager layoutManager;
        if (getResources().getBoolean(R.bool.landscape)) {
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SearchListAdapter();
        recyclerView.setAdapter(adapter);

        searchBar = (EditText) findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(this);

        home = findViewById(R.id.home);
        clear = findViewById(R.id.clear);
        clear.setOnClickListener(this);
        home.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.myPrimaryColor));
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.toString().equals("")) {
            adapter.updateList(databaseManager.search(s.toString()));
        } else {
            adapter.updateList(Collections.<Posts>emptyList());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home:
                onBackPressed();
                break;
            case R.id.clear:
                if (searchBar != null)
                    searchBar.setText("");
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.ViewHolder> {

        List<Posts> myPosts = Collections.emptyList();
        SimpleDateFormat simpleDateFormat;

        public SearchListAdapter() {
            simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy (EEEE)", Locale.getDefault());
            OkHttpClient picassoClient = BlogsiteApplication.getInstance().client.clone();
            picassoClient.interceptors().add(BlipUtils.REWRITE_CACHE_CONTROL_INTERCEPTOR);
            new Picasso.Builder(SearchActivity.this).downloader(new OkHttpDownloader(picassoClient)).build();
        }

        public void updateList(List<Posts> Posts) {
            this.myPosts = Posts;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.content_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Posts posts = myPosts.get(position);
            String title = posts.getPostID() + ". " + Html.fromHtml(posts.getTitle());

            holder.title.setText(title);
            holder.alt.setText(Html.fromHtml(posts.getExcerpt()));

            Picasso.with(holder.img.getContext())
                    .load(posts.getFeaturedImage())
                    .error(R.drawable.placeholder)
                    .into(holder.img);

        }

        @Override
        public int getItemCount() {
            return myPosts.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView title, date, alt;
            ImageView img, favourite;
            View browser, transcript, imgContainer, share, explain;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title);
                alt = (TextView) itemView.findViewById(R.id.excerpt);
                img = (ImageView) itemView.findViewById(R.id.thumbnail);

                img.setOnClickListener(this);
                title.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                final int position = getAdapterPosition();
                Log.d("The Jones Theory-Search", "ID Clicked: " + myPosts.get(position).getPostID());
                Intent intent = new Intent(getBaseContext(), PostSelectedActivity.class);
                intent.putExtra("PostID", myPosts.get(position).getPostID());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getBaseContext().startActivity(intent);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}
