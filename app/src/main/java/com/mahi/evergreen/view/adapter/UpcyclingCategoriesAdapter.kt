package com.mahi.evergreen.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mahi.evergreen.R
import com.mahi.evergreen.model.UpcyclingCategory

class UpcyclingCategoriesAdapter(
    val isUpcyclingCreationAction: Boolean?,
    val upcyclingCategListener: UpcyclingCategoriesListener
) : RecyclerView.Adapter<UpcyclingCategoriesAdapter.ViewHolder>() {

    var listOfUpcycingCateg = ArrayList<UpcyclingCategory>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_upcycling_categories, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val category = listOfUpcycingCateg[position]

        Glide.with(holder.itemView.context) // contexto
            .load(category.image) // donde esta la url de la imagen
            .placeholder(R.drawable.shape_topico) // placeholder
            .into(holder.ivCategoryImage) // donde la vamos a colocar

        holder.tvCategoryName.text = category.name

        if(isUpcyclingCreationAction != true){
            holder.tvCategoryDesc.text = category.description
        } else {
            holder.tvCategoryDesc.text = category.creatorHelp
        }



        holder.itemView.setOnClickListener {
            upcyclingCategListener.onUpcyclingCategoryItemClicked(category, position)
        }
    }

    override fun getItemCount() = listOfUpcycingCateg.size

    fun updateData(data: List<UpcyclingCategory>) {
        listOfUpcycingCateg.clear()
        listOfUpcycingCateg.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCategoryImage = itemView.findViewById<ImageView>(R.id.ivCategoryImage)
        val tvCategoryName = itemView.findViewById<TextView>(R.id.tvCategoryName)
        val tvCategoryDesc = itemView.findViewById<TextView>(R.id.tvCategoryDesc)
    }
}