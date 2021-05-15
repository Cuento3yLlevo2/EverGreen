package com.mahi.evergreen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.bumptech.glide.Glide
import com.mahi.evergreen.R

class PostImagesViewPagerAdapter(
    itemList: ArrayList<String>,
    isInfinite: Boolean
) : LoopingPagerAdapter<String>(itemList, isInfinite) {

    //This method will be triggered if the item View has not been inflated before.
    override fun inflateView(
        viewType: Int,
        container: ViewGroup,
        listPosition: Int
    ): View {
        return LayoutInflater.from(container.context).inflate(R.layout.item_post_image, container, false)
    }

    //Bind your data with your item View here.
    //Below is just an example in the demo app.
    //You can assume convertView will not be null here.
    //You may also consider using a ViewHolder pattern.
    override fun bindView(
        convertView: View,
        listPosition: Int,
        viewType: Int
    ) {

        val imageView = convertView.findViewById<ImageView>(R.id.ivPostImageView)

        Glide.with(convertView.context) // contexto
            .load(itemList?.get(listPosition)) // donde esta la url de la imagen
            .into(imageView) // donde la vamos a colocar
    }
}