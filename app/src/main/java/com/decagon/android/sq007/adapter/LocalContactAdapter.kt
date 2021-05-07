package com.decagon.android.sq007.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.decagon.android.sq007.R
import com.decagon.android.sq007.model.Contact

class LocalContactAdapter(private var contactList: List<Contact>, private val interaction: Interaction? = null) : RecyclerView.Adapter<LocalContactAdapter.RecyclerViewHolder>() {

    /*Create a view holder to display items*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {

        return RecyclerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.local_contact_list_item, parent, false),
            interaction
        )
    }

    /*Bind data to views and display the data at the specified position*/
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.itemView.apply {
            val contactName = findViewById<TextView>(R.id.tvLocalContactName)
            val contactCard = findViewById<CardView>(R.id.contactCard)
            val item = contactList[position]
            contactName.text = item.firstName

            contactCard.setOnClickListener {
                interaction?.onItemSelected(position, item)
            }
        }
    }

    /*Returns the total number of items in the data set held by the adapter*/
    override fun getItemCount(): Int {
        return contactList.size
    }

    /*Create the RecyclerView Holder*/
    class RecyclerViewHolder(itemView: View, private val interaction: Interaction?) : RecyclerView.ViewHolder(itemView)

    interface Interaction {
        fun onItemSelected(position: Int, item: Contact)
    }
}
