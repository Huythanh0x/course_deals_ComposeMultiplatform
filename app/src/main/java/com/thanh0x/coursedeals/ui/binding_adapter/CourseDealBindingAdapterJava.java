package com.thanh0x.coursedeals.ui.binding_adapter;

import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import com.thanh0x.coursedeals.R;
import com.thanh0x.coursedeals.util.MapperToView;
import com.squareup.picasso.Picasso;

public class CourseDealBindingAdapterJava {
    @BindingAdapter("loadImageFromUrl")
    public static void loadImageFromUrl(ImageView imageView, String url) {
        Picasso.get()
            .load(url)
            .error(R.drawable.error_loading_image)
            .placeholder(R.drawable.progress_animation)
            .into(imageView);
    }

    @BindingAdapter("displayNumberOfStudents")
    public static void displayNumberOfStudents(TextView textView, Integer numberOfStudent) {
        textView.setText(new MapperToView(textView.getContext()).mapNumberOfStudent(numberOfStudent));
    }

    @BindingAdapter("displayNumberOfReviews")
    public static void displayNumberOfReviews(TextView textView, Integer numberOfReview) {
        textView.setText(new MapperToView(textView.getContext()).mapNumberOfReview(numberOfReview));
    }

    @BindingAdapter("displayRating")
    public static void displayRating(RatingBar ratingBar, Double rating) {
        ratingBar.setRating(new MapperToView(ratingBar.getContext()).mapRating(rating));
    }

    @BindingAdapter("parseHTML")
    public static void parseHTML(TextView textView, String htmlString) {
        textView.setText(new MapperToView(textView.getContext()).mapHTMLContent(htmlString));
    }

    @BindingAdapter("contentLength")
    public static void contentLength(TextView textView, Integer contentLength) {
        textView.setText(new MapperToView(textView.getContext()).mapContentLength(contentLength));
    }

    @BindingAdapter("timeLeft")
    public static void timeLeft(TextView textView, Long expiredDate) {
        textView.setText(new MapperToView(textView.getContext()).mapTimeLeft(expiredDate));
    }
}
