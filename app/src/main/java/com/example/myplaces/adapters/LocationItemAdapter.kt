package com.example.myplaces.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myplaces.R
import com.example.myplaces.models.Location

open class LocationItemAdapter(private val context : Context,
                               private var list : ArrayList<Location>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnclickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_location, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_location_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_card_location_image))

            holder.itemView.findViewById<TextView>(R.id.tv_card_location_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_card_location_description).text = model.description

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnclickListener{
        fun onClick(position: Int, model: Location)
    }

    fun setOnClickListener(onClickListener: OnclickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View, ) : RecyclerView.ViewHolder(view) {

    }

}