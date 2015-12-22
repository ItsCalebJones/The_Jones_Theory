package me.calebjones.blogsite.util.images;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;


import java.io.InputStream;
import java.net.URL;

public class URLImageParser implements ImageGetter {
    Context context;
    TextView container;
    private int width, height;

    public URLImageParser(TextView container, Context context) {
        this.context = context;
        this.container = container;
    }

    public Drawable getDrawable(String source) {
        Log.d("The Jones Theory", "getDrawable: " + source);
        Bitmap bitmap;
        if(source.matches("data:image.*base64.*")) {
            Log.d("The Jones Theory", "getDrawable: if base64" + source);
            String base_64_source = source.replaceAll("data:image.*base64", "");
            byte[] data = Base64.decode(base_64_source, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Drawable image = new BitmapDrawable(context.getResources(), bitmap);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            return image;
        } else {
            Log.d("The Jones Theory", "getDrawable: not base64 " + source);
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
            Log.d("The Jones Theory", "getDrawable: doInBackground" + params[0]);
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            Log.d("The Jones Theory", "getDrawable: onPostExecute");
            urlDrawable.setBounds(0, 0, width, height);//set the correct bound according to the result from HTTP call
            urlDrawable.drawable = result; //change the reference of the current drawable to the result from the HTTP call
            URLImageParser.this.container.invalidate(); //redraw the image by invalidating the container
            container.setText(container.getText());
            Log.d("The Jones Theory", "getDrawable: onPostExecute finished");
        }

        public Drawable fetchDrawable(String urlString) {
            try {
                Log.d("The Jones Theory", "getDrawable: DisplayMetrics");
                DisplayMetrics metrics;
                new DisplayMetrics();
                metrics = Resources.getSystem().getDisplayMetrics();

                Log.d("The Jones Theory", "getDrawable: decodeStream");
                InputStream is = (InputStream) new URL(urlString).getContent();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                Drawable drawable = new BitmapDrawable (context.getResources(), bmp);
                Log.d("The Jones Theory", "getDrawable: decodeStream: " + bmp.getByteCount());

                Log.d("The Jones Theory", "getDrawable: calculate width/height");
                //Need logic here to calculate maximum width of image vs height so it doesnt strech
                int originalWidthScaled = (int) (drawable.getIntrinsicWidth() * metrics.density);
                int originalHeightScaled = (int) (drawable.getIntrinsicHeight() * metrics.density);
                if (originalWidthScaled > (metrics.widthPixels * 95) / 100) {
                    width = (metrics.widthPixels * 95) / 100;

                    height = drawable.getIntrinsicHeight() * width
                            / drawable.getIntrinsicWidth();
                }else {
                    height = originalHeightScaled;
                    width = originalWidthScaled;
                }

                Log.d("The Jones Theory", "getDrawable: setBounds");
                drawable.setBounds(0, 0, width, height);
                return drawable;
            } catch (Exception e) {
                return null;
            }
        }
    }
}