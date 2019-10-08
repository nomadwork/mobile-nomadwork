package com.example.nomadwork.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.afollestad.viewpagerdots.DotsIndicator
import com.example.nomadwork.R
import com.squareup.picasso.Picasso

class WSDetailsImagesAdapter (private val context : Context, private val imagesList: List<String>) : PagerAdapter() {
    private var layoutInflater : LayoutInflater? = null

    private val dots = DotsIndicator

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view ===  `object`
    }

    override fun getCount(): Int {
        return imagesList.size
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = layoutInflater!!.inflate(R.layout.bottom_sheet_images , null)
        val image = v.findViewById<View>(R.id.wsDetailsImages) as ImageView

        image.loadUrl(imagesList[position])

        //image.setImageResource(Image[position])
        val vp = container as ViewPager
        vp.addView(v , 0)

        return v

    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val v = `object` as View
        vp.removeView(v)
    }

    private fun ImageView.loadUrl(url: String) {
        Picasso.get()
            .load(url)
            .error(android.R.drawable.stat_notify_error)
            .into(this)
    }

    override fun getItemPosition(`object`: Any): Int {

        return super.getItemPosition(`object`)
    }
}