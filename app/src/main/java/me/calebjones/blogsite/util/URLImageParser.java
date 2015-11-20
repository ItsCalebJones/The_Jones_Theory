package me.calebjones.blogsite.util;


import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.Html.ImageGetter;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import me.calebjones.blogsite.R;

public class URLImageParser implements ImageGetter {
    Context context;
    TextView container;
    private int width, height;

    public URLImageParser(TextView container, Context context) {
        this.context = context;
        this.container = container;
    }

    public Drawable getDrawable(String source) {
        Bitmap bitmap;
        if(source.matches("data:image.*base64.*")) {
            String base_64_source = source.replaceAll("data:image.*base64", "");
            byte[] data = Base64.decode(base_64_source, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Drawable image = new BitmapDrawable(context.getResources(), bitmap);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            return image;
        } else {
            URLDrawable urlDrawable = new URLDrawable();
            ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);
            asyncTask.execute(source);
            return urlDrawable; //return reference to URLDrawable where We will change with actual image from the src tag
        }
    }

    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            urlDrawable.setBounds(0, 0, width, height);//set the correct bound according to the result from HTTP call
            urlDrawable.drawable = result; //change the reference of the current drawable to the result from the HTTP call
            URLImageParser.this.container.invalidate(); //redraw the image by invalidating the container
            container.setText(container.getText());

        }

        public Drawable fetchDrawable(String urlString) {
            try {

                DisplayMetrics metrics;
                new DisplayMetrics();
                metrics = Resources.getSystem().getDisplayMetrics();
                InputStream is = (InputStream) new URL(urlString).getContent();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                Drawable drawable = new BitmapDrawable (context.getResources(), bmp);
                //Need logic here to calculate maximum width of image vs height so it doesnt strech
                int originalWidthScaled = (int) (drawable.getIntrinsicWidth() * metrics.density);
                int originalHeightScaled = (int) (drawable.getIntrinsicHeight() * metrics.density);
                if (originalWidthScaled > (metrics.widthPixels * 80) / 100) {
                    width = (metrics.widthPixels * 80) / 100;

                    height = drawable.getIntrinsicHeight() * width
                            / drawable.getIntrinsicWidth();
                }else {
                    height = originalHeightScaled;
                    width = originalWidthScaled;
                }
                drawable.setBounds(0, 0, width, height);
                return drawable;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
