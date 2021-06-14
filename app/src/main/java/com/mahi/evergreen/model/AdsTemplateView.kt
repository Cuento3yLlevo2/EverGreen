package com.mahi.evergreen.model

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mahi.evergreen.R


/**
 * Base class for a template view. *
 */
class AdsTemplateView : FrameLayout {
    private var templateType = 0
    private var nativeAd: NativeAd? = null
    private var rootNativeAdView: NativeAdView? = null
    private var adHeadlineView: TextView? = null
    // private var secondaryView: TextView? = null
    // private var ratingBar: RatingBar? = null
    private var adBodyView: TextView? = null
    private var iconView: ImageView? = null
    private var mediaView: MediaView? = null
    private var callToActionView: Button? = null
    // private var background: ConstraintLayout? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    /*
    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store: String? = nativeAd.store
        val advertiser: String? = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }

     */

    fun setNativeAd(nativeAd: NativeAd) {
        this.nativeAd = nativeAd
        // val store: String? = nativeAd.store
        // val advertiser: String? = nativeAd.advertiser
        val headline: String? = nativeAd.headline
        val body: String? = nativeAd.body
        val cta: String? = nativeAd.callToAction
        // val starRating: Double? = nativeAd.starRating
        val icon: NativeAd.Image? = nativeAd.icon
        rootNativeAdView?.callToActionView = callToActionView
        rootNativeAdView?.headlineView = adHeadlineView
        rootNativeAdView?.mediaView = mediaView
        /*
        secondaryView?.visibility = VISIBLE
        val secondaryText: String? = if (adHasOnlyStore(nativeAd)) {
            rootNativeAdView?.storeView = secondaryView
            store
        } else if (!TextUtils.isEmpty(advertiser)) {
            rootNativeAdView?.advertiserView = secondaryView
            advertiser
        } else {
            ""
        }

         */
        adHeadlineView?.text = headline
        callToActionView?.text = cta

        //  Set the secondary view to be the star rating if available.
        /*
        if (starRating != null && starRating > 0) {
            secondaryView?.visibility = GONE
            ratingBar?.visibility = VISIBLE
            ratingBar?.rating = starRating.toFloat()
            rootNativeAdView?.setStarRatingView(ratingBar)
        } else {
            secondaryView?.text = secondaryText
            secondaryView?.visibility = VISIBLE
            ratingBar?.visibility = GONE
        }

         */
        if (icon != null) {
            iconView?.visibility = VISIBLE
            iconView?.setImageDrawable(icon.drawable)
        } else {
            iconView?.visibility = GONE
        }
        if (adBodyView != null) {
            adBodyView!!.text = body
            rootNativeAdView!!.bodyView = adBodyView
        }
        rootNativeAdView!!.setNativeAd(nativeAd)
    }

    /**
     * To prevent memory leaks, make sure to destroy your ad when you don't need it anymore. This
     * method does not destroy the template view.
     * https://developers.google.com/admob/android/native-unified#destroy_ad
     */
    fun destroyNativeAd() {
        nativeAd?.destroy()
    }

    private fun initView(context: Context, attributeSet: AttributeSet?) {
        val attributes: TypedArray =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.TemplateView, 0, 0)
        templateType = try {
            attributes.getResourceId(
                R.styleable.TemplateView_gnt_template_type, R.layout.item_native_admod
            )
        } finally {
            attributes.recycle()
        }
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(templateType, this)
    }



    public override fun onFinishInflate() {
        super.onFinishInflate()
        rootNativeAdView = findViewById(R.id.postSponsoredNativeAdView)
        adHeadlineView = findViewById(R.id.tvPostSponsoredHeadline)
        // secondaryView = findViewById<View>(R.id.secondary) as TextView
        adBodyView = findViewById(R.id.tvPostSponsoredbody)
        // ratingBar = findViewById<View>(R.id.rating_bar) as RatingBar
        // ratingBar?.isEnabled = false
        callToActionView = findViewById(R.id.bPostSponsoredCallToAction)
        iconView = findViewById(R.id.ivPostSponsoredIcon)
        mediaView = findViewById(R.id.mvPostSponsoredMediaView)
    }

}
