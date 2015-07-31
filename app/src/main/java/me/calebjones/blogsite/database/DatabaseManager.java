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

package me.calebjones.blogsite.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import me.calebjones.blogsite.models.Posts;
import me.calebjones.blogsite.util.BlipUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {

    private static final String DB_NAME = "WP_POSTS.db";
    private static final int DB_VERSION = 1;
    private SQLiteDatabase DB;

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_REAL = " REAL";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String COMMA_SEP = ",";

    private static final String DOWNLOAD_TABLE = "Download_Table";
    private static final String TABLE_POST = "Post_Table";
    private static final String DATE = "DATE";
    private static final String PostID = "postID";
    private static final String URL = "link";
    private static final String CONTENT = "content";
    private static final String EXCERPT = "excerpt";
    private static final String TAGS = "tags";
    private static final String CATEGORY = "category";
    private static final String IMG = "img";
    private static final String TITLE = "title";
    private static final String FAV = "fav";
    private static final String ID = "id";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_POST + "(" +
                    "id integer primary key autoincrement" + COMMA_SEP +
                    DATE + TYPE_TEXT + COMMA_SEP +
                    PostID + TYPE_INTEGER + COMMA_SEP +
                    URL + TYPE_TEXT + COMMA_SEP +
                    CONTENT + TYPE_TEXT + COMMA_SEP +
                    EXCERPT + TYPE_TEXT + COMMA_SEP +
                    TAGS + TYPE_TEXT + COMMA_SEP +
                    CATEGORY + TYPE_TEXT + COMMA_SEP +
                    IMG + TYPE_TEXT + COMMA_SEP +
                    TITLE + TYPE_TEXT + COMMA_SEP +
                    FAV + TYPE_INTEGER + ")";

    private static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_POST;

    public DatabaseManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TABLE);
        onCreate(db);
    }

    public void rebuildDB(){
        DB.execSQL("DELETE FROM Post_Table");
    }

    public void addPost(Posts item) {
        if (feedExists(item)) {
            return;
        } else if (itemExists(item)){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DATE, item.getDate());
        values.put(PostID, item.getPostID());
        values.put(URL, item.getURL());
        values.put(CONTENT, item.getContent());
        values.put(EXCERPT, item.getExcerpt());
        values.put(TAGS, item.getTags());
        values.put(CATEGORY, item.getCategories());
        values.put(IMG, item.getFeaturedImage());
        values.put(TITLE, item.getTitle());
        values.put(FAV, item.isFavourite() ? 1 : 0);

        getWritableDatabase().insert(TABLE_POST, null, values);
    }

    public void setFavourite(int num, boolean fav) {
        if (!feedExists(getPost(num))) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(FAV, fav ? 1 : 0);
        getWritableDatabase().update(TABLE_POST, contentValues, ID + " = ?", new String[]{String.valueOf(num)});
    }

    public void updatePost(Posts item) {
        if (!feedExists(item)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DATE, item.getDate());
        values.put(PostID, item.getPostID());
        values.put(URL, item.getURL());
        values.put(CONTENT, item.getContent());
        values.put(EXCERPT, item.getExcerpt());
        values.put(TAGS, item.getTags());
        values.put(CATEGORY, item.getCategories());
        values.put(IMG, item.getFeaturedImage());
        values.put(TITLE, item.getTitle());
        values.put(FAV, item.isFavourite() ? 1 : 0);

        getWritableDatabase().update(TABLE_POST, values, ID + " = ?", new String[]{String.valueOf(item.getID())});
    }

    public Posts getPost(int num) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + ID + " = ?",
                new String[]{String.valueOf(num)});
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            Posts item = new Posts();
            item.setID(cursor.getInt(0));
            item.setDate(cursor.getString(1));
            item.setPostID(cursor.getInt(2));
            item.setURL(cursor.getString(3));
            item.setContent(cursor.getString(4));
            item.setExcerpt(cursor.getString(5));
            item.setTags(cursor.getString(6));
            item.setCategories(cursor.getString(7));
            item.setFeaturedImage(cursor.getString(8));
            item.setTitle(cursor.getString(9));
            item.setFavourite(cursor.getInt(10) == 1);
            cursor.close();
            return item;
        }
        return null;
    }

    public List<Posts> getAllPosts() {
        List<Posts> posts = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST, null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            posts = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                posts.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return posts;
    }

    public List<Integer> getAllMissingTranscripts() {
        List<Integer> nums = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT " + ID + " FROM " +
                TABLE_POST + " WHERE " + TAGS + " = ''", null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            nums = new ArrayList<>();
            do {
                nums.add(cursor.getInt(0));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return nums;
    }

    public List<Posts> search(String keyWord) {
        List<Posts> comics = Collections.emptyList();
        int num = 0;
        if (BlipUtils.isNumeric(keyWord)) {
            num = Integer.parseInt(keyWord);
        }
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + TITLE +
                " LIKE '%" + keyWord + "%' OR " + ID + " = " + num, null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            comics = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                comics.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return comics;
    }

    //Need to add this logic to the different categories.
    public List<Posts> getFeed(String mCategory) {
        switch(mCategory){
            case (""):
                return getFeed(0);
            case ("home"):
                return getFeed(0);
            case ("blog"):
                return getBlog();
            case ("science"):
                return getScience();
            case ("android"):
                return getAndroid();
            case ("parenting"):
                return getParenting();
            case ("technology"):
                return getTechnology();
        }
        return getFeed(0);
    }

    public List<Posts> getFeed(int continuationNum) {
        int continuation = getMax();
        if (continuationNum != 0)
            continuation = continuationNum;

        int low = continuation - 15;
        if (low < 1) {
            low = 1;
        }
        Log.d("The Jones Theory", "Low: " + low + " contNum: " + continuation);

        List<Posts> posts = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + ID + " <= ? AND " + ID + " >= ?" +
                        " ORDER BY " + " date(date) " + " DESC",
                new String[]{String.valueOf(continuation), String.valueOf(low)});
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            posts = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                posts.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return posts;
    }


    public List<Posts> getScience() {
        List<Posts> posts = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + CATEGORY + " LIKE " + "'%science%'" +
                        " ORDER BY " + " date(DATE) " + " DESC", null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            posts = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                posts.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return posts;
    }

    public List<Posts> getBlog() {

        List<Posts> posts = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + CATEGORY + " LIKE " + "'%blog%'" +
                " ORDER BY " + " date(DATE) " + " DESC", null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            posts = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                posts.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return posts;
    }


    public List<Posts> getParenting() {
        List<Posts> posts = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + CATEGORY + " LIKE " + "'%parenting%'" +
                " ORDER BY " + " date(DATE) " + " DESC", null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            posts = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                posts.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return posts;
    }

    public List<Posts> getAndroid() {
        List<Posts> posts = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + CATEGORY + " LIKE " + "'%android%'" +
                " ORDER BY " + " date(DATE) " + " DESC", null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            posts = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                posts.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return posts;
    }

    public List<Posts> getTechnology() {
        List<Posts> posts = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + CATEGORY + " LIKE " + "'%technology%'" +
                " ORDER BY " + " date(DATE) " + " DESC", null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            posts = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                posts.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return posts;
    }

    public List<Posts> getFavourites() {
        List<Posts> comics = Collections.emptyList();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST + " WHERE " + FAV + " = 1", null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToFirst()) {
            comics = new ArrayList<>();
            do {
                Posts item = new Posts();
                item.setID(cursor.getInt(0));
                item.setDate(cursor.getString(1));
                item.setPostID(cursor.getInt(2));
                item.setURL(cursor.getString(3));
                item.setContent(cursor.getString(4));
                item.setExcerpt(cursor.getString(5));
                item.setTags(cursor.getString(6));
                item.setCategories(cursor.getString(7));
                item.setFeaturedImage(cursor.getString(8));
                item.setTitle(cursor.getString(9));
                item.setFavourite(cursor.getInt(10) == 1);
                comics.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return comics;
    }

    public int getMax() {
        int max = 0;
        Cursor cursor = getReadableDatabase().rawQuery("SELECT max(" + ID + ") FROM " + TABLE_POST, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            max = cursor.getInt(0);
            cursor.close();
        }
        return max;
    }

    public int getCount() {
        int count = 0;
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST, null);
        if (cursor != null && cursor.getCount() != 0) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }


    public boolean feedExists(Posts item) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST
                + " WHERE " + ID + " = ?", new String[]{String.valueOf(item.getID())});

        boolean exists = false;
        if (cursor != null && cursor.getCount() != 0) {
            exists = true;
            Log.d("The Jones Theory", "feedExists: " + exists);
            cursor.close();
        }
        return exists;
    }

    public boolean idExists(String item) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST
                + " WHERE " + PostID + " = ?", new String[]{item});

        boolean exists = false;
        if (cursor != null && cursor.getCount() != 0) {
            exists = true;
            Log.d("The Jones Theory", "feedExists: " + exists);
            cursor.close();
        }
        return exists;
    }

    public boolean itemExists(Posts item) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_POST
                + " WHERE " + TITLE + " = ?", new String[]{String.valueOf(item.getTitle())});
        boolean exists = false;
        if (cursor != null && cursor.getCount() != 0) {
            exists = true;
            cursor.close();
        }
        return exists;
    }
}
