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

package me.calebjones.blogsite;

import android.app.Application;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;

import java.util.Collections;

import me.calebjones.blogsite.content.database.SharedPrefs;

public class BlogsiteApplication extends Application {

    private static BlogsiteApplication mInstance;

    // Disable HTTP/2 for interop with NGINX 1.9.5.
    // TODO: remove this hack after 2015-12-31.
    public OkHttpClient client = new OkHttpClient().setProtocols(Collections.singletonList(Protocol.HTTP_1_1));

    public static synchronized BlogsiteApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        int cacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(getCacheDir(), cacheSize);
        client.setCache(cache);

        SharedPrefs.create(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
