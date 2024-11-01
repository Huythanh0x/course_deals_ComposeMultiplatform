package com.batdaulaptrinh.freeudemycoupons.ui.binding_adapter

import android.widget.RatingBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import com.batdaulaptrinh.freeudemycoupons.R
import com.batdaulaptrinh.freeudemycoupons.util.MapperToView
import com.squareup.picasso.Picasso

object UdemyCouponBindingAdapter {
    @BindingAdapter("loadImageFromUrl")
    @JvmStatic
    fun loadImageFromUrl(imageView: AppCompatImageView, url: String) {
        Picasso.get()
            .load(url)
            .error(R.drawable.error_loading_image)
            .placeholder(R.drawable.progress_animation)
            .into(imageView)
    }

    @BindingAdapter("displayNumberOfStudents")
    @JvmStatic
    fun displayNumberOfStudents(
        textView: AppCompatTextView,
        numberOfStudent: Int
    ) {
        textView.text = MapperToView(textView.context).mapNumberOfStudent(numberOfStudent)
    }

    @BindingAdapter("displayNumberOfReviews")
    @JvmStatic
    fun displayNumberOfReviews(
        textView: AppCompatTextView,
        numberOfReview: Int
    ) {
        textView.text = MapperToView(textView.context).mapNumberOfReview(numberOfReview)
    }

    @BindingAdapter("displayRating")
    @JvmStatic
    fun displayRating(ratingBar: RatingBar, rating: Double) {
        ratingBar.rating = MapperToView(ratingBar.context).mapRating(rating)
    }

    @BindingAdapter("parseHTML")
    @JvmStatic
    fun parseHTML(textView: AppCompatTextView, htmlString: String) {
        textView.text = MapperToView(textView.context).mapHTMLContent(htmlString)
    }

    @BindingAdapter("contentLength")
    @JvmStatic
    fun contentLength(textView: AppCompatTextView, contentLength: Int) {
        textView.text = MapperToView(textView.context).mapContentLength(contentLength)
    }

    @BindingAdapter("timeLeft")
    @JvmStatic
    fun timeLeft(textView: AppCompatTextView, expiredDate: String) {
        textView.text = MapperToView(textView.context).mapTimeLeft(expiredDate)
    }
}
