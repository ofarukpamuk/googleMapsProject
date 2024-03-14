package com.example.googlemapsproject.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.googlemapsproject.databinding.RecyclerRowBinding
import com.example.googlemapsproject.model.Place
import com.example.googlemapsproject.view.MapsActivity

class PlaceAdapter(val placelist :List<Place>): RecyclerView.Adapter<PlaceAdapter.Placeholders>() {
    class Placeholders(val recyclerRowBinding: RecyclerRowBinding):RecyclerView.ViewHolder(recyclerRowBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Placeholders {
       val recyclerRowBinding= RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Placeholders(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return placelist.size
    }


    override fun onBindViewHolder(holder: Placeholders, position: Int) {
        holder.recyclerRowBinding.recyclerViewText.text = placelist.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, MapsActivity::class.java)
            intent.putExtra("selectedPlace", placelist.get(position))
            intent.putExtra("info","old") // sayfaya nereden gelindiğini anlayacak bu sayede
            holder.itemView.context.startActivity(intent)
        }
    }
}