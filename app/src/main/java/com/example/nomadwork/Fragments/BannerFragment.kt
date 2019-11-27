package com.example.nomadwork.Fragments


import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.nomadwork.Activity.MapsActivity
import com.example.nomadwork.Helpers.WorkStationHelper
import com.example.nomadwork.R
import com.example.nomadwork.models.WorkStationDetails
import com.squareup.picasso.Picasso
import retrofit2.http.Url

/**
 * A simple [Fragment] subclass.
 */
class BannerFragment : Fragment() {

    @BindView (R.id.banner_ws_name) lateinit var bannerWSName: TextView
    @BindView (R.id.banner_image) lateinit var bannerPhoto: ImageView
    var ws: WorkStationDetails? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_banner, container, false)
        ButterKnife.bind(this, v)

        bannerWSName.text = ws!!.workStationName
        bannerWSName.underline()
        bannerPhoto.loadUrl(ws!!.workStationPhotos[0])

        return v
    }

    @OnClick (R.id.banner_buttom)
    fun closeBanner(){
        activity!!.supportFragmentManager.beginTransaction().remove(this).commit()
    }

    private fun ImageView.loadUrl(url: String) {
        Picasso.get()
            .load(url)
            .error(android.R.drawable.stat_notify_error)
            .into(this)
    }

    @OnClick (R.id.banner_ws_name)
    fun showDetails(){
        (activity as MapsActivity).run {
            moveCamByFragment(ws!!)
            showWSDetails(ws!!)
        }
        closeBanner()
    }

    fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }
}
