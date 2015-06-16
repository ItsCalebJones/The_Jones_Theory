package me.calebjones.blogsite.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.calebjones.blogsite.R;
import me.calebjones.blogsite.feed.FeedItem;

public class PhotoGridView extends Fragment {

    public static String mURL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=100";
    public List<FeedItem> mGridItemList;
    private GridView mGridview;
    ArrayList<getPhotos> arrPhotos;
    private int i;
    public static final String TAG = "The Jones Theory";

    // HOLD THE URL TO MAKE THE API CALL TO
    private String URL;

    // STORE THE PAGING URL
    private String pagingURL;

    // FLAG FOR CURRENT PAGE
    int current_page = 1;

    // BOOLEAN TO CHECK IF NEW FEEDS ARE LOADING
    Boolean loadingMore = true;

    Boolean stopLoadingData = false;


    public PhotoGridView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context hostActivity = getActivity();
        new getPhotosData().execute();
        arrPhotos = new ArrayList<getPhotos>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = getActivity();


        View v = inflater.inflate(R.layout.fragment_grid_view, container, false);
        GridView mGridview = (GridView) v.findViewById(R.id.gridViewImage);

        // uses the view to get the context instead of getActivity().
        return v;
    }


    private class getPhotosData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            // CHANGE THE LOADING MORE STATUS TO PREVENT DUPLICATE CALLS FOR
            // MORE DATA WHILE LOADING A BATCH
            loadingMore = true;

            // SET THE INITIAL URL TO GET THE FIRST LOT OF ALBUMS
            URL = "https://public-api.wordpress.com/rest/v1.1/sites/calebjones.me/posts?number=20";

            try {

                HttpClient hc = new DefaultHttpClient();
                HttpGet get = new HttpGet(URL);
                HttpResponse rp = hc.execute(get);

                if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String queryAlbums = EntityUtils.toString(rp.getEntity());

                    JSONObject JOTemp = new JSONObject(queryAlbums);

                    JSONArray JAPhotos = JOTemp.getJSONArray("data");

                    // IN MY CODE, I GET THE NEXT PAGE LINK HERE

                    getPhotos photos;

                    for (int i = 0; i < JAPhotos.length(); i++) {
                        JSONObject JOPhotos = JAPhotos.getJSONObject(i);
                        // Log.e("INDIVIDUAL ALBUMS", JOPhotos.toString());

                        if (JOPhotos.has("link")) {

                            photos = new getPhotos();

                            // GET THE ALBUM ID
                            if (JOPhotos.has("id")) {
                                photos.setPhotoID(JOPhotos.getString("id"));
                            } else {
                                photos.setPhotoID(null);
                            }

                            // GET THE ALBUM NAME
                            if (JOPhotos.has("name")) {
                                photos.setPhotoName(JOPhotos.getString("name"));
                            } else {
                                photos.setPhotoName(null);
                            }

                            // GET THE ALBUM COVER PHOTO
                            if (JOPhotos.has("picture")) {
                                photos.setPhotoPicture(JOPhotos
                                        .getString("img"));
                            } else {
                                photos.setPhotoPicture(null);
                            }

                            // GET THE PHOTO'S SOURCE
                            if (JOPhotos.has("source")) {
                                photos.setPhotoSource(JOPhotos
                                        .getString("source"));
                            } else {
                                photos.setPhotoSource(null);
                            }

                            arrPhotos.add(photos);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            // SET THE ADAPTER TO THE GRIDVIEW
            mGridview.setAdapter(new PhotosAdapter(getActivity(), arrPhotos));

            // CHANGE THE LOADING MORE STATUS
            loadingMore = false;
        }

    }

    public class getPhotos {

        String PhotoID;

        String PhotoName;

        String PhotoPicture;

        String PhotoSource;

        // SET THE PHOTO ID
        public void setPhotoID(String PhotoID)  {
            this.PhotoID = PhotoID;
        }

        // GET THE PHOTO ID
        public String getPhotoID()  {
            return PhotoID;
        }

        // SET THE PHOTO NAME
        public void setPhotoName(String PhotoName)  {
            this.PhotoName = PhotoName;
        }

        // GET THE PHOTO NAME
        public String getPhotoName()    {
            return PhotoName;
        }

        // SET THE PHOTO PICTURE
        public void setPhotoPicture(String PhotoPicture)    {
            this.PhotoPicture = PhotoPicture;
        }

        // GET THE PHOTO PICTURE
        public String getPhotoPicture() {
            return PhotoPicture;
        }

        // SET THE PHOTO SOURCE
        public void setPhotoSource(String PhotoSource)  {
            this.PhotoSource = PhotoSource;
        }

        // GET THE PHOTO SOURCE
        public String getPhotoSource()  {
            return PhotoSource;
        }
    }
    public static class PhotosAdapter extends BaseAdapter {

        private Activity activity;

        ArrayList<getPhotos> arrayPhotos;

        private static LayoutInflater inflater = null;
//        ImageLoader imageLoader;

        public PhotosAdapter(Activity a, ArrayList<getPhotos> arrPhotos) {

            activity = a;

            arrayPhotos = arrPhotos;

            inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            imageLoader = new ImageLoader(activity.getApplicationContext());
        }

        public int getCount() {
            return arrayPhotos.size();
        }

        public Object getItem(int position) {
            return arrayPhotos.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            View vi = convertView;
            if(convertView == null) {
                vi = inflater.inflate(R.layout.fragment_grid_view, null);

                holder = new ViewHolder();

                holder.imgPhoto = (ImageView)vi.findViewById(R.id.gridViewImage);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }


            if (arrayPhotos.get(position).getPhotoPicture() != null){
//                imageLoader.DisplayImage(arrayPhotos.get(position).getPhotoPicture(), holder.imgPhoto);
                Ion.with(holder.imgPhoto)
                        .placeholder(R.drawable.placeholder)
                        .centerCrop()
                        .error(R.drawable.placeholder)
                        .load(arrayPhotos.get(position).getPhotoPicture());
            }
            return vi;
        }

        static class ViewHolder {
            ImageView imgPhoto;

        }
    }

}